// src/pages/BattleRoomPage.tsx - Updated Implementation
import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { battleApi } from '../api/battleApi';
import { useAuthStore } from '../store/authStore';
import battleWebSocketService from '../service/BattleWebSocketService';
import {
    BattleRoomResponse,
    BattleJoinResponse,
    BattleStartResponse,
    BattleProgressResponse,
    BattleNextQuestionResponse,
    BattleEndResponse,
    BattleAnswerResponse
} from '../types/battle';

/**
 * 배틀룸 페이지 컴포넌트
 * WebSocket을 이용한 실시간 퀴즈 대결을 제공합니다.
 */
const BattleRoomPage: React.FC = () => {
    const { roomId } = useParams<{ roomId: string }>();
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAuthStore();
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    // 상태 관리
    const [battleRoom, setBattleRoom] = useState<BattleRoomResponse | null>(null);
    const [status, setStatus] = useState<'WAITING' | 'IN_PROGRESS' | 'FINISHED'>('WAITING');
    const [isReady, setIsReady] = useState<boolean>(false);
    const [currentQuestion, setCurrentQuestion] = useState<BattleNextQuestionResponse | null>(null);
    const [selectedAnswer, setSelectedAnswer] = useState<string>('');
    const [timeLeft, setTimeLeft] = useState<number>(0);
    const [startTime, setStartTime] = useState<number>(0);
    const [participants, setParticipants] = useState<BattleJoinResponse['participants']>([]);
    const [progress, setProgress] = useState<BattleProgressResponse | null>(null);
    const [result, setResult] = useState<BattleEndResponse | null>(null);
    const [answerSubmitted, setAnswerSubmitted] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [countdownActive, setCountdownActive] = useState<boolean>(false);
    const [countdown, setCountdown] = useState<number>(3);
    const [isWebSocketConnected, setIsWebSocketConnected] = useState<boolean>(false);

    // 배틀 상태 변경 이벤트 핸들러
    const handleStatusChange = (data: any) => {
        console.log('배틀 상태 변경:', data);

        // 배틀 상태 업데이트
        if (data.status) {
            setStatus(data.status);
        }

        // 배틀이 종료된 경우
        if (data.status === 'FINISHED') {
            // 결과 페이지로 리다이렉트 준비
            // 실제 리다이렉트는 END 이벤트에서 처리하므로 여기서는 상태만 업데이트
            console.log('배틀이 종료되었습니다. 결과 대기 중...');
        }
    };

    // 인증 확인
    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: `/battles/${roomId}` } });
            return;
        }
    }, [isAuthenticated, navigate, roomId]);

    // 배틀룸 정보 로드
    useEffect(() => {
        const fetchBattleRoom = async () => {
            if (!roomId) return;

            try {
                setLoading(true);
                const response = await battleApi.getBattleRoom(parseInt(roomId));

                if (response.data.success) {
                    const room = response.data.data;
                    setBattleRoom(room);
                    setStatus(room.status as any);

                    // 내 준비 상태 확인
                    if (user) {
                        const myParticipant = room.participants.find(p => p.userId === user.id);
                        if (myParticipant) {
                            setIsReady(myParticipant.isReady);
                        }
                    }

                    setParticipants(room.participants);

                    // 이미 진행 중인 배틀인 경우
                    if (room.status === 'IN_PROGRESS') {
                        // 상태 업데이트
                        setStatus('IN_PROGRESS');
                    }
                } else {
                    setError('배틀룸을 불러오는 데 실패했습니다.');
                }
            } catch (err: any) {
                console.error('배틀룸 로딩 중 오류:', err);
                setError('배틀룸을 불러오는 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchBattleRoom();
    }, [roomId, user]);

    // WebSocket 연결 관련 useEffect - 개선된 버전
    useEffect(() => {
        if (!isAuthenticated || !roomId) return;

        // 마운트 여부를 추적하는 변수
        let isMounted = true;

        // WebSocket 연결 및 초기화를 한 번만 실행
        const initializeWebSocket = async () => {
            try {
                console.log("배틀룸 WebSocket 연결 초기화 시작");
                setLoading(true);

                // WebSocket 연결 시도 전에 이벤트 핸들러 등록 제거
                battleWebSocketService.off('PARTICIPANTS');
                battleWebSocketService.off('START');
                battleWebSocketService.off('PROGRESS');
                battleWebSocketService.off('NEXT_QUESTION');
                battleWebSocketService.off('END');
                battleWebSocketService.off('ANSWER');
                battleWebSocketService.off('STATUS');

                if (isMounted) {
                    // 이벤트 핸들러 등록 - 연결 전에 등록하여 첫 이벤트 놓치지 않도록
                    battleWebSocketService.on<BattleJoinResponse>('PARTICIPANTS', handleParticipantJoin);
                    battleWebSocketService.on<BattleStartResponse>('START', handleBattleStart);
                    battleWebSocketService.on<BattleProgressResponse>('PROGRESS', handleBattleProgress);
                    battleWebSocketService.on<BattleNextQuestionResponse>('NEXT_QUESTION', handleNextQuestion);
                    battleWebSocketService.on<BattleEndResponse>('END', handleBattleEnd);
                    battleWebSocketService.on<BattleAnswerResponse>('ANSWER', handleAnswerResult);
                    battleWebSocketService.on<any>('STATUS', handleStatusChange);
                }

                // WebSocket 연결 시도
                await battleWebSocketService.connect(parseInt(roomId));

                if (isMounted) {
                    console.log("WebSocket 연결 성공");
                    setIsWebSocketConnected(true);
                    setLoading(false);
                }
            } catch (err) {
                console.error("WebSocket 연결 오류:", err);
                if (isMounted) {
                    setError("실시간 연결에 실패했습니다. 페이지를 새로고침하거나 다시 시도해주세요.");
                    setIsWebSocketConnected(false);
                    setLoading(false);
                }
            }
        };

        // 초기화 함수 호출
        initializeWebSocket();

        // 컴포넌트 언마운트 시 정리
        return () => {
            isMounted = false;
            console.log("배틀룸 컴포넌트 언마운트 - WebSocket 연결 정리");

            // 이벤트 핸들러 제거
            battleWebSocketService.off('PARTICIPANTS');
            battleWebSocketService.off('START');
            battleWebSocketService.off('PROGRESS');
            battleWebSocketService.off('NEXT_QUESTION');
            battleWebSocketService.off('END');
            battleWebSocketService.off('ANSWER');
            battleWebSocketService.off('STATUS');

            // 타이머 정리
            if (timerRef.current) {
                clearInterval(timerRef.current);
                timerRef.current = null;
            }

            // 현재 이 컴포넌트에서 연결한 방과 동일한 방에 대한 연결만 종료
            if (battleWebSocketService.getCurrentRoomId() === parseInt(roomId)) {
                battleWebSocketService.disconnect().catch(err => {
                    console.error('WebSocket 연결 종료 중 오류:', err);
                });
            }
        };
    }, [roomId, isAuthenticated]); // battleRoom 의존성 제거, 필수 의존성만 유지

    // 타이머 설정
    useEffect(() => {
        if (status !== 'IN_PROGRESS' || !currentQuestion || !timeLeft) return;

        if (timerRef.current) {
            clearInterval(timerRef.current);
        }

        timerRef.current = setInterval(() => {
            setTimeLeft((prevTime) => {
                if (prevTime <= 1) {
                    clearInterval(timerRef.current as NodeJS.Timeout);

                    // 시간이 다 됐는데 답변을 제출하지 않은 경우, 자동으로 빈 답변 제출
                    if (!answerSubmitted && currentQuestion) {
                        handleSubmitAnswer('');
                    }

                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, [status, currentQuestion, timeLeft, answerSubmitted]);

    // 카운트다운 효과
    useEffect(() => {
        if (!countdownActive) return;

        const countdownTimer = setInterval(() => {
            setCountdown(prev => {
                if (prev <= 1) {
                    clearInterval(countdownTimer);
                    setCountdownActive(false);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(countdownTimer);
    }, [countdownActive]);

    // 참가자 입장 이벤트 핸들러
    const handleParticipantJoin = (data: BattleJoinResponse) => {
        console.log('참가자 입장/상태 변경:', data);

        // 백엔드와의 타입 일치를 위해 명시적으로 맵핑 (isReady 속성은 이미 올바르게 포함됨)
        const updatedParticipants = data.participants.map(participant => ({
            userId: participant.userId,
            username: participant.username,
            profileImage: participant.profileImage,
            level: participant.level,
            isReady: participant.isReady
        }));

        setParticipants(updatedParticipants);

        // 내 준비 상태 확인 및 업데이트
        if (user) {
            const myParticipant = updatedParticipants.find(p => p.userId === user.id);
            if (myParticipant) {
                setIsReady(myParticipant.isReady);
            }
        }
    };

    // 배틀 시작 이벤트 핸들러
    const handleBattleStart = (data: BattleStartResponse) => {
        console.log('배틀 시작:', data);
        setStatus('IN_PROGRESS');
        setStartTime(Date.now());
        setCountdownActive(true);
        setCountdown(3);

        // 타이머 설정하여 카운트다운 후 첫 문제 표시
        setTimeout(() => {
            // 첫 문제 설정
            if (data.firstQuestion) {
                setCurrentQuestion(data.firstQuestion);
                setTimeLeft(data.firstQuestion.timeLimit);
                setAnswerSubmitted(false);
                setSelectedAnswer('');
            }
        }, 3000);
    };

    // 배틀 진행 상황 이벤트 핸들러
    const handleBattleProgress = (data: BattleProgressResponse) => {
        console.log('배틀 진행 상황:', data);
        setProgress(data);

        // 배틀 상태 업데이트
        if (data.status) {
            setStatus(data.status);
        }
    };

    // 다음 문제 이벤트 핸들러
    const handleNextQuestion = (data: BattleNextQuestionResponse) => {
        console.log('다음 문제:', data);

        if (data.isGameOver) {
            // 게임 종료 시 처리
            setStatus('FINISHED');
        } else {
            // 새 문제 설정
            setCurrentQuestion(data);
            setTimeLeft(data.timeLimit);
            setAnswerSubmitted(false);
            setSelectedAnswer('');
        }
    };

    // 배틀 종료 이벤트 핸들러
    const handleBattleEnd = (data: BattleEndResponse) => {
        console.log('배틀 종료:', data);
        setStatus('FINISHED');
        setResult(data);

        // 결과 페이지로 상태와 함께 리다이렉트
        navigate(`/battles/${roomId}/results`, { state: { result: data } });
    };

    // 답변 결과 이벤트 핸들러
    const handleAnswerResult = (data: BattleAnswerResponse) => {
        console.log('답변 결과:', data);
        // 필요에 따라 추가 처리
    };

    // 준비 상태 토글
    const handleToggleReady = () => {
        if (!isWebSocketConnected) {
            console.error('WebSocket 연결이 설정되지 않았습니다.');
            setError('서버와의 연결이 원활하지 않습니다. 페이지를 새로고침해보세요.');
            return;
        }

        console.log('준비 상태 토글 요청');
        try {
            // WebSocket을 통해 준비 상태 토글 요청
            battleWebSocketService.toggleReady();
        } catch (err) {
            console.error('준비 상태 변경 중 오류:', err);
            setError('준비 상태 변경 중 오류가 발생했습니다.');
        }
    };

    // 배틀룸 나가기
    const handleLeaveBattle = async () => {
        if (!roomId) return;

        try {
            const response = await battleApi.leaveBattleRoom(parseInt(roomId));

            if (response.data.success) {
                // WebSocket 연결 종료 및 목록 페이지로 이동
                battleWebSocketService.disconnect();
                navigate('/battles');
            } else {
                setError('배틀룸 나가기에 실패했습니다.');
            }
        } catch (err) {
            console.error('배틀룸 나가기 중 오류:', err);
            setError('배틀룸 나가기 중 오류가 발생했습니다.');
        }
    };

    // 답변 제출
    const handleSubmitAnswer = (answer: string = selectedAnswer) => {
        if (!currentQuestion || !roomId || !isWebSocketConnected) return;

        // 이미 제출한 경우 중복 제출 방지
        if (answerSubmitted) return;

        // 경과 시간 계산
        const timeSpentSeconds = Math.min(
            currentQuestion.timeLimit - timeLeft,
            currentQuestion.timeLimit
        );

        console.log('답변 제출:', {
            questionId: currentQuestion.questionId,
            answer,
            timeSpentSeconds
        });

        // WebSocket으로 답변 제출
        battleWebSocketService.submitAnswer(
            currentQuestion.questionId,
            answer,
            timeSpentSeconds
        );

        // 제출 상태 업데이트
        setAnswerSubmitted(true);
    };

    // 남은 시간 포맷팅
    const formatTimeLeft = () => {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    // 로딩 상태 UI
    if (loading) {
        return (
            <div className="loading-container" style={{
                textAlign: 'center',
                padding: '2rem',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '50vh'
            }}>
                <div style={{
                    width: '50px',
                    height: '50px',
                    border: '5px solid #f3f3f3',
                    borderTop: '5px solid #1976d2',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite',
                    marginBottom: '1rem'
                }}></div>
                <p style={{ fontSize: '1.2rem' }}>배틀룸을 불러오는 중...</p>
                <style>{`
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                `}</style>
            </div>
        );
    }

    // 에러 상태 UI
    if (error) {
        return (
            <div className="error-container" style={{
                backgroundColor: '#ffebee',
                color: '#d32f2f',
                padding: '2rem',
                borderRadius: '8px',
                marginBottom: '1rem',
                maxWidth: '600px',
                margin: '2rem auto',
                textAlign: 'center',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h2 style={{ marginTop: 0 }}>오류가 발생했습니다</h2>
                <p style={{ marginBottom: '1.5rem' }}>{error}</p>
                <button onClick={() => navigate('/battles')} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginTop: '0.5rem',
                    fontSize: '1rem',
                    fontWeight: 'bold',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                }}>
                    배틀 목록으로 돌아가기
                </button>
            </div>
        );
    }

    // 배틀룸이 로드되지 않은 경우
    if (!battleRoom) {
        return (
            <div className="not-found-container" style={{
                textAlign: 'center',
                padding: '2rem',
                maxWidth: '600px',
                margin: '2rem auto',
                backgroundColor: '#f5f5f5',
                borderRadius: '8px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h2 style={{ marginTop: 0 }}>배틀룸을 찾을 수 없습니다</h2>
                <p style={{ marginBottom: '1.5rem' }}>요청하신 배틀룸이 존재하지 않거나 이미 종료되었습니다.</p>
                <button onClick={() => navigate('/battles')} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    fontWeight: 'bold',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                }}>
                    배틀 목록으로 돌아가기
                </button>
            </div>
        );
    }

    // 대기 상태 UI
    if (status === 'WAITING') {
        return (
            <div className="battle-waiting" style={{
                maxWidth: '1000px',
                margin: '0 auto',
                padding: '2rem 1rem'
            }}>
                <div style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '2rem',
                    boxShadow: '0 4px 10px rgba(0,0,0,0.1)'
                }}>
                    <h1 style={{margin: 0, fontSize: '1.8rem'}}>{battleRoom.quizTitle} - 대기실</h1>
                    <p style={{margin: '0.5rem 0 0', opacity: 0.8}}>대결이 시작되기를 기다리는 중입니다</p>
                </div>

                <div className="room-info" style={{
                    backgroundColor: '#f5f5f5',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '1.5rem',
                    boxShadow: '0 2px 6px rgba(0,0,0,0.05)'
                }}>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                        gap: '1rem'
                    }}>
                        <div>
                            <p style={{color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem'}}>방 코드</p>
                            <p style={{margin: 0, fontWeight: 'bold', fontSize: '1.2rem'}}>{battleRoom.roomCode}</p>
                        </div>
                        <div>
                            <p style={{color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem'}}>참가자</p>
                            <p style={{margin: 0, fontWeight: 'bold', fontSize: '1.2rem'}}>
                                {battleRoom.currentParticipants}/{battleRoom.maxParticipants}
                            </p>
                        </div>
                        <div>
                            <p style={{color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem'}}>문제 수</p>
                            <p style={{
                                margin: 0,
                                fontWeight: 'bold',
                                fontSize: '1.2rem'
                            }}>{battleRoom.questionCount}문제</p>
                        </div>
                        <div>
                            <p style={{color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem'}}>제한 시간</p>
                            <p style={{margin: 0, fontWeight: 'bold', fontSize: '1.2rem'}}>{battleRoom.timeLimit}분</p>
                        </div>
                    </div>
                </div>

                <div className="participants-list" style={{marginBottom: '2rem'}}>
                    <h2 style={{borderBottom: '2px solid #1976d2', paddingBottom: '0.5rem'}}>참가자 목록</h2>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                        gap: '1.5rem',
                        marginTop: '1.5rem'
                    }}>
                        {participants.map((participant) => (
                            <div key={participant.userId} style={{
                                backgroundColor: 'white',
                                padding: '1.5rem',
                                borderRadius: '8px',
                                border: '1px solid #e0e0e0',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                boxShadow: '0 2px 6px rgba(0,0,0,0.05)',
                                transition: 'transform 0.2s, box-shadow 0.2s',
                                position: 'relative'
                            }}>
                                {participant.isReady && (
                                    <div style={{
                                        position: 'absolute',
                                        top: '10px',
                                        right: '10px',
                                        backgroundColor: '#4caf50',
                                        color: 'white',
                                        borderRadius: '50%',
                                        width: '24px',
                                        height: '24px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontSize: '0.8rem',
                                        fontWeight: 'bold'
                                    }}>
                                        ✓
                                    </div>
                                )}
                                {participant.profileImage ? (
                                    <img
                                        src={participant.profileImage}
                                        alt={participant.username}
                                        style={{
                                            width: '80px',
                                            height: '80px',
                                            borderRadius: '50%',
                                            objectFit: 'cover',
                                            marginBottom: '1rem',
                                            border: '3px solid ' + (participant.isReady ? '#4caf50' : '#e0e0e0')
                                        }}
                                    />
                                ) : (
                                    <div style={{
                                        width: '80px',
                                        height: '80px',
                                        borderRadius: '50%',
                                        backgroundColor: '#e0e0e0',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        marginBottom: '1rem',
                                        fontSize: '2rem',
                                        fontWeight: 'bold',
                                        color: '#666',
                                        border: '3px solid ' + (participant.isReady ? '#4caf50' : '#e0e0e0')
                                    }}>
                                        {participant.username[0].toUpperCase()}
                                    </div>
                                )}
                                <p style={{
                                    margin: '0.5rem 0',
                                    fontSize: '1.2rem',
                                    fontWeight: 'bold'
                                }}>{participant.username}</p>
                                <p style={{
                                    margin: '0',
                                    fontSize: '0.9rem',
                                    color: '#666',
                                    backgroundColor: '#f5f5f5',
                                    padding: '0.25rem 0.5rem',
                                    borderRadius: '4px'
                                }}>레벨 {participant.level}</p>
                                <div style={{
                                    marginTop: '1rem',
                                    padding: '0.5rem 1rem',
                                    borderRadius: '20px',
                                    backgroundColor: participant.isReady ? '#e8f5e9' : '#ffebee',
                                    color: participant.isReady ? '#2e7d32' : '#d32f2f',
                                    fontSize: '0.9rem',
                                    fontWeight: 'bold',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '0.25rem'
                                }}>
                                    {participant.isReady ? (
                                        <>
                                            <span>준비 완료</span>
                                            <span style={{fontSize: '1.2rem'}}>✓</span>
                                        </>
                                    ) : (
                                        <>
                                            <span>대기 중</span>
                                            <span style={{fontSize: '1.2rem'}}>⌛</span>
                                        </>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="action-buttons" style={{
                    display: 'flex',
                    gap: '1rem',
                    justifyContent: 'center',
                    marginTop: '2rem'
                }}>
                    <button
                        onClick={handleLeaveBattle}
                        style={{
                            padding: '1rem 2rem',
                            borderRadius: '8px',
                            backgroundColor: '#f44336',
                            color: 'white',
                            border: 'none',
                            fontSize: '1.1rem',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            transition: 'all 0.2s ease'
                        }}
                    >
                        나가기
                    </button>
                    <button
                        onClick={handleToggleReady}
                        style={{
                            padding: '1rem 2rem',
                            borderRadius: '8px',
                            backgroundColor: isReady ? '#f44336' : '#4caf50',
                            color: 'white',
                            border: 'none',
                            fontSize: '1.1rem',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            transition: 'all 0.2s ease'
                        }}
                    >
                        {isReady ? '준비 취소' : '준비 완료'}
                    </button>
                </div>

                {/* 준비 안내 메시지 */}
                <div style={{
                    backgroundColor: '#e3f2fd',
                    borderRadius: '8px',
                    padding: '1rem',
                    marginTop: '2rem',
                    display: 'flex',
                    alignItems: 'center',
                    boxShadow: '0 2px 6px rgba(0,0,0,0.05)'
                }}>
                    <div style={{
                        backgroundColor: '#1976d2',
                        borderRadius: '50%',
                        color: 'white',
                        width: '30px',
                        height: '30px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        marginRight: '1rem',
                        fontWeight: 'bold',
                        fontSize: '1.2rem'
                    }}>i
                    </div>
                    <p style={{margin: 0}}>
                        모든 참가자가 준비 완료되면 대결이 자동으로 시작됩니다. 준비 버튼을 클릭하여 대결 준비를 완료하세요!
                    </p>
                </div>
            </div>
        );
    }

    // 진행 중 상태 UI
    if (status === 'IN_PROGRESS') {
        // 카운트다운 표시
        if (countdownActive) {
            return (
                <div className="battle-countdown" style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    height: '70vh',
                    backgroundColor: '#1976d2',
                    color: 'white',
                    borderRadius: '8px',
                    maxWidth: '800px',
                    margin: '2rem auto',
                    padding: '2rem',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.2)'
                }}>
                    <h1 style={{marginBottom: '2rem', fontSize: '2rem'}}>대결 시작!</h1>
                    <div style={{
                        fontSize: '8rem',
                        fontWeight: 'bold',
                        animation: 'pulse 1s infinite',
                        textShadow: '0 0 20px rgba(255,255,255,0.5)'
                    }}>
                        {countdown}
                    </div>
                    <p style={{fontSize: '1.5rem', marginTop: '2rem'}}>곧 퀴즈가 시작됩니다!</p>
                    <style>{`
                        @keyframes pulse {
                            0% { transform: scale(1); }
                            50% { transform: scale(1.1); }
                            100% { transform: scale(1); }
                        }
                    `}</style>
                </div>
            );
        }

        return (
            <div className="battle-in-progress" style={{
                maxWidth: '1000px',
                margin: '0 auto',
                padding: '2rem 1rem'
            }}>
                {/* 상단 정보 표시 */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '2rem',
                    backgroundColor: '#f5f5f5',
                    padding: '1rem',
                    borderRadius: '8px',
                    boxShadow: '0 2px 6px rgba(0,0,0,0.05)'
                }}>
                    <div>
                        <h1 style={{margin: 0, fontSize: '1.6rem'}}>{battleRoom.quizTitle}</h1>
                        <p style={{margin: '0.25rem 0 0', color: '#666'}}>
                            {currentQuestion ? `문제 ${progress?.currentQuestionIndex + 1 || '1'}/${battleRoom.questionCount}` : ''}
                        </p>
                    </div>
                    <div style={{
                        backgroundColor: timeLeft <= 10 ? '#f44336' : '#1976d2',
                        color: 'white',
                        borderRadius: '8px',
                        padding: '0.75rem 1.5rem',
                        fontWeight: 'bold',
                        fontSize: '1.8rem',
                        boxShadow: timeLeft <= 10 ? '0 0 10px rgba(244, 67, 54, 0.5)' : 'none',
                        animation: timeLeft <= 10 ? 'pulse-time 1s infinite' : 'none'
                    }}>
                        {formatTimeLeft()}
                    </div>
                </div>

                {/* 문제 영역 */}
                {currentQuestion && (
                    <div className="question-container" style={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        padding: '2rem',
                        marginBottom: '2rem',
                        boxShadow: '0 4px 10px rgba(0,0,0,0.1)'
                    }}>
                        <h2 style={{
                            fontSize: '1.5rem',
                            marginTop: 0,
                            marginBottom: '1.5rem',
                            borderBottom: '2px solid #1976d2',
                            paddingBottom: '0.75rem'
                        }}>
                            {currentQuestion.questionText}
                        </h2>

                        {/* 코드 스니펫은 battle.ts에는 없으므로 제거 */}

                        {/* 선택지 목록 */}
                        <div className="options-container" style={{
                            display: 'grid',
                            gridTemplateColumns: '1fr 1fr',
                            gap: '1rem',
                            marginTop: '1.5rem'
                        }}>
                            {currentQuestion.options?.map((option, index) => (
                                <button
                                    key={index}
                                    onClick={() => {
                                        if (!answerSubmitted) {
                                            setSelectedAnswer(option);
                                            // 바로 제출하지 않고, 선택만 표시
                                        }
                                    }}
                                    disabled={answerSubmitted}
                                    style={{
                                        padding: '1.5rem',
                                        borderRadius: '8px',
                                        border: selectedAnswer === option
                                            ? '2px solid #1976d2'
                                            : '1px solid #e0e0e0',
                                        backgroundColor: selectedAnswer === option ? '#e3f2fd' : 'white',
                                        cursor: answerSubmitted ? 'default' : 'pointer',
                                        fontSize: '1.1rem',
                                        textAlign: 'center',
                                        transition: 'all 0.2s ease',
                                        boxShadow: selectedAnswer === option
                                            ? '0 0 10px rgba(25, 118, 210, 0.3)'
                                            : '0 2px 5px rgba(0,0,0,0.05)',
                                        opacity: answerSubmitted ? 0.7 : 1
                                    }}
                                >
                                    {`${String.fromCharCode(65 + index)}. ${option}`}
                                </button>
                            ))}
                        </div>

                        {/* 답변 제출 버튼 */}
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            marginTop: '2rem'
                        }}>
                            <button
                                onClick={() => handleSubmitAnswer()}
                                disabled={!selectedAnswer || answerSubmitted}
                                style={{
                                    padding: '1rem 2.5rem',
                                    backgroundColor: answerSubmitted ? '#9e9e9e' : '#4caf50',
                                    color: 'white',
                                    borderRadius: '8px',
                                    border: 'none',
                                    fontSize: '1.2rem',
                                    fontWeight: 'bold',
                                    cursor: answerSubmitted ? 'default' : 'pointer',
                                    transition: 'all 0.2s ease',
                                    opacity: (!selectedAnswer || answerSubmitted) ? 0.7 : 1,
                                    boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                                }}
                            >
                                {answerSubmitted ? '답변 제출 완료' : '답변 제출하기'}
                            </button>
                        </div>
                    </div>
                )}

                {/* 참가자 현황 */}
                {progress && (
                    <div className="progress-container" style={{
                        backgroundColor: '#f5f5f5',
                        borderRadius: '8px',
                        padding: '1.5rem',
                        boxShadow: '0 2px 6px rgba(0,0,0,0.05)'
                    }}>
                        <h3 style={{
                            margin: '0 0 1rem 0',
                            fontSize: '1.2rem',
                            borderBottom: '1px solid #e0e0e0',
                            paddingBottom: '0.5rem'
                        }}>
                            참가자 현황
                        </h3>
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
                            gap: '1rem'
                        }}>
                            {progress && Object.values(progress.participantProgress).map((participant) => (
                                <div key={participant.userId} style={{
                                    backgroundColor: 'white',
                                    borderRadius: '8px',
                                    padding: '1rem',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: 'center',
                                    boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                                    border: participant.userId === user?.id ? '2px solid #1976d2' : '1px solid #e0e0e0'
                                }}>
                                    <div style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        marginBottom: '0.5rem',
                                        width: '100%'
                                    }}>
                                        {/* 참가자의 프로필 이미지는 progress에 없을 수 있으므로 기본 이니셜 표시 */}
                                        <div style={{
                                            width: '40px',
                                            height: '40px',
                                            borderRadius: '50%',
                                            backgroundColor: '#e0e0e0',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            marginRight: '0.75rem',
                                            fontWeight: 'bold'
                                        }}>
                                            {participant.username[0]}
                                        </div>
                                        <div>
                                            <p style={{
                                                margin: 0,
                                                fontWeight: 'bold',
                                                fontSize: '0.9rem'
                                            }}>{participant.username}</p>
                                            <p style={{
                                                margin: 0,
                                                fontSize: '0.8rem',
                                                color: '#666'
                                            }}>
                                                {participant.correctAnswers}/{participant.totalAnswered} 정답
                                            </p>
                                        </div>
                                    </div>
                                    <div style={{
                                        width: '100%',
                                        backgroundColor: '#f5f5f5',
                                        borderRadius: '4px',
                                        padding: '0.5rem',
                                        marginTop: '0.5rem',
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'center'
                                    }}>
                                        <span style={{fontWeight: 'bold', fontSize: '0.9rem'}}>점수:</span>
                                        <span style={{
                                            backgroundColor: '#1976d2',
                                            color: 'white',
                                            padding: '0.25rem 0.5rem',
                                            borderRadius: '4px',
                                            fontWeight: 'bold',
                                            fontSize: '0.9rem'
                                        }}>{participant.currentScore}</span>
                                    </div>
                                    {participant.hasAnsweredCurrent && (
                                        <div style={{
                                            marginTop: '0.5rem',
                                            padding: '0.25rem 0.75rem',
                                            borderRadius: '20px',
                                            backgroundColor: '#e8f5e9',
                                            color: '#2e7d32',
                                            fontSize: '0.8rem',
                                            fontWeight: 'bold',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '0.25rem'
                                        }}>
                                            답변 완료 ✓
                                        </div>
                                    )}
                                    {participant.currentStreak > 1 && (
                                        <div style={{
                                            marginTop: '0.5rem',
                                            padding: '0.25rem 0.75rem',
                                            borderRadius: '20px',
                                            backgroundColor: '#fff3e0',
                                            color: '#e65100',
                                            fontSize: '0.8rem',
                                            fontWeight: 'bold'
                                        }}>
                                            {participant.currentStreak} 연속 정답 🔥
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <style>{`
                    @keyframes pulse-time {
                        0% { transform: scale(1); }
                        50% { transform: scale(1.05); }
                        100% { transform: scale(1); }
                    }
                `}</style>
            </div>
        )
    }
};
export default BattleRoomPage;