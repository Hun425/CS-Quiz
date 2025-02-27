// src/pages/HomePage.tsx
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { quizApi } from '../api/quizApi';
import { QuizSummaryResponse, QuizResponse } from '../types/api';
import QuizCard from '../components/quiz/QuizCard';
import { useAuthStore } from '../store/authStore';

const HomePage: React.FC = () => {
    const [recommendedQuizzes, setRecommendedQuizzes] = useState<QuizSummaryResponse[]>([]);
    const [dailyQuiz, setDailyQuiz] = useState<QuizResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // 인증 상태 가져오기
    const { isAuthenticated, user } = useAuthStore();

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // 추천 퀴즈 가져오기
                const recommendedRes = await quizApi.getRecommendedQuizzes();
                if (recommendedRes.data.success) {
                    setRecommendedQuizzes(recommendedRes.data.data);
                }

                // 데일리 퀴즈 가져오기
                const dailyRes = await quizApi.getDailyQuiz();
                if (dailyRes.data.success) {
                    setDailyQuiz(dailyRes.data.data);
                }
            } catch (err: any) {
                console.error('데이터 로딩 중 오류:', err);
                setError('데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

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
                        <Link to="/quizzes" style={{
                            backgroundColor: 'white',
                            color: '#1976d2',
                            padding: '0.5rem 1rem',
                            borderRadius: '4px',
                            textDecoration: 'none',
                            fontWeight: 'bold',
                            display: 'inline-block',
                            marginTop: '0.5rem'
                        }}>
                            퀴즈 탐색하기
                        </Link>
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
                    {dailyQuiz.difficultyLevel}
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
                                <p>오늘의 퀴즈가 없습니다.</p>
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
                                <p>추천 퀴즈가 없습니다.</p>
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
                                <h3 style={{ color: '#1976d2' }}>실시간 대결</h3>
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