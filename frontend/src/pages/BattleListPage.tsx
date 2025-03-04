// src/pages/BattleListPage.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { battleApi } from '../api/battleApi';
import { quizApi } from '../api/quizApi';
import { BattleRoomResponse, QuizSummaryResponse } from '../types/api';
import { useAuthStore } from '../store/authStore';

/**
 * 배틀 목록 페이지 컴포넌트
 * 사용자가 활성화된 배틀룸을 확인하고 참여하거나 새로운 배틀룸을 생성할 수 있습니다.
 */
const BattleListPage: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuthStore();

    // 상태 관리
    const [battleRooms, setBattleRooms] = useState<BattleRoomResponse[]>([]);
    const [myActiveRoom, setMyActiveRoom] = useState<BattleRoomResponse | null>(null);
    const [quizzes, setQuizzes] = useState<QuizSummaryResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [createModalOpen, setCreateModalOpen] = useState<boolean>(false);
    const [selectedQuizId, setSelectedQuizId] = useState<number | null>(null);
    const [maxParticipants, setMaxParticipants] = useState<number>(4);
    const [createLoading, setCreateLoading] = useState<boolean>(false);

    // 인증 확인
    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: '/battles' } });
            return;
        }
    }, [isAuthenticated, navigate]);

    // 데이터 로드
    useEffect(() => {
        const loadData = async () => {
            try {
                setLoading(true);

                // 1. 활성화된 배틀룸 목록 조회
                const roomsResponse = await battleApi.getActiveBattleRooms();
                if (roomsResponse.data.success) {
                    setBattleRooms(roomsResponse.data.data);
                }

                // 2. 내 활성 배틀룸 조회 (이미 참여 중인 배틀이 있는지)
                try {
                    const myRoomResponse = await battleApi.getMyActiveBattleRoom();
                    if (myRoomResponse.data.success) {
                        setMyActiveRoom(myRoomResponse.data.data);
                    }
                } catch (err) {
                    // 활성 배틀이 없는 경우는 에러가 아님
                    console.log('활성 배틀룸이 없습니다.');
                }

                // 3. 퀴즈 목록 조회 (배틀룸 생성에 사용)
                const quizzesResponse = await quizApi.searchQuizzes({}, 0, 10);
                if (quizzesResponse.data.success) {
                    setQuizzes(quizzesResponse.data.data.content);
                }
            } catch (err: any) {
                console.error('데이터 로딩 중 오류:', err);
                setError('배틀룸 데이터를 불러오는 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        loadData();

        // 주기적으로 배틀룸 목록 갱신
        const intervalId = setInterval(() => {
            if (!createModalOpen) {
                refreshBattleRooms();
            }
        }, 10000); // 10초마다 갱신

        return () => clearInterval(intervalId);
    }, [isAuthenticated, createModalOpen]);

    // 배틀룸 목록 새로고침
    const refreshBattleRooms = async () => {
        try {
            const roomsResponse = await battleApi.getActiveBattleRooms();
            if (roomsResponse.data.success) {
                setBattleRooms(roomsResponse.data.data);
            }
        } catch (err) {
            console.error('배틀룸 새로고침 중 오류:', err);
        }
    };

    // 배틀룸 참가
    const handleJoinBattle = async (roomId: number) => {
        try {
            setLoading(true);
            const response = await battleApi.joinBattleRoom(roomId);

            if (response.data.success) {
                // 참가 성공 시 해당 배틀룸으로 이동
                navigate(`/battles/${roomId}`);
            } else {
                setError('배틀룸 참가에 실패했습니다.');
            }
        } catch (err: any) {
            console.error('배틀룸 참가 중 오류:', err);
            setError(err.response?.data?.message || '배틀룸 참가 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 배틀룸 생성 모달 토글
    const toggleCreateModal = () => {
        setCreateModalOpen(!createModalOpen);
        // 모달 열 때 첫 번째 퀴즈를 기본 선택
        if (!createModalOpen && quizzes.length > 0) {
            setSelectedQuizId(quizzes[0].id);
        }
    };

    // 배틀룸 생성
    const handleCreateBattle = async () => {
        if (!selectedQuizId) {
            setError('퀴즈를 선택해주세요.');
            return;
        }

        try {
            setCreateLoading(true);
            const response = await battleApi.createBattleRoom({
                quizId: selectedQuizId,
                maxParticipants: maxParticipants
            });

            if (response.data.success) {
                // 생성 성공 시 해당 배틀룸으로 이동
                setCreateModalOpen(false);
                navigate(`/battles/${response.data.data.id}`);
            } else {
                setError('배틀룸 생성에 실패했습니다.');
            }
        } catch (err: any) {
            console.error('배틀룸 생성 중 오류:', err);
            setError(err.response?.data?.message || '배틀룸 생성 중 오류가 발생했습니다.');
        } finally {
            setCreateLoading(false);
        }
    };

    // 진행 중인 배틀로 이동
    const handleContinueBattle = () => {
        if (myActiveRoom) {
            navigate(`/battles/${myActiveRoom.id}`);
        }
    };

    // 난이도에 따른 색상 및 라벨
    const getDifficultyColor = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '#4caf50';
            case 'INTERMEDIATE': return '#ff9800';
            case 'ADVANCED': return '#f44336';
            default: return '#9e9e9e';
        }
    };

    const getDifficultyLabel = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '입문';
            case 'INTERMEDIATE': return '중급';
            case 'ADVANCED': return '고급';
            default: return '알 수 없음';
        }
    };

    // 로딩 상태 UI
    if (loading && !createModalOpen) {
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
                <p style={{ fontSize: '1.2rem' }}>배틀 목록을 불러오는 중...</p>
                <style>{`
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                `}</style>
            </div>
        );
    }

    return (
        <div className="battle-list-page" style={{
            maxWidth: '1200px',
            margin: '0 auto',
            padding: '2rem 1rem'
        }}>
            {/* 헤더 */}
            <div style={{
                backgroundColor: '#1976d2',
                color: 'white',
                padding: '2rem',
                borderRadius: '8px',
                marginBottom: '2rem',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '1rem',
                boxShadow: '0 4px 10px rgba(0,0,0,0.1)'
            }}>
                <div>
                    <h1 style={{ margin: '0 0 0.5rem', fontSize: '2rem' }}>퀴즈 대결</h1>
                    <p style={{ margin: '0', opacity: '0.9' }}>
                        다른 사용자들과 실시간으로 퀴즈를 풀며 경쟁해보세요!
                    </p>
                </div>

                <div style={{ display: 'flex', gap: '1rem' }}>
                    {myActiveRoom ? (
                        <button
                            onClick={handleContinueBattle}
                            style={{
                                padding: '0.75rem 1.5rem',
                                backgroundColor: '#4caf50',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '0.5rem'
                            }}
                        >
                            <span>진행중인 대결 이어하기</span>
                            <span style={{ fontSize: '1.2rem' }}>→</span>
                        </button>
                    ) : (
                        <button
                            onClick={toggleCreateModal}
                            style={{
                                padding: '0.75rem 1.5rem',
                                backgroundColor: 'white',
                                color: '#1976d2',
                                border: 'none',
                                borderRadius: '8px',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '0.5rem'
                            }}
                        >
                            <span>새 대결 만들기</span>
                            <span style={{ fontSize: '1.2rem' }}>+</span>
                        </button>
                    )}
                </div>
            </div>

            {/* 에러 메시지 */}
            {error && (
                <div style={{
                    backgroundColor: '#ffebee',
                    color: '#d32f2f',
                    padding: '1rem',
                    borderRadius: '8px',
                    marginBottom: '1.5rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '1rem'
                }}>
                    <span style={{ fontSize: '1.5rem' }}>⚠️</span>
                    <div>
                        <p style={{ margin: '0', fontWeight: 'bold' }}>오류가 발생했습니다</p>
                        <p style={{ margin: '0.25rem 0 0' }}>{error}</p>
                    </div>
                    <button
                        onClick={() => setError(null)}
                        style={{
                            marginLeft: 'auto',
                            backgroundColor: 'transparent',
                            border: 'none',
                            color: '#d32f2f',
                            cursor: 'pointer',
                            fontSize: '1.2rem',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            width: '28px',
                            height: '28px',
                            borderRadius: '50%'
                        }}
                    >
                        ×
                    </button>
                </div>
            )}

            {/* 내 활성 배틀룸 표시 */}
            {myActiveRoom && (
                <div style={{
                    backgroundColor: '#e8f5e9',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '2rem',
                    border: '1px solid #4caf50',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
                }}>
                    <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flexWrap: 'wrap',
                        gap: '1rem'
                    }}>
                        <div>
                            <h3 style={{ margin: '0 0 0.5rem', color: '#2e7d32' }}>
                                <span style={{ marginRight: '0.5rem' }}>🎮</span>
                                진행 중인 대결
                            </h3>
                            <p style={{ margin: '0', fontSize: '1.2rem', fontWeight: 'bold' }}>
                                {myActiveRoom.quizTitle}
                            </p>
                            <p style={{ margin: '0.25rem 0 0', color: '#666' }}>
                                상태: {myActiveRoom.status === 'WAITING' ? '대기 중' : '진행 중'} •
                                참가자: {myActiveRoom.currentParticipants}/{myActiveRoom.maxParticipants}
                            </p>
                        </div>

                        <button
                            onClick={handleContinueBattle}
                            style={{
                                padding: '0.75rem 1.5rem',
                                backgroundColor: '#4caf50',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                            }}
                        >
                            이어하기
                        </button>
                    </div>
                </div>
            )}

            {/* 활성화된 배틀룸 목록 */}
            <div className="battle-rooms-section">
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '1rem'
                }}>
                    <h2 style={{ margin: 0 }}>활성화된 대결</h2>
                    <button
                        onClick={refreshBattleRooms}
                        style={{
                            backgroundColor: 'transparent',
                            border: 'none',
                            color: '#1976d2',
                            cursor: 'pointer',
                            fontSize: '0.9rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.25rem'
                        }}
                    >
                        <span style={{ fontSize: '1rem' }}>🔄</span>
                        새로고침
                    </button>
                </div>

                {battleRooms.length === 0 ? (
                    <div style={{
                        backgroundColor: '#f5f5f5',
                        padding: '2rem',
                        borderRadius: '8px',
                        textAlign: 'center'
                    }}>
                        <p style={{ margin: '0 0 1rem', fontSize: '1.1rem' }}>
                            현재 활성화된 대결이 없습니다.
                        </p>
                        <button
                            onClick={toggleCreateModal}
                            style={{
                                padding: '0.75rem 1.5rem',
                                backgroundColor: '#1976d2',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                cursor: 'pointer',
                                boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                            }}
                        >
                            새 대결 만들기
                        </button>
                    </div>
                ) : (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                        gap: '1.5rem'
                    }}>
                        {battleRooms.map((room) => (
                            <div
                                key={room.id}
                                style={{
                                    backgroundColor: 'white',
                                    borderRadius: '8px',
                                    overflow: 'hidden',
                                    border: '1px solid #e0e0e0',
                                    boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                                    transition: 'transform 0.2s, box-shadow 0.2s',
                                    display: 'flex',
                                    flexDirection: 'column'
                                }}
                            >
                                <div style={{
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    padding: '1rem',
                                    position: 'relative'
                                }}>
                                    <span style={{
                                        position: 'absolute',
                                        top: '0.5rem',
                                        right: '0.5rem',
                                        backgroundColor: '#ffffff33',
                                        padding: '0.25rem 0.5rem',
                                        borderRadius: '4px',
                                        fontSize: '0.8rem',
                                        fontWeight: 'bold'
                                    }}>
                                        방 코드: {room.roomCode}
                                    </span>
                                    <h3 style={{
                                        margin: '0.5rem 0 0.25rem',
                                        fontSize: '1.2rem',
                                        paddingRight: '4rem'
                                    }}>{room.quizTitle}</h3>
                                    <p style={{ margin: '0', fontSize: '0.9rem', opacity: '0.9' }}>
                                        {room.questionCount}문제 • {room.timeLimit}분
                                    </p>
                                </div>

                                <div style={{ padding: '1rem', flex: '1', display: 'flex', flexDirection: 'column' }}>
                                    <div style={{ marginBottom: '1rem' }}>
                                        <div style={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            marginBottom: '0.5rem'
                                        }}>
                                            <span style={{ color: '#666', fontSize: '0.9rem' }}>참가자</span>
                                            <span style={{ fontWeight: 'bold' }}>
                                                {room.currentParticipants}/{room.maxParticipants}
                                            </span>
                                        </div>
                                        <div style={{
                                            width: '100%',
                                            height: '8px',
                                            backgroundColor: '#e0e0e0',
                                            borderRadius: '4px',
                                            overflow: 'hidden'
                                        }}>
                                            <div style={{
                                                width: `${(room.currentParticipants / room.maxParticipants) * 100}%`,
                                                height: '100%',
                                                backgroundColor: room.currentParticipants < room.maxParticipants ? '#4caf50' : '#f44336',
                                                borderRadius: '4px'
                                            }}></div>
                                        </div>
                                    </div>

                                    <div style={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        gap: '0.5rem',
                                        marginTop: 'auto'
                                    }}>
                                        {/* 참가자 프로필 이미지 또는 이니셜 */}
                                        {room.participants.map((participant) => (
                                            <div key={participant.userId} style={{
                                                position: 'relative',
                                                width: '40px',
                                                height: '40px'
                                            }}>
                                                {participant.profileImage ? (
                                                    <img
                                                        src={participant.profileImage}
                                                        alt={participant.username}
                                                        style={{
                                                            width: '100%',
                                                            height: '100%',
                                                            borderRadius: '50%',
                                                            objectFit: 'cover',
                                                            border: '2px solid ' + (participant.ready ? '#4caf50' : '#e0e0e0')
                                                        }}
                                                    />
                                                ) : (
                                                    <div style={{
                                                        width: '100%',
                                                        height: '100%',
                                                        borderRadius: '50%',
                                                        backgroundColor: '#e0e0e0',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        justifyContent: 'center',
                                                        fontWeight: 'bold',
                                                        border: '2px solid ' + (participant.ready ? '#4caf50' : '#e0e0e0')
                                                    }}>
                                                        {participant.username[0].toUpperCase()}
                                                    </div>
                                                )}
                                                {participant.ready && (
                                                    <div style={{
                                                        position: 'absolute',
                                                        bottom: '0',
                                                        right: '0',
                                                        backgroundColor: '#4caf50',
                                                        width: '12px',
                                                        height: '12px',
                                                        borderRadius: '50%',
                                                        border: '2px solid white'
                                                    }}></div>
                                                )}
                                            </div>
                                        ))}
                                    </div>

                                    <button
                                        onClick={() => handleJoinBattle(room.id)}
                                        disabled={room.currentParticipants >= room.maxParticipants || myActiveRoom !== null}
                                        style={{
                                            padding: '0.75rem',
                                            backgroundColor: room.currentParticipants >= room.maxParticipants || myActiveRoom !== null
                                                ? '#9e9e9e'
                                                : '#1976d2',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '8px',
                                            fontSize: '1rem',
                                            fontWeight: 'bold',
                                            cursor: room.currentParticipants >= room.maxParticipants || myActiveRoom !== null
                                                ? 'not-allowed'
                                                : 'pointer',
                                            marginTop: '1rem'
                                        }}
                                    >
                                        {room.currentParticipants >= room.maxParticipants
                                            ? '정원 초과'
                                            : myActiveRoom !== null
                                                ? '이미 참가 중인 대결이 있습니다'
                                                : '참가하기'}
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* 대결 방법 가이드 */}
            <div style={{
                backgroundColor: '#f5f5f5',
                padding: '1.5rem',
                borderRadius: '8px',
                marginTop: '2rem'
            }}>
                <h3 style={{ marginTop: 0 }}>대결 방법</h3>
                <ol style={{ paddingLeft: '1.5rem', margin: '0' }}>
                    <li style={{ marginBottom: '0.5rem' }}>
                        <strong>대결 참가:</strong> 위 목록에서 참가하려는 대결을 선택하거나 새로운 대결을 만듭니다.
                    </li>
                    <li style={{ marginBottom: '0.5rem' }}>
                        <strong>준비 완료:</strong> 대결방에 입장하면 '준비 완료' 버튼을 클릭하여 준비 상태로 변경합니다.
                    </li>
                    <li style={{ marginBottom: '0.5rem' }}>
                        <strong>대결 시작:</strong> 모든 참가자가 준비 완료되면 대결이 자동으로 시작됩니다.
                    </li>
                    <li>
                        <strong>정답 제출:</strong> 문제를 풀어 빠르고 정확하게 답변을 제출하세요. 정답률과 응답 시간에 따라 점수가 부여됩니다.
                    </li>
                </ol>
            </div>

            {/* 배틀룸 생성 모달 */}
            {createModalOpen && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        borderRadius: '8px',
                        padding: '2rem',
                        width: '90%',
                        maxWidth: '600px',
                        maxHeight: '90vh',
                        overflow: 'auto',
                        position: 'relative'
                    }}>
                        <button
                            onClick={toggleCreateModal}
                            style={{
                                position: 'absolute',
                                top: '1rem',
                                right: '1rem',
                                backgroundColor: 'transparent',
                                border: 'none',
                                fontSize: '1.5rem',
                                cursor: 'pointer',
                                width: '30px',
                                height: '30px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                borderRadius: '50%'
                            }}
                        >
                            ×
                        </button>

                        <h2 style={{ marginTop: 0 }}>새 대결 만들기</h2>

                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{
                                display: 'block',
                                marginBottom: '0.5rem',
                                fontWeight: 'bold'
                            }}>
                                퀴즈 선택
                            </label>
                            <select
                                value={selectedQuizId || ''}
                                onChange={(e) => setSelectedQuizId(parseInt(e.target.value))}
                                style={{
                                    width: '100%',
                                    padding: '0.75rem',
                                    borderRadius: '4px',
                                    border: '1px solid #ccc',
                                    fontSize: '1rem'
                                }}
                            >
                                <option value="">퀴즈를 선택하세요</option>
                                {quizzes.map((quiz) => (
                                    <option key={quiz.id} value={quiz.id}>
                                        {quiz.title} - {getDifficultyLabel(quiz.difficultyLevel)}
                                        ({quiz.questionCount}문제)
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{
                                display: 'block',
                                marginBottom: '0.5rem',
                                fontWeight: 'bold'
                            }}>
                                최대 참가자 수
                            </label>
                            <div style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '1rem'
                            }}>
                                <input
                                    type="range"
                                    min="2"
                                    max="8"
                                    value={maxParticipants}
                                    onChange={(e) => setMaxParticipants(parseInt(e.target.value))}
                                    style={{ flex: 1 }}
                                />
                                <span style={{
                                    minWidth: '30px',
                                    fontWeight: 'bold',
                                    fontSize: '1.2rem'
                                }}>
                                    {maxParticipants}명
                                </span>
                            </div>
                        </div>

                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            marginTop: '2rem'
                        }}>
                            <button
                                onClick={handleCreateBattle}
                                disabled={!selectedQuizId || createLoading}
                                style={{
                                    padding: '0.75rem 2rem',
                                    backgroundColor: !selectedQuizId ? '#9e9e9e' : '#4caf50',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '1rem',
                                    fontWeight: 'bold',
                                    cursor: !selectedQuizId || createLoading ? 'not-allowed' : 'pointer',
                                    minWidth: '200px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    gap: '0.5rem'
                                }}
                            >
                                {createLoading ? (
                                    <>
                                        <span style={{
                                            width: '20px',
                                            height: '20px',
                                            border: '3px solid rgba(255, 255, 255, 0.3)',
                                            borderTop: '3px solid white',
                                            borderRadius: '50%',
                                            animation: 'spin 1s linear infinite'
                                        }}></span>
                                        <span>생성 중...</span>
                                    </>
                                ) : (
                                    <>
                                        <span>대결 만들기</span>
                                        <span style={{ fontSize: '1.2rem' }}>🎮</span>
                                    </>
                                )}
                            </button>
                        </div>

                        {/* 선택한 퀴즈 정보 표시 */}
                        {selectedQuizId && quizzes.length > 0 && (
                            <div style={{
                                marginTop: '2rem',
                                padding: '1rem',
                                backgroundColor: '#f5f5f5',
                                borderRadius: '8px'
                            }}>
                                <h3 style={{ marginTop: 0, fontSize: '1.1rem' }}>선택한 퀴즈 정보</h3>
                                {(() => {
                                    const selectedQuiz = quizzes.find(q => q.id === selectedQuizId);
                                    if (!selectedQuiz) return null;

                                    return (
                                        <div>
                                            <div style={{ marginBottom: '0.5rem' }}>
                                                <strong>제목:</strong> {selectedQuiz.title}
                                            </div>
                                            <div style={{ marginBottom: '0.5rem' }}>
                                                <strong>난이도:</strong>{' '}
                                                <span style={{
                                                    display: 'inline-block',
                                                    padding: '0.2rem 0.5rem',
                                                    borderRadius: '4px',
                                                    backgroundColor: getDifficultyColor(selectedQuiz.difficultyLevel),
                                                    color: 'white',
                                                    fontSize: '0.8rem',
                                                    fontWeight: 'bold'
                                                }}>
                                                    {getDifficultyLabel(selectedQuiz.difficultyLevel)}
                                                </span>
                                            </div>
                                            <div style={{ marginBottom: '0.5rem' }}>
                                                <strong>문제 수:</strong> {selectedQuiz.questionCount}문제
                                            </div>
                                            <div style={{ marginBottom: '0.5rem' }}>
                                                <strong>태그:</strong>{' '}
                                                {selectedQuiz.tags.length > 0 ? (
                                                    <div style={{
                                                        display: 'flex',
                                                        flexWrap: 'wrap',
                                                        gap: '0.25rem',
                                                        marginTop: '0.25rem'
                                                    }}>
                                                        {selectedQuiz.tags.map(tag => (
                                                            <span key={tag.id} style={{
                                                                backgroundColor: '#e3f2fd',
                                                                color: '#1976d2',
                                                                padding: '0.2rem 0.5rem',
                                                                borderRadius: '4px',
                                                                fontSize: '0.8rem'
                                                            }}>
                                                                {tag.name}
                                                            </span>
                                                        ))}
                                                    </div>
                                                ) : '없음'}  {/* 수정 부분 1: 삼항 연산자 정렬 */}
                                            </div>
                                        </div>
                                    );
                                })()}  {/* 수정 부분 2: IIFE 정확한 닫힘 */}
                            </div>
                        )}
                    </div>
                </div>
            )}
</div>
    )};

            export default BattleListPage;