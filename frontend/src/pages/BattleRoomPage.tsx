// src/pages/BattleRoomPage.tsx - Complete Implementation
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
} from '../types/api';

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

    // WebSocket 연결 및 이벤트 핸들러 등록
    useEffect(() => {
        if (!battleRoom || !isAuthenticated || !roomId) return;

        // WebSocket 연결
        const connectWebSocket = async () => {
            try {
                await battleWebSocketService.connect(parseInt(roomId));

                // 각종 이벤트 핸들러 등록
                battleWebSocketService.on<BattleJoinResponse>('JOIN', handleParticipantJoin);
                battleWebSocketService.on<BattleStartResponse>('START', handleBattleStart);
                battleWebSocketService.on<BattleProgressResponse>('PROGRESS', handleBattleProgress);
                battleWebSocketService.on<BattleNextQuestionResponse>('NEXT_QUESTION', handleNextQuestion);
                battleWebSocketService.on<BattleEndResponse>('END', handleBattleEnd);
                battleWebSocketService.on<BattleAnswerResponse>('ANSWER', handleAnswerResult);
            } catch (err) {
                console.error('WebSocket 연결 오류:', err);
                setError('실시간 연결에 실패했습니다. 페이지를 새로고침 해주세요.');
            }
        };

        connectWebSocket();

        // 컴포넌트 언마운트 시 WebSocket 연결 종료
        return () => {
            battleWebSocketService.disconnect();
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, [battleRoom, isAuthenticated, roomId]);

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
        console.log('참가자 입장:', data);

        // 백엔드와의 타입 일치를 위해 명시적으로 맵핑 (isReady 속성은 이미 올바르게 포함됨)
        const updatedParticipants = data.participants.map(participant => ({
            userId: participant.userId,
            username: participant.username,
            profileImage: participant.profileImage,
            level: participant.level,
            isReady: participant.isReady
        }));

        setParticipants(updatedParticipants);
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
    };

    // 답변 결과 이벤트 핸들러
    const handleAnswerResult = (data: BattleAnswerResponse) => {
        console.log('답변 결과:', data);
        // 필요에 따라 추가 처리
    };

    // 준비 상태 토글
    const handleToggleReady = async () => {
        if (!roomId) return;

        try {
            const response = await battleApi.toggleReady(parseInt(roomId));

            if (response.data.success) {
                const updatedRoom = response.data.data;
                setBattleRoom(updatedRoom);

                // 내 준비 상태 토글
                setIsReady(!isReady);
            } else {
                setError('준비 상태 변경에 실패했습니다.');
            }
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
        if (!currentQuestion || !roomId) return;

        // 이미 제출한 경우 중복 제출 방지
        if (answerSubmitted) return;

        // 경과 시간 계산
        const timeSpentSeconds = Math.min(
            currentQuestion.timeLimit - timeLeft,
            currentQuestion.timeLimit
        );

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
                    {/* 준비 상태 토글 버튼 */}
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

                    {/* 나가기 버튼 */}
                    <button
                        onClick={handleLeaveBattle}
                        style={{
                            padding: '1rem 2rem',
                            borderRadius: '8px',
                            backgroundColor: '#9e9e9e',
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
                        width: '120px',
                        height: '120px',
                        borderRadius: '50%',
                        backgroundColor: 'rgba(255, 255, 255, 0.2)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '4rem',
                        fontWeight: 'bold',
                        marginBottom: '2rem',
                        border: '4px solid white',
                        animation: 'pulse 1s infinite'
                    }}>
                        {countdown}
                    </div>
                    <p style={{ fontSize: '1.2rem' }}>준비하세요! 곧 문제가 출제됩니다.</p>

                    <style>{`
                        @keyframes pulse {
                            0% { transform: scale(0.95); }
                            50% { transform: scale(1.05); }
                            100% { transform: scale(0.95); }
                        }
                    `}</style>
                </div>
            );
        }

        return (
            <div className="battle-in-progress" style={{
                maxWidth: '1000px',
                margin: '0 auto',
                padding: '1rem'
            }}>
                {/* 퀴즈 헤더 */}
                <div className="quiz-header" style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '1rem 1.5rem',
                    backgroundColor: '#1976d2',
                    color: 'white',
                    borderRadius: '8px',
                    marginBottom: '1.5rem',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                }}>
                    <div>
                        <h1 style={{ margin: 0, fontSize: '1.3rem' }}>{battleRoom.quizTitle}</h1>
                        <p style={{ margin: '0.5rem 0 0', fontSize: '0.9rem' }}>
                            문제 {progress?.currentQuestionIndex || 1}/{battleRoom.questionCount}
                        </p>
                    </div>

                    <div className="timer" style={{
                        backgroundColor: timeLeft < 10 ? '#f44336' : '#1976d2',
                        color: 'white',
                        padding: '0.5rem 1rem',
                        borderRadius: '20px',
                        fontWeight: 'bold',
                        fontSize: '1.2rem',
                        border: '2px solid white',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                    }}>
                        <span style={{ fontSize: '1rem' }}>⏱️</span>
                        {formatTimeLeft()}
                    </div>
                </div>

                {/* 진행 상태 표시 */}
                <div className="progress-bar" style={{
                    marginBottom: '1.5rem',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '8px',
                    padding: '1rem',
                    boxShadow: '0 2px 6px rgba(0,0,0,0.05)'
                }}>
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        marginBottom: '0.5rem'
                    }}>
                        <span>진행도</span>
                        <span>{progress?.currentQuestionIndex || 1}/{battleRoom.questionCount}</span>
                    </div>
                    <div style={{
                        width: '100%',
                        height: '8px',
                        backgroundColor: '#e0e0e0',
                        borderRadius: '4px',
                        overflow: 'hidden'
                    }}>
                        <div style={{
                            width: `${((progress?.currentQuestionIndex || 1) / battleRoom.questionCount) * 100}%`,
                            height: '100%',
                            backgroundColor: '#1976d2',
                            borderRadius: '4px',
                            transition: 'width 0.3s ease-in-out'
                        }}></div>
                    </div>
                </div>

                {/* 참가자 점수 */}
                <div className="participants-scores" style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '1rem',
                    marginBottom: '1.5rem'
                }}>
                    {progress && progress.participantProgress && Object.values(progress.participantProgress).map((participant) => (
                        <div key={participant.userId} style={{
                            flex: '1',
                            minWidth: '150px',
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '8px',
                            border: '1px solid #e0e0e0',
                            boxShadow: '0 2px 6px rgba(0,0,0,0.05)',
                            position: 'relative'
                        }}>
                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                marginBottom: '0.5rem'
                            }}>
                                <span style={{ fontWeight: 'bold' }}>{participant.username}</span>
                                {participant.hasAnsweredCurrent && (
                                    <span style={{
                                        marginLeft: 'auto',
                                        backgroundColor: '#4caf50',
                                        color: 'white',
                                        padding: '0.2rem 0.4rem',
                                        borderRadius: '4px',
                                        fontSize: '0.7rem',
                                        fontWeight: 'bold'
                                    }}>
                                        답변 완료
                                    </span>
                                )}
                            </div>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center'
                            }}>
                                <div>
                                    <span style={{ color: '#666', fontSize: '0.8rem' }}>점수</span>
                                    <p style={{
                                        margin: '0',
                                        fontWeight: 'bold',
                                        fontSize: '1.3rem',
                                        color: '#1976d2'
                                    }}>
                                        {participant.currentScore}
                                    </p>
                                </div>
                                <div>
                                    <span style={{ color: '#666', fontSize: '0.8rem' }}>정답률</span>
                                    <p style={{ margin: '0', fontWeight: 'bold' }}>
                                        {participant.correctAnswers}/{participant.totalAnswered}
                                        ({((participant.correctAnswers / Math.max(participant.totalAnswered, 1)) * 100).toFixed(0)}%)
                                    </p>
                                </div>
                            </div>
                            {participant.currentStreak > 2 && (
                                <div style={{
                                    position: 'absolute',
                                    top: '-10px',
                                    right: '-10px',
                                    backgroundColor: '#ff9800',
                                    color: 'white',
                                    padding: '0.2rem 0.5rem',
                                    borderRadius: '20px',
                                    fontSize: '0.8rem',
                                    fontWeight: 'bold',
                                    boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '0.25rem'
                                }}>
                                    <span>{participant.currentStreak}연속</span>
                                    <span style={{ fontSize: '1rem' }}>🔥</span>
                                </div>
                            )}
                        </div>
                    ))}
                </div>

                {/* 현재 문제 */}
                {currentQuestion && (
                    <div className="current-question" style={{
                        backgroundColor: 'white',
                        padding: '1.5rem',
                        borderRadius: '8px',
                        marginBottom: '1.5rem',
                        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                        position: 'relative'
                    }}>
                        <div style={{
                            position: 'absolute',
                            top: '1rem',
                            right: '1rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                            backgroundColor: '#f5f5f5',
                            padding: '0.25rem 0.5rem',
                            borderRadius: '4px',
                            fontSize: '0.8rem',
                            color: '#666'
                        }}>
                            <span>배점</span>
                            <span style={{ fontWeight: 'bold', color: '#1976d2' }}>{currentQuestion.points}점</span>
                        </div>

                        <h2>문제</h2>
                        <p style={{ fontSize: '1.1rem', marginBottom: '1.5rem' }}>{currentQuestion.questionText}</p>

                        <div className="options" style={{ marginBottom: '1.5rem' }}>
                            {currentQuestion.questionType === 'MULTIPLE_CHOICE' && currentQuestion.options.map((option, index) => (
                                <div
                                    key={index}
                                    onClick={() => !answerSubmitted && setSelectedAnswer(option)}
                                    style={{
                                        padding: '1rem',
                                        marginBottom: '0.5rem',
                                        border: `2px solid ${selectedAnswer === option ? '#1976d2' : '#e0e0e0'}`,
                                        borderRadius: '8px',
                                        cursor: answerSubmitted ? 'default' : 'pointer',
                                        backgroundColor: selectedAnswer === option ? '#e3f2fd' : 'white',
                                        transition: 'all 0.2s ease',
                                        opacity: answerSubmitted && selectedAnswer !== option ? 0.7 : 1,
                                    }}
                                >
                                    <label style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        cursor: answerSubmitted ? 'default' : 'pointer',
                                        width: '100%'
                                    }}>
                                        <input
                                            type="radio"
                                            name="answer"
                                            value={option}
                                            checked={selectedAnswer === option}
                                            onChange={() => !answerSubmitted && setSelectedAnswer(option)}
                                            disabled={answerSubmitted}
                                            style={{ marginRight: '1rem' }}
                                        />
                                        <span style={{ flex: 1 }}>{option}</span>
                                        {answerSubmitted && selectedAnswer === option && (
                                            <span style={{ marginLeft: 'auto', color: '#1976d2', fontWeight: 'bold' }}>선택됨</span>
                                        )}
                                    </label>
                                </div>
                            ))}

                            {currentQuestion.questionType === 'TRUE_FALSE' && (
                                <div style={{ display: 'flex', gap: '1rem' }}>
                                    {['True', 'False'].map((option) => (
                                        <div
                                            key={option}
                                            onClick={() => !answerSubmitted && setSelectedAnswer(option)}
                                            style={{
                                                flex: '1',
                                                padding: '1rem',
                                                textAlign: 'center',
                                                border: `2px solid ${selectedAnswer === option ? '#1976d2' : '#e0e0e0'}`,
                                                borderRadius: '8px',
                                                cursor: answerSubmitted ? 'default' : 'pointer',
                                                backgroundColor: selectedAnswer === option ? '#e3f2fd' : 'white',
                                                fontWeight: 'bold',
                                                fontSize: '1.1rem',
                                                transition: 'all 0.2s ease',
                                                opacity: answerSubmitted && selectedAnswer !== option ? 0.7 : 1,
                                            }}
                                        >
                                            {option === 'True' ? '참 (True)' : '거짓 (False)'}
                                            {answerSubmitted && selectedAnswer === option && (
                                                <div style={{
                                                    fontSize: '0.8rem',
                                                    color: '#1976d2',
                                                    marginTop: '0.5rem'
                                                }}>선택됨</div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}

                            {(currentQuestion.questionType === 'SHORT_ANSWER' ||
                                currentQuestion.questionType === 'CODE_ANALYSIS' ||
                                currentQuestion.questionType === 'DIAGRAM_BASED') && (
                                <textarea
                                    value={selectedAnswer}
                                    onChange={(e) => !answerSubmitted && setSelectedAnswer(e.target.value)}
                                    placeholder="답변을 입력하세요..."
                                    disabled={answerSubmitted}
                                    style={{
                                        width: '100%',
                                        padding: '1rem',
                                        borderRadius: '8px',
                                        border: '2px solid #e0e0e0',
                                        fontSize: '1rem',
                                        minHeight: '120px',
                                        resize: 'vertical'
                                    }}
                                />
                            )}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'center' }}>
                            <button
                                onClick={() => handleSubmitAnswer()}
                                disabled={answerSubmitted || !selectedAnswer}
                                style={{
                                    padding: '1rem 2rem',
                                    backgroundColor: answerSubmitted ? '#9e9e9e' : (selectedAnswer ? '#4caf50' : '#9e9e9e'),
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '1.1rem',
                                    fontWeight: 'bold',
                                    cursor: answerSubmitted || !selectedAnswer ? 'default' : 'pointer',
                                    opacity: answerSubmitted || !selectedAnswer ? 0.7 : 1,
                                    boxShadow: answerSubmitted || !selectedAnswer ? 'none' : '0 3px 8px rgba(0,0,0,0.2)',
                                    transition: 'all 0.2s ease',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '0.5rem'
                                }}
                            >
                                {answerSubmitted ? (
                                    <>
                                        <span>제출 완료</span>
                                        <span style={{ fontSize: '1.2rem' }}>✓</span>
                                    </>
                                ) : (
                                    <>
                                        <span>답변 제출</span>
                                        <span style={{ fontSize: '1.2rem' }}>→</span>
                                    </>
                                )}
                            </button>
                        </div>

                        {answerSubmitted && (
                            <div style={{
                                marginTop: '1.5rem',
                                padding: '1rem',
                                backgroundColor: '#e8f5e9',
                                borderRadius: '8px',
                                display: 'flex',
                                alignItems: 'center'
                            }}>
                                <div style={{
                                    backgroundColor: '#4caf50',
                                    borderRadius: '50%',
                                    color: 'white',
                                    width: '24px',
                                    height: '24px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    marginRight: '0.5rem',
                                    fontWeight: 'bold'
                                }}>✓</div>
                                <p style={{ margin: 0 }}>
                                    답변이 제출되었습니다. 다른 참가자들을 기다리는 중입니다...
                                </p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        );
    }

    // 종료 상태 UI
    if (status === 'FINISHED' && result) {
        return (
            <div className="battle-finished" style={{
                maxWidth: '1000px',
                margin: '0 auto',
                padding: '2rem 1rem'
            }}>
                <div style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    padding: '2rem',
                    borderRadius: '8px',
                    marginBottom: '2rem',
                    textAlign: 'center',
                    boxShadow: '0 4px 15px rgba(0,0,0,0.15)'
                }}>
                    <h1 style={{ margin: '0 0 1rem', fontSize: '2rem' }}>대결 종료!</h1>
                    <p style={{ margin: '0', fontSize: '1.2rem', opacity: 0.9 }}>
                        {battleRoom.quizTitle} 대결이 종료되었습니다.
                    </p>
                </div>

                {/* 최종 결과 */}
                <div className="results-summary" style={{
                    backgroundColor: 'white',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '2rem',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                }}>
                    <h2 style={{
                        borderBottom: '2px solid #1976d2',
                        paddingBottom: '0.5rem',
                        marginBottom: '1.5rem'
                    }}>최종 결과</h2>

                    <div className="result-stats" style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                        gap: '1.5rem',
                        marginBottom: '1.5rem'
                    }}>
                        <div>
                            <p style={{ color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem' }}>총 문제 수</p>
                            <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.3rem' }}>
                                {result.totalQuestions}문제
                            </p>
                        </div>
                        <div>
                            <p style={{ color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem' }}>소요 시간</p>
                            <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.3rem' }}>
                                {Math.floor(result.timeTakenSeconds / 60)}분 {result.timeTakenSeconds % 60}초
                            </p>
                        </div>
                        <div>
                            <p style={{ color: '#666', fontSize: '0.9rem', margin: '0 0 0.25rem' }}>완료 일시</p>
                            <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.3rem' }}>
                                {new Date(result.endTime).toLocaleString()}
                            </p>
                        </div>
                    </div>

                    {/* 우승자 표시 */}
                    {result.results.length > 0 && (
                        <div className="winner-section" style={{
                            backgroundColor: '#fff9c4',
                            padding: '1.5rem',
                            borderRadius: '8px',
                            marginBottom: '2rem',
                            textAlign: 'center',
                            position: 'relative',
                            overflow: 'hidden'
                        }}>
                            <div style={{
                                position: 'absolute',
                                top: '0',
                                left: '0',
                                width: '100%',
                                height: '5px',
                                background: 'linear-gradient(to right, #ffd700, #ffeb3b, #ffd700)'
                            }}></div>

                            <h3 style={{ margin: '0 0 1rem', color: '#ff6f00' }}>
                                <span style={{ fontSize: '1.5rem', marginRight: '0.5rem' }}>🏆</span>
                                우승자
                            </h3>

                            {result.results.filter(p => p.isWinner).map(winner => (
                                <div key={winner.userId} style={{ marginBottom: '1rem' }}>
                                    <p style={{
                                        margin: '0 0 0.5rem',
                                        fontSize: '1.5rem',
                                        fontWeight: 'bold',
                                        color: '#ff6f00'
                                    }}>
                                        {winner.username}
                                    </p>
                                    <p style={{ margin: '0', fontSize: '1.2rem' }}>
                                        점수: <strong>{winner.finalScore}점</strong> •
                                        정답률: <strong>{Math.round((winner.correctAnswers / result.totalQuestions) * 100)}%</strong>
                                    </p>
                                </div>
                            ))}

                            <p style={{
                                margin: '1rem 0 0',
                                color: '#795548'
                            }}>
                                <span style={{ marginRight: '0.5rem' }}>✨</span>
                                경험치 {result.results.find(p => p.isWinner)?.experienceGained || 0}점을 획득했습니다!
                            </p>
                        </div>
                    )}

                    {/* 참가자 순위 */}
                    <h3 style={{ marginBottom: '1rem' }}>참가자 순위</h3>
                    <div className="participants-ranking">
                        {/* 테이블 헤더 */}
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: '50px 2fr 1fr 1fr 1fr',
                            backgroundColor: '#f5f5f5',
                            padding: '0.75rem 1rem',
                            borderRadius: '8px 8px 0 0',
                            fontWeight: 'bold',
                            color: '#666'
                        }}>
                            <div>순위</div>
                            <div>이름</div>
                            <div style={{ textAlign: 'center' }}>점수</div>
                            <div style={{ textAlign: 'center' }}>정답</div>
                            <div style={{ textAlign: 'center' }}>경험치</div>
                        </div>

                        {/* 테이블 내용 */}
                        {result.results
                            .sort((a, b) => b.finalScore - a.finalScore)
                            .map((participant, index) => (
                                <div
                                    key={participant.userId}
                                    style={{
                                        display: 'grid',
                                        gridTemplateColumns: '50px 2fr 1fr 1fr 1fr',
                                        padding: '1rem',
                                        borderBottom: '1px solid #e0e0e0',
                                        backgroundColor: participant.isWinner ? '#fff8e1' : 'white',
                                    }}
                                >
                                    <div style={{
                                        fontWeight: 'bold',
                                        color: index === 0 ? '#ff6f00' : '#666'
                                    }}>
                                        {index + 1}
                                    </div>
                                    <div style={{
                                        fontWeight: 'bold',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '0.5rem'
                                    }}>
                                        {participant.isWinner && <span style={{ fontSize: '1.2rem' }}>🏆</span>}
                                        {participant.username}
                                    </div>
                                    <div style={{
                                        textAlign: 'center',
                                        fontWeight: 'bold',
                                        color: '#1976d2'
                                    }}>
                                        {participant.finalScore}
                                    </div>
                                    <div style={{ textAlign: 'center' }}>
                                        {participant.correctAnswers}/{result.totalQuestions}
                                    </div>
                                    <div style={{
                                        textAlign: 'center',
                                        color: '#4caf50',
                                        fontWeight: 'bold'
                                    }}>
                                        +{participant.experienceGained}
                                    </div>
                                </div>
                            ))}
                    </div>
                </div>

                {/* 문제별 결과 (선택적) */}
                {result.results.length > 0 && result.results[0].questionResults && (
                    <div className="question-results" style={{
                        backgroundColor: 'white',
                        padding: '1.5rem',
                        borderRadius: '8px',
                        marginBottom: '2rem',
                        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                    }}>
                        <h3 style={{
                            borderBottom: '2px solid #1976d2',
                            paddingBottom: '0.5rem',
                            marginBottom: '1.5rem'
                        }}>문제별 결과</h3>

                        {/* 여기에 문제별 결과를 표시하는 UI를 추가할 수 있습니다 */}
                    </div>
                )}

                {/* 액션 버튼 */}
                <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '2rem' }}>
                    <button
                        onClick={() => navigate('/battles')}
                        style={{
                            padding: '0.75rem 1.5rem',
                            backgroundColor: '#1976d2',
                            color: 'white',
                            border: 'none',
                            borderRadius: '8px',
                            fontSize: '1rem',
                            cursor: 'pointer',
                            boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem'
                        }}
                    >
                        <span>배틀 목록으로</span>
                        <span style={{ fontSize: '1.2rem' }}>→</span>
                    </button>
                </div>
            </div>
        );
    }

    // 기본 상태 (이 부분은 도달하지 않아야 함)
    return <div>로딩 중...</div>;
};

export default BattleRoomPage;
