// src/pages/QuizResultsPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { quizApi } from '../api/quizApi';
import { QuizResultResponse } from '../types/api';

const QuizResultsPage: React.FC = () => {
    const { quizId, attemptId } = useParams<{ quizId: string; attemptId: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const { isAuthenticated } = useAuthStore();

    const [result, setResult] = useState<QuizResultResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: location.pathname } });
            return;
        }

        const fetchResult = async () => {
            try {
                setLoading(true);

                if (!quizId || !attemptId) {
                    setError('퀴즈 또는 시도 ID가 올바르지 않습니다.');
                    setLoading(false);
                    return;
                }

                const response = await quizApi.getQuizResult(parseInt(quizId), parseInt(attemptId));

                if (response.data.success) {
                    setResult(response.data.data);
                } else {
                    setError('결과를 불러오는 데 실패했습니다: ' + response.data.message);
                }
            } catch (err: any) {
                console.error('결과 로딩 중 오류:', err);
                setError('결과를 불러오는 중 오류가 발생했습니다: ' + (err.response?.data?.message || err.message));
            } finally {
                setLoading(false);
            }
        };

        fetchResult();
    }, [quizId, attemptId, isAuthenticated, navigate, location.pathname]);

    // 점수에 따른 피드백 메시지
    const getScoreFeedback = (score: number) => {
        if (score >= 90) return '훌륭합니다! 완벽에 가까운 점수입니다.';
        if (score >= 75) return '잘 했습니다! 대부분의 문제를 맞혔습니다.';
        if (score >= 60) return '괜찮습니다. 더 노력하면 더 좋은 결과를 얻을 수 있습니다.';
        return '아쉽습니다. 다시 공부하고 도전해보세요!';
    };

    // 소요 시간 포맷팅
    const formatTimeTaken = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}분 ${remainingSeconds}초`;
    };

    // 날짜 포맷팅
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString();
    };

    // 로딩 상태 UI
    if (loading) {
        return (
            <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>결과를 불러오는 중...</p>
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
        );
    }

    // 결과 UI
    return (
        <div className="quiz-results-container">
            {result && (
                <>
                    {/* 결과 헤더 */}
                    <div className="results-header" style={{
                        backgroundColor: '#f5f5f5',
                        padding: '2rem',
                        borderRadius: '8px',
                        marginBottom: '2rem',
                        textAlign: 'center'
                    }}>
                        <h1 style={{ marginTop: 0, marginBottom: '1rem' }}>퀴즈 결과</h1>
                        <h2 style={{ margin: '0 0 1.5rem', color: '#1976d2' }}>{result.title}</h2>

                        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem' }}>
                            <div style={{
                                width: '150px',
                                height: '150px',
                                borderRadius: '50%',
                                border: '10px solid #1976d2',
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                backgroundColor: 'white'
                            }}>
                                <span style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#1976d2' }}>
                                    {result.score}%
                                </span>
                                <span style={{ fontSize: '0.9rem', color: '#666' }}>
                                    {result.correctAnswers}/{result.totalQuestions} 정답
                                </span>
                            </div>
                        </div>

                        <p style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>
                            {getScoreFeedback(result.score)}
                        </p>

                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            gap: '2rem',
                            flexWrap: 'wrap',
                            margin: '1.5rem 0'
                        }}>
                            <div style={{ textAlign: 'center' }}>
                                <p style={{ margin: '0 0 0.25rem', color: '#666' }}>소요 시간</p>
                                <p style={{ margin: 0, fontWeight: 'bold' }}>{formatTimeTaken(result.timeTaken)}</p>
                            </div>

                            <div style={{ textAlign: 'center' }}>
                                <p style={{ margin: '0 0 0.25rem', color: '#666' }}>얻은 경험치</p>
                                <p style={{ margin: 0, fontWeight: 'bold' }}>+{result.experienceGained} EXP</p>
                            </div>

                            <div style={{ textAlign: 'center' }}>
                                <p style={{ margin: '0 0 0.25rem', color: '#666' }}>완료 일시</p>
                                <p style={{ margin: 0, fontWeight: 'bold' }}>
                                    {formatDate(result.completedAt)}
                                </p>
                            </div>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '1.5rem' }}>
                            <button
                                onClick={() => navigate(`/quizzes/${quizId}`)}
                                style={{
                                    padding: '0.5rem 1rem',
                                    borderRadius: '4px',
                                    border: '1px solid #1976d2',
                                    backgroundColor: 'white',
                                    color: '#1976d2',
                                    cursor: 'pointer'
                                }}
                            >
                                퀴즈 상세 보기
                            </button>

                            <button
                                onClick={() => navigate('/quizzes')}
                                style={{
                                    padding: '0.5rem 1rem',
                                    borderRadius: '4px',
                                    border: 'none',
                                    backgroundColor: '#1976d2',
                                    color: 'white',
                                    cursor: 'pointer'
                                }}
                            >
                                다른 퀴즈 풀기
                            </button>
                        </div>
                    </div>

                    {/* 문제별 결과 */}
                    <h2 style={{ marginBottom: '1.5rem' }}>상세 결과</h2>

                    <div className="questions-results">
                        {result.questions.map((question, index) => (
                            <div
                                key={question.id}
                                className="question-result"
                                style={{
                                    marginBottom: '1.5rem',
                                    padding: '1.5rem',
                                    borderRadius: '8px',
                                    border: '1px solid #e0e0e0',
                                    backgroundColor: question.isCorrect ? '#e8f5e9' : '#ffebee'
                                }}
                            >
                                <h3 style={{ marginTop: 0, display: 'flex', justifyContent: 'space-between' }}>
                                    <span>문제 {index + 1}</span>
                                    <span style={{
                                        backgroundColor: question.isCorrect ? '#4caf50' : '#f44336',
                                        color: 'white',
                                        padding: '0.25rem 0.5rem',
                                        borderRadius: '4px',
                                        fontSize: '0.8rem'
                                    }}>
                                        {question.isCorrect ? '정답' : '오답'}
                                    </span>
                                </h3>

                                <p style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>{question.questionText}</p>

                                <div style={{ marginBottom: '1rem' }}>
                                    <p style={{ margin: '0 0 0.5rem', fontWeight: 'bold', color: '#666' }}>제출한 답변:</p>
                                    <div style={{
                                        padding: '0.75rem',
                                        backgroundColor: 'white',
                                        borderRadius: '4px',
                                        border: '1px solid #e0e0e0'
                                    }}>
                                        {question.yourAnswer || '(답변 없음)'}
                                    </div>
                                </div>

                                <div style={{ marginBottom: '1rem' }}>
                                    <p style={{ margin: '0 0 0.5rem', fontWeight: 'bold', color: '#666' }}>정답:</p>
                                    <div style={{
                                        padding: '0.75rem',
                                        backgroundColor: 'white',
                                        borderRadius: '4px',
                                        border: '1px solid #4caf50',
                                        color: '#4caf50',
                                        fontWeight: 'bold'
                                    }}>
                                        {question.correctAnswer}
                                    </div>
                                </div>

                                <div>
                                    <p style={{ margin: '0 0 0.5rem', fontWeight: 'bold', color: '#666' }}>설명:</p>
                                    <div style={{
                                        padding: '0.75rem',
                                        backgroundColor: '#f5f5f5',
                                        borderRadius: '4px'
                                    }}>
                                        {question.explanation}
                                    </div>
                                </div>

                                <div style={{
                                    marginTop: '1rem',
                                    textAlign: 'right',
                                    fontWeight: 'bold',
                                    color: question.isCorrect ? '#4caf50' : '#f44336'
                                }}>
                                    {question.isCorrect ? `+${question.points}점` : `0/${question.points}점`}
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default QuizResultsPage;