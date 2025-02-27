// src/pages/QuizDetailPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { quizApi } from '../api/quizApi';
import { QuizDetailResponse } from '../types/api'; // QuizResponse가 아닌 QuizDetailResponse 임포트
import { useAuthStore } from '../store/authStore';

const QuizDetailPage: React.FC = () => {
    const { quizId } = useParams<{ quizId: string }>();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuthStore();

    // 타입을 QuizResponse에서 QuizDetailResponse로 변경
    const [quiz, setQuiz] = useState<QuizDetailResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchQuizDetail = async () => {
            if (!quizId) return;

            try {
                setLoading(true);
                const response = await quizApi.getQuiz(parseInt(quizId));

                if (response.data.success) {
                    // 응답 데이터 타입이 QuizDetailResponse임
                    setQuiz(response.data.data);
                } else {
                    setError('퀴즈를 불러오는 데 실패했습니다.');
                }
            } catch (err) {
                console.error('퀴즈 상세 로딩 중 오류:', err);
                setError('퀴즈 상세 정보를 불러오는 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchQuizDetail();
    }, [quizId]);

    const handleStartQuiz = () => {
        if (!isAuthenticated) {
            // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
            navigate('/login', { state: { from: `/quizzes/${quizId}` } });
            return;
        }

        // 로그인된 경우 퀴즈 풀기 페이지로 이동
        navigate(`/quizzes/${quizId}/play`);
    };

    // 난이도에 따른 색상
    const getDifficultyColor = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '#4caf50';  // 초록색
            case 'INTERMEDIATE': return '#ff9800';  // 주황색
            case 'ADVANCED': return '#f44336';  // 빨간색
            default: return '#9e9e9e';  // 회색
        }
    };

    // 난이도 한글 표시
    const getDifficultyLabel = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '입문';
            case 'INTERMEDIATE': return '중급';
            case 'ADVANCED': return '고급';
            default: return '알 수 없음';
        }
    };

    // 퀴즈 타입 한글 표시
    const getQuizTypeLabel = (type: string) => {
        switch (type) {
            case 'DAILY': return '데일리 퀴즈';
            case 'TAG_BASED': return '태그 기반';
            case 'TOPIC_BASED': return '주제 기반';
            case 'CUSTOM': return '커스텀';
            default: return '알 수 없음';
        }
    };

    return (
        <div className="quiz-detail-page">
            {loading ? (
                <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                    <p>퀴즈 정보를 불러오는 중...</p>
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
                    <button onClick={() => navigate('/quizzes')} style={{
                        backgroundColor: '#1976d2',
                        color: 'white',
                        border: 'none',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '0.5rem'
                    }}>
                        퀴즈 목록으로 돌아가기
                    </button>
                </div>
            ) : quiz ? (
                <div>
                    {/* 퀴즈 헤더 */}
                    <div className="quiz-header" style={{
                        backgroundColor: '#f5f5f5',
                        padding: '2rem',
                        borderRadius: '8px',
                        marginBottom: '1.5rem'
                    }}>
                        <h1 style={{ marginTop: 0, marginBottom: '1rem' }}>{quiz.title}</h1>
                        <p style={{ marginBottom: '1.5rem' }}>{quiz.description}</p>

                        <div style={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            gap: '1rem',
                            marginBottom: '1.5rem'
                        }}>
                            <div style={{
                                backgroundColor: getDifficultyColor(quiz.difficultyLevel),
                                color: 'white',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.5rem',
                                fontSize: '0.9rem'
                            }}>
                                <span>난이도: {getDifficultyLabel(quiz.difficultyLevel)}</span>
                            </div>

                            <div style={{
                                backgroundColor: '#e0e0e0',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.5rem',
                                fontSize: '0.9rem'
                            }}>
                                <span>유형: {getQuizTypeLabel(quiz.quizType)}</span>
                            </div>

                            <div style={{
                                backgroundColor: '#e0e0e0',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.5rem',
                                fontSize: '0.9rem'
                            }}>
                                <span>문제 수: {quiz.questionCount}문제</span>
                            </div>

                            <div style={{
                                backgroundColor: '#e0e0e0',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '0.5rem',
                                fontSize: '0.9rem'
                            }}>
                                <span>제한 시간: {quiz.timeLimit}분</span>
                            </div>
                        </div>

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.5rem' }}>
                            {quiz.tags.map((tag) => (
                                <span key={tag.id} style={{
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    padding: '0.3rem 0.6rem',
                                    borderRadius: '4px',
                                    fontSize: '0.8rem'
                                }}>
                  {tag.name}
                </span>
                            ))}
                        </div>

                        <button
                            onClick={handleStartQuiz}
                            style={{
                                backgroundColor: '#1976d2',
                                color: 'white',
                                padding: '0.75rem 1.5rem',
                                borderRadius: '4px',
                                border: 'none',
                                fontSize: '1rem',
                                fontWeight: 'bold',
                                cursor: 'pointer'
                            }}
                        >
                            퀴즈 시작하기
                        </button>
                    </div>

                    {/* 퀴즈 통계 */}
                    {quiz.statistics && (
                        <div className="quiz-statistics" style={{
                            backgroundColor: 'white',
                            padding: '1.5rem',
                            borderRadius: '8px',
                            border: '1px solid #e0e0e0',
                            marginBottom: '1.5rem'
                        }}>
                            <h2 style={{ marginTop: 0, fontSize: '1.3rem' }}>퀴즈 통계</h2>

                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                                gap: '1rem',
                                marginTop: '1rem'
                            }}>
                                <div className="stat-item">
                                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>시도 횟수</h3>
                                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                        {quiz.statistics.totalAttempts}회
                                    </p>
                                </div>

                                <div className="stat-item">
                                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>평균 점수</h3>
                                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                        {quiz.statistics.averageScore?.toFixed(1) || '0'}점
                                    </p>
                                </div>

                                <div className="stat-item">
                                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>완료율</h3>
                                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                        {quiz.statistics.completionRate?.toFixed(1) || '0'}%
                                    </p>
                                </div>

                                <div className="stat-item">
                                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>평균 소요 시간</h3>
                                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                        {Math.floor(quiz.statistics.averageTimeSeconds / 60)}분 {quiz.statistics.averageTimeSeconds % 60}초
                                    </p>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 퀴즈 생성자 정보 */}
                    <div className="quiz-creator" style={{
                        backgroundColor: 'white',
                        padding: '1.5rem',
                        borderRadius: '8px',
                        border: '1px solid #e0e0e0'
                    }}>
                        <h2 style={{ marginTop: 0, fontSize: '1.3rem' }}>생성자 정보</h2>

                        <div style={{ display: 'flex', alignItems: 'center', marginTop: '1rem' }}>
                            {quiz.creator.profileImage ? (
                                <img
                                    src={quiz.creator.profileImage}
                                    alt={quiz.creator.username}
                                    style={{
                                        width: '50px',
                                        height: '50px',
                                        borderRadius: '50%',
                                        objectFit: 'cover',
                                        marginRight: '1rem'
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
                                    marginRight: '1rem',
                                    fontSize: '1.2rem',
                                    fontWeight: 'bold',
                                    color: '#666'
                                }}>
                                    {quiz.creator.username.charAt(0).toUpperCase()}
                                </div>
                            )}

                            <div>
                                <p style={{ margin: '0 0 0.25rem', fontWeight: 'bold' }}>{quiz.creator.username}</p>
                                <p style={{ margin: 0, fontSize: '0.9rem', color: '#666' }}>
                                    레벨 {quiz.creator.level} • 가입일: {new Date(quiz.creator.joinedAt).toLocaleDateString()}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            ) : null}
        </div>
    );
};

export default QuizDetailPage;