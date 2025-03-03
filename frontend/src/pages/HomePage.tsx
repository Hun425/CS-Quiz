// src/pages/HomePage.tsx
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { quizApi } from '../api/quizApi';
import { battleApi } from '../api/battleApi';
import { QuizSummaryResponse, QuizResponse, BattleRoomResponse } from '../types/api';
import QuizCard from '../components/quiz/QuizCard';
import { useAuthStore } from '../store/authStore';

const HomePage: React.FC = () => {
    const [recommendedQuizzes, setRecommendedQuizzes] = useState<QuizSummaryResponse[]>([]);
    const [dailyQuiz, setDailyQuiz] = useState<QuizResponse | null>(null);
    const [activeBattles, setActiveBattles] = useState<BattleRoomResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // 인증 상태 가져오기
    const { isAuthenticated, user } = useAuthStore();

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // 여러 API 요청을 병렬로 처리
                const promises = [
                    isAuthenticated ? quizApi.getRecommendedQuizzes() : Promise.resolve({ data: { success: true, data: [] } }),
                    quizApi.getDailyQuiz().catch(() => ({ data: { success: false, data: null } })),
                    isAuthenticated ? battleApi.getActiveBattleRooms().catch(() => ({ data: { success: true, data: [] } })) : Promise.resolve({ data: { success: true, data: [] } })
                ];

                const [recommendedRes, dailyRes, battlesRes] = await Promise.all(promises);

                // 추천 퀴즈 설정 - 타입 가드 적용
                if (recommendedRes.data.success && Array.isArray(recommendedRes.data.data)) {
                    setRecommendedQuizzes(recommendedRes.data.data as QuizSummaryResponse[]);
                } else {
                    setRecommendedQuizzes([]); // 빈 배열로 초기화
                }

                // 데일리 퀴즈 설정 - 타입 가드 적용
                if (dailyRes.data.success && dailyRes.data.data) {
                    setDailyQuiz(dailyRes.data.data as QuizResponse);
                } else {
                    setDailyQuiz(null); // null로 초기화
                }

                // 활성 배틀룸 설정 - 타입 가드 적용
                if (battlesRes.data.success && Array.isArray(battlesRes.data.data)) {
                    setActiveBattles(battlesRes.data.data as BattleRoomResponse[]);
                } else {
                    setActiveBattles([]); // 빈 배열로 초기화
                }
            } catch (err: any) {
                console.error('데이터 로딩 중 오류:', err);
                setError('데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [isAuthenticated]);

    // 난이도 표시 함수
    const getDifficultyLabel = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '입문';
            case 'INTERMEDIATE': return '중급';
            case 'ADVANCED': return '고급';
            default: return '알 수 없음';
        }
    };

    // 배틀룸 상태 표시 함수
    const getBattleStatusLabel = (status: string) => {
        switch (status) {
            case 'WAITING': return '대기중';
            case 'IN_PROGRESS': return '진행중';
            case 'FINISHED': return '종료';
            default: return '알 수 없음';
        }
    };

    return (
        <div className="home-page">
            {/* 헤더 배너 섹션 */}
            <section className="hero-section" style={{
                backgroundColor: '#1976d2',
                color: 'white',
                padding: '2rem 1rem',
                borderRadius: '8px',
                marginBottom: '2rem',
                textAlign: 'center'
            }}>
                <h1 style={{ fontSize: '2rem', marginBottom: '1rem' }}>CS 퀴즈 플랫폼</h1>
                <p style={{ fontSize: '1.2rem', marginBottom: '1.5rem' }}>
                    컴퓨터 과학 지식을 테스트하고 향상시켜 보세요!
                </p>

                {isAuthenticated ? (
                    <div>
                        <p>안녕하세요, {user?.username}님! 오늘도 퀴즈를 풀어볼까요?</p>
                        <div style={{
                            display: 'flex',
                            gap: '1rem',
                            justifyContent: 'center',
                            marginTop: '1rem'
                        }}>
                            <Link to="/quizzes" style={{
                                backgroundColor: 'white',
                                color: '#1976d2',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                textDecoration: 'none',
                                fontWeight: 'bold',
                                display: 'inline-block'
                            }}>
                                퀴즈 탐색하기
                            </Link>
                            <Link to="/battles" style={{
                                backgroundColor: '#f44336',
                                color: 'white',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                textDecoration: 'none',
                                fontWeight: 'bold',
                                display: 'inline-block'
                            }}>
                                퀴즈 대결 참여하기
                            </Link>
                        </div>
                    </div>
                ) : (
                    <Link to="/login" style={{
                        backgroundColor: 'white',
                        color: '#1976d2',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        textDecoration: 'none',
                        fontWeight: 'bold',
                        display: 'inline-block'
                    }}>
                        로그인하여 시작하기
                    </Link>
                )}
            </section>

            {/* 로딩 및 에러 상태 처리 */}
            {loading ? (
                <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                    <p>데이터를 불러오는 중...</p>
                </div>
            ) : error ? (
                <div className="error-container" style={{
                    backgroundColor: '#ffebee',
                    color: '#d32f2f',
                    padding: '1rem',
                    borderRadius: '4px',
                    marginBottom: '1rem'
                }}>
                    <p>{error}</p>
                    <button onClick={() => window.location.reload()} style={{
                        backgroundColor: '#d32f2f',
                        color: 'white',
                        border: 'none',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '0.5rem'
                    }}>
                        다시 시도
                    </button>
                </div>
            ) : (
                <>
                    {/* 퀴즈 대결 섹션 */}
                    {isAuthenticated && (
                        <section className="battle-section" style={{ marginBottom: '2rem' }}>
                            <h2 style={{
                                borderBottom: '2px solid #f44336',
                                paddingBottom: '0.5rem',
                                marginBottom: '1rem',
                                color: '#f44336'
                            }}>
                                실시간 퀴즈 대결
                            </h2>

                            {activeBattles.length > 0 ? (
                                <div>
                                    <p style={{ marginBottom: '1rem' }}>현재 활성화된 대결방에 참여하거나 새로운 대결방을 만들어보세요!</p>

                                    <div className="battle-rooms" style={{
                                        display: 'grid',
                                        gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                                        gap: '1rem',
                                        marginBottom: '1.5rem'
                                    }}>
                                        {activeBattles.map(battle => (
                                            <div
                                                key={battle.id}
                                                className="battle-room-card"
                                                style={{
                                                    border: '1px solid #e0e0e0',
                                                    borderRadius: '8px',
                                                    padding: '1rem',
                                                    backgroundColor: battle.status === 'WAITING' ? '#fff' : '#f5f5f5',
                                                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                                                    position: 'relative',
                                                    overflow: 'hidden'
                                                }}
                                            >
                                                {battle.status === 'WAITING' && (
                                                    <div style={{
                                                        position: 'absolute',
                                                        top: '10px',
                                                        right: '10px',
                                                        backgroundColor: '#4CAF50',
                                                        color: 'white',
                                                        padding: '0.25rem 0.5rem',
                                                        borderRadius: '20px',
                                                        fontSize: '0.8rem',
                                                        fontWeight: 'bold'
                                                    }}>
                                                        참여 가능
                                                    </div>
                                                )}

                                                <h3 style={{ marginTop: 0, marginBottom: '0.5rem', fontSize: '1.1rem' }}>
                                                    {battle.quizTitle}
                                                </h3>

                                                <div style={{
                                                    display: 'flex',
                                                    justifyContent: 'space-between',
                                                    marginBottom: '0.5rem',
                                                    fontSize: '0.9rem',
                                                    color: '#666'
                                                }}>
                                                    <span>방 코드: {battle.roomCode}</span>
                                                    <span>
                                                        {battle.currentParticipants}/{battle.maxParticipants} 참가자
                                                    </span>
                                                </div>

                                                <div style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    marginBottom: '1rem'
                                                }}>
                                                    <span style={{
                                                        display: 'inline-block',
                                                        width: '10px',
                                                        height: '10px',
                                                        borderRadius: '50%',
                                                        backgroundColor: battle.status === 'WAITING' ? '#4CAF50' : battle.status === 'IN_PROGRESS' ? '#FFC107' : '#9E9E9E',
                                                        marginRight: '0.5rem'
                                                    }}></span>
                                                    <span style={{ fontSize: '0.9rem' }}>
                                                        {getBattleStatusLabel(battle.status)}
                                                    </span>
                                                </div>

                                                <div style={{
                                                    display: 'flex',
                                                    flexWrap: 'wrap',
                                                    gap: '0.5rem',
                                                    marginBottom: '1rem',
                                                    minHeight: '30px'
                                                }}>
                                                    {battle.participants.slice(0, 3).map(participant => (
                                                        <div key={participant.id} style={{
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            backgroundColor: '#f5f5f5',
                                                            padding: '0.25rem 0.5rem',
                                                            borderRadius: '4px',
                                                            fontSize: '0.8rem'
                                                        }}>
                                                            {participant.profileImage ? (
                                                                <img
                                                                    src={participant.profileImage}
                                                                    alt={participant.username}
                                                                    style={{
                                                                        width: '20px',
                                                                        height: '20px',
                                                                        borderRadius: '50%',
                                                                        marginRight: '0.25rem'
                                                                    }}
                                                                />
                                                            ) : (
                                                                <div style={{
                                                                    width: '20px',
                                                                    height: '20px',
                                                                    borderRadius: '50%',
                                                                    backgroundColor: '#e0e0e0',
                                                                    display: 'flex',
                                                                    alignItems: 'center',
                                                                    justifyContent: 'center',
                                                                    fontSize: '0.7rem',
                                                                    marginRight: '0.25rem'
                                                                }}>
                                                                    {participant.username.charAt(0).toUpperCase()}
                                                                </div>
                                                            )}
                                                            <span>{participant.username}</span>
                                                            {participant.ready && (
                                                                <span style={{
                                                                    marginLeft: '0.25rem',
                                                                    color: '#4CAF50',
                                                                    fontSize: '0.7rem'
                                                                }}>✓</span>
                                                            )}
                                                        </div>
                                                    ))}
                                                    {battle.participants.length > 3 && (
                                                        <div style={{
                                                            backgroundColor: '#f5f5f5',
                                                            padding: '0.25rem 0.5rem',
                                                            borderRadius: '4px',
                                                            fontSize: '0.8rem'
                                                        }}>
                                                            +{battle.participants.length - 3}명
                                                        </div>
                                                    )}
                                                </div>

                                                {battle.status === 'WAITING' && (
                                                    <Link
                                                        to={`/battles/${battle.id}`}
                                                        style={{
                                                            display: 'block',
                                                            width: '100%',
                                                            padding: '0.5rem',
                                                            backgroundColor: '#f44336',
                                                            color: 'white',
                                                            border: 'none',
                                                            borderRadius: '4px',
                                                            textAlign: 'center',
                                                            textDecoration: 'none',
                                                            fontWeight: 'bold',
                                                            cursor: 'pointer'
                                                        }}
                                                    >
                                                        참여하기
                                                    </Link>
                                                )}

                                                {battle.status === 'IN_PROGRESS' && (
                                                    <Link
                                                        to={`/battles/${battle.id}`}
                                                        style={{
                                                            display: 'block',
                                                            width: '100%',
                                                            padding: '0.5rem',
                                                            backgroundColor: '#FFC107',
                                                            color: 'white',
                                                            border: 'none',
                                                            borderRadius: '4px',
                                                            textAlign: 'center',
                                                            textDecoration: 'none',
                                                            fontWeight: 'bold',
                                                            cursor: 'pointer'
                                                        }}
                                                    >
                                                        관전하기
                                                    </Link>
                                                )}
                                            </div>
                                        ))}
                                    </div>

                                    <div style={{ textAlign: 'center', marginTop: '1rem' }}>
                                        <Link to="/battles/create" style={{
                                            display: 'inline-block',
                                            padding: '0.75rem 1.5rem',
                                            backgroundColor: '#f44336',
                                            color: 'white',
                                            borderRadius: '4px',
                                            textDecoration: 'none',
                                            fontWeight: 'bold'
                                        }}>
                                            새 대결방 만들기
                                        </Link>
                                    </div>
                                </div>
                            ) : (
                                <div style={{
                                    textAlign: 'center',
                                    padding: '2rem',
                                    backgroundColor: '#f5f5f5',
                                    borderRadius: '8px',
                                    marginBottom: '1rem'
                                }}>
                                    <p style={{ marginBottom: '1rem' }}>현재 활성화된 대결방이 없습니다. 새로운 대결을 시작해보세요!</p>
                                    <Link to="/battles/create" style={{
                                        display: 'inline-block',
                                        padding: '0.75rem 1.5rem',
                                        backgroundColor: '#f44336',
                                        color: 'white',
                                        borderRadius: '4px',
                                        textDecoration: 'none',
                                        fontWeight: 'bold'
                                    }}>
                                        새 대결방 만들기
                                    </Link>
                                </div>
                            )}
                        </section>
                    )}

                    {/* 데일리 퀴즈 섹션 */}
                    <section className="daily-quiz-section" style={{ marginBottom: '2rem' }}>
                        <h2 style={{
                            borderBottom: '2px solid #1976d2',
                            paddingBottom: '0.5rem',
                            marginBottom: '1rem'
                        }}>
                            오늘의 퀴즈
                        </h2>

                        {dailyQuiz ? (
                            <div className="daily-quiz-card" style={{
                                border: '1px solid #e0e0e0',
                                borderRadius: '8px',
                                padding: '1.5rem',
                                boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                                backgroundColor: '#f5f5f5'
                            }}>
                                <h3 style={{ marginTop: 0, color: '#1976d2' }}>{dailyQuiz.title}</h3>
                                <p style={{ marginBottom: '1rem' }}>{dailyQuiz.description}</p>

                                <div style={{ display: 'flex', marginBottom: '1rem' }}>
                                    <span style={{
                                        backgroundColor: '#1976d2',
                                        color: 'white',
                                        padding: '0.25rem 0.5rem',
                                        borderRadius: '4px',
                                        fontSize: '0.8rem',
                                        marginRight: '0.5rem'
                                    }}>
                                        {getDifficultyLabel(dailyQuiz.difficultyLevel)}
                                    </span>
                                    <span style={{
                                        backgroundColor: '#e0e0e0',
                                        padding: '0.25rem 0.5rem',
                                        borderRadius: '4px',
                                        fontSize: '0.8rem'
                                    }}>
                                        {dailyQuiz.questionCount}문제
                                    </span>
                                </div>

                                <Link to={`/quizzes/${dailyQuiz.id}`} style={{
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    padding: '0.5rem 1rem',
                                    borderRadius: '4px',
                                    textDecoration: 'none',
                                    display: 'inline-block'
                                }}>
                                    지금 풀기
                                </Link>
                            </div>
                        ) : (
                            <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                                <p>오늘의 퀴즈가 아직 준비되지 않았습니다. 잠시 후 다시 확인해주세요.</p>
                            </div>
                        )}
                    </section>

                    {/* 추천 퀴즈 섹션 */}
                    <section className="recommended-quiz-section">
                        <h2 style={{
                            borderBottom: '2px solid #1976d2',
                            paddingBottom: '0.5rem',
                            marginBottom: '1rem'
                        }}>
                            추천 퀴즈
                        </h2>

                        {recommendedQuizzes.length > 0 ? (
                            <div className="quiz-grid" style={{
                                display: 'grid',
                                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                                gap: '1rem'
                            }}>
                                {recommendedQuizzes.map((quiz) => (
                                    <QuizCard key={quiz.id} quiz={quiz} />
                                ))}
                            </div>
                        ) : (
                            <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                                <p>{isAuthenticated ? '추천 퀴즈가 없습니다. 더 많은 퀴즈를 풀어보세요!' : '로그인하면 맞춤형 퀴즈를 추천해드립니다.'}</p>
                            </div>
                        )}

                        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
                            <Link to="/quizzes" style={{
                                backgroundColor: '#1976d2',
                                color: 'white',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                textDecoration: 'none',
                                display: 'inline-block'
                            }}>
                                모든 퀴즈 보기
                            </Link>
                        </div>
                    </section>

                    {/* 플랫폼 소개 섹션 */}
                    <section className="features-section" style={{ marginTop: '3rem' }}>
                        <h2 style={{
                            borderBottom: '2px solid #1976d2',
                            paddingBottom: '0.5rem',
                            marginBottom: '1.5rem'
                        }}>
                            CS 퀴즈 플랫폼 특징
                        </h2>

                        <div className="feature-grid" style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
                            gap: '1.5rem'
                        }}>
                            <div className="feature-card" style={{ padding: '1rem', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                                <h3 style={{ color: '#1976d2' }}>다양한 퀴즈 유형</h3>
                                <p>객관식, 참/거짓, 코드 분석 등 다양한 유형의 퀴즈를 제공합니다.</p>
                            </div>

                            <div className="feature-card" style={{ padding: '1rem', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                                <h3 style={{ color: '#f44336' }}>실시간 대결</h3>
                                <p>다른 사용자와 실시간으로 대결하며 지식을 겨루어 보세요.</p>
                            </div>

                            <div className="feature-card" style={{ padding: '1rem', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                                <h3 style={{ color: '#1976d2' }}>성과 추적</h3>
                                <p>자신의 성과를 추적하고 시간에 따른 향상도를 확인하세요.</p>
                            </div>

                            <div className="feature-card" style={{ padding: '1rem', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                                <h3 style={{ color: '#1976d2' }}>맞춤형 추천</h3>
                                <p>사용자의 수준과 관심사에 따라 맞춤형 퀴즈를 추천해 드립니다.</p>
                            </div>
                        </div>
                    </section>
                </>
            )}
        </div>
    );
};

export default HomePage;