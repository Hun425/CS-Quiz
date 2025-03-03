// src/pages/BattleRoomPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { battleApi } from '../api/battleApi';
import { useAuthStore } from '../store/authStore';
import battleWebSocketService from '../services/BattleWebSocketService';
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
                            setIsReady(myParticipant.ready);
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
        };
    }, [battleRoom, isAuthenticated, roomId]);

    // 타이머 설정
    useEffect(() => {
        if (status !== 'IN_PROGRESS' || !currentQuestion || !timeLeft) return;

        const timer = setInterval(() => {
            setTimeLeft((prevTime) => {
                if (prevTime <= 1) {
                    clearInterval(timer);

                    // 시간이 다 됐는데 답변을 제출하지 않은 경우, 자동으로 빈 답변 제출
                    if (!answerSubmitted && currentQuestion) {
                        handleSubmitAnswer('');
                    }

                    return 0;
                }
                return prevTime - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [status, currentQuestion, timeLeft, answerSubmitted]);

    // 참가자 입장 이벤트 핸들러
    const handleParticipantJoin = (data: BattleJoinResponse) => {
        console.log('참가자 입장:', data);
        setParticipants(data.participants);
    };

    // 배틀 시작 이벤트 핸들러
    const handleBattleStart = (data: BattleStartResponse) => {
        console.log('배틀 시작:', data);
        setStatus('IN_PROGRESS');
        setStartTime(Date.now());

        // 첫 문제 설정
        if (data.firstQuestion) {
            setCurrentQuestion(data.firstQuestion);
            setTimeLeft(data.firstQuestion.timeLimit);
            setAnswerSubmitted(false);
            setSelectedAnswer('');
        }
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
            <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>배틀룸을 불러오는 중...</p>
            </div>
        );
    }

    // 에러 상태 UI
    if (error) {
        return (
            <div className="error-container" style={{
                backgroundColor: '#ffebee',
                color: '#d32f2f',
                padding: '1rem',
                borderRadius: '4px',
                marginBottom: '1rem'
            }}>
                <p>{error}</p>
                <button onClick={() => navigate('/battles')} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginTop: '0.5rem'
                }}>
                    배틀 목록으로 돌아가기
                </button>
            </div>
        );
    }

    // 배틀룸이 로드되지 않은 경우
    if (!battleRoom) {
        return (
            <div className="not-found-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>배틀룸을 찾을 수 없습니다.</p>
                <button onClick={() => navigate('/battles')} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginTop: '0.5rem'
                }}>
                    배틀 목록으로 돌아가기
                </button>
            </div>
        );
    }

    // 대기 상태 UI
    if (status === 'WAITING') {
        return (
            <div className="battle-waiting">
                <h1>{battleRoom.quizTitle} - 대기실</h1>

                <div className="room-info" style={{
                    backgroundColor: '#f5f5f5',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '1.5rem'
                }}>
                    <p><strong>방 코드:</strong> {battleRoom.roomCode}</p>
                    <p><strong>참가자:</strong> {battleRoom.currentParticipants}/{battleRoom.maxParticipants}</p>
                    <p><strong>문제 수:</strong> {battleRoom.questionCount}</p>
                    <p><strong>제한 시간:</strong> {battleRoom.timeLimit}분</p>
                </div>

                <div className="participants-list" style={{ marginBottom: '2rem' }}>
                    <h2>참가자 목록</h2>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                        gap: '1rem'
                    }}>
                        {participants.map((participant) => (
                            <div key={participant.userId} style={{
                                backgroundColor: 'white',
                                padding: '1rem',
                                borderRadius: '8px',
                                border: '1px solid #e0e0e0',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center'
                            }}>
                                {participant.profileImage ? (
                                    <img
                                        src={participant.profileImage}
                                        alt={participant.username}
                                        style={{
                                            width: '50px',
                                            height: '50px',
                                            borderRadius: '50%',
                                            objectFit: 'cover',
                                            marginBottom: '0.5rem'
                                        }}
                                    />
                                ) : (
                                    <div style={{
                                        width: '50px',
                                        height: '50px',
                                        borderRadius: '50%',
                                        backgroundColor: '#e0e0e0',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        marginBottom: '0.5rem',
                                        fontSize: '1.2rem',
                                        fontWeight: 'bold',
                                        color: '#666'
                                    }}>
                                        {participant.username[0].toUpperCase()}
                                    </div>
                                )}
                                <p style={{ margin: '0.5rem 0' }}>{participant.username}</p>
                                <p style={{ margin: '0', fontSize: '0.8rem', color: '#666' }}>레벨 {participant.level}</p>
                                <div style={{
                                    marginTop: '0.5rem',
                                    padding: '0.25rem 0.5rem',
                                    borderRadius: '4px',
                                    backgroundColor: participant.isReady ? '#e8f5e9' : '#ffebee',
                                    color: participant.isReady ? '#2e7d32' : '#d32f2f',
                                    fontSize: '0.8rem',
                                    fontWeight: 'bold'
                                }}>
                                    {participant.isReady ? '준비 완료' : '대기 중'}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="action-buttons" style={{
                    display: 'flex',
                    gap: '1rem',
                    justifyContent: 'center'
                }}>
                    <button
                        onClick={handleToggleReady}
                        style={{
                            padding: '0.75rem 1.5rem',
                            borderRadius: '4px',
                            backgroundColor: isReady ? '#f44336' : '#4caf50',
                            color: 'white',
                            border: 'none',
                            fontSize: '1rem',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}
                    >
                        {isReady ? '준비 취소' : '준비 완료'}
                    </button>

                    <button
                        onClick={handleLeaveBattle}
                        style={{
                            padding: '0.75rem 1.5rem',
                            borderRadius: '4px',
                            backgroundColor: 'white',
                            color: '#f44336',
                            border: '1px solid #f44336',
                            fontSize: '1rem',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}
                    >
                        나가기
                    </button>
                </div>
            </div>
        );
    }

    // 진행 중 상태 UI
    if (status === 'IN_PROGRESS' && currentQuestion) {
        return (
            <div className="battle-in-progress">
                <div className="battle-header" style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '1rem',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '4px',
                    marginBottom: '1.5rem'
                }}>
                    <div>
                        <h1 style={{ margin: 0, fontSize: '1.3rem' }}>{battleRoom.quizTitle}</h1>
                        {progress && (
                            <p style={{ margin: '0.5rem 0 0', fontSize: '0.9rem' }}>
                                문제 {progress.currentQuestionIndex + 1}/{progress.totalQuestions}
                            </p>
                        )}
                    </div>

                    <div className="timer" style={{
                        backgroundColor: timeLeft < 10 ? '#f44336' : '#1976d2',
                        color: 'white',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        fontWeight: 'bold',
                        fontSize: '1.1rem'
                    }}>
                        {formatTimeLeft()}
                    </div>
                </div>

                <div className="question-container" style={{
                    backgroundColor: 'white',
                    padding: '2rem',
                    borderRadius: