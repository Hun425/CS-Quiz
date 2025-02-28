// src/pages/QuizResultsPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

// 결과 데이터 타입 정의
interface QuizResult {
    quizId: number;
    title: string;
    totalQuestions: number;
    correctAnswers: number;
    score: number;
    totalPossibleScore: number;
    timeTaken: number;
    completedAt: string;
    questions: {
        id: number;
        questionText: string;
        yourAnswer: string;
        correctAnswer: string;
        isCorrect: boolean;
        explanation: string;
        points: number;
    }[];
}

const QuizResultsPage: React.FC = () => {
    const { quizId } = useParams<{ quizId: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const { isAuthenticated } = useAuthStore();

    const [result, setResult] = useState<QuizResult | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // URL 파라미터에서 임시 플래그 가져오기
    const tempResult = new URLSearchParams(location.search).get('temp') === '1';

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        // 실제로는 API 호출을 통해 결과 데이터를 가져옵니다.
        // 여기서는 임시 데이터를 사용합니다.
        const fetchResult = async () => {
            try {
                setLoading(true);

                if (tempResult) {
                    // 임시 결과 데이터
                    setTimeout(() => {
                        const mockResult: QuizResult = {
                            quizId: parseInt(quizId || '0'),
                            title: 'CS 기초 퀴즈',
                            totalQuestions: 5,
                            correctAnswers: 3,
                            score: 75,
                            totalPossibleScore: 100,
                            timeTaken: 360, // 초 단위
                            completedAt: new Date().toISOString(),
                            questions: [
                                {
                                    id: 1,
                                    questionText: '시간 복잡도 O(n log n)을 가진 정렬 알고리즘은?',
                                    yourAnswer: '퀵 정렬',
                                    correctAnswer: '퀵 정렬',
                                    isCorrect: true,
                                    explanation: '퀵 정렬의 평균 시간 복잡도는 O(n log n)입니다.',
                                    points: 20,
                                },
                                {
                                    id: 2,
                                    questionText: 'HTTP 상태 코드 404의 의미는?',
                                    yourAnswer: '서버 내부 오류',
                                    correctAnswer: '찾을 수 없음',
                                    isCorrect: false,
                                    explanation: '404는 요청한 리소스를 찾을 수 없음을 의미합니다. 서버 내부 오류는 500입니다.',
                                    points: 15,
                                },
                                {
                                    id: 3,
                                    questionText: 'JavaScript에서 == 연산자와 === 연산자의 차이점은?',
                                    yourAnswer: '=== 연산자는 타입과 값을 모두 비교한다',
                                    correctAnswer: '=== 연산자는 타입과 값을 모두 비교한다',
                                    isCorrect: true,
                                    explanation: '== 연산자는 값만 비교하고, === 연산자는 값과 타입을 모두 비교합니다.',
                                    points: 15,
                                },
                                {
                                    id: 4,
                                    questionText: 'TCP와 UDP의 주요 차이점은?',
                                    yourAnswer: 'TCP는 연결 지향적이고 신뢰성이 있으며, UDP는 비연결성이고 신뢰성이 낮다',
                                    correctAnswer: 'TCP는 연결 지향적이고 신뢰성이 있으며, UDP는 비연결성이고 신뢰성이 낮다',
                                    isCorrect: true,
                                    explanation: 'TCP는 연결 설정 후 데이터를 주고받고 신뢰성이 높습니다. UDP는 연결 설정 없이 데이터를 전송하고 신뢰성이 낮지만 속도가 빠릅니다.',
                                    points: 25,
                                },
                                {
                                    id: 5,
                                    questionText: '객체 지향 프로그래밍의 4가지 주요 개념은?',
                                    yourAnswer: '추상화, 상속, 캡슐화, 다형성',
                                    correctAnswer: '추상화, 상속, 캡슐화, 다형성',
                                    isCorrect: false,
                                    explanation: '객체 지향 프로그래밍의 4가지 주요 개념은 추상화, 상속, 캡슐화, 다형성입니다.',
                                    points: 25,
                                },
                            ],
                        };

                        setResult(mockResult);
                        setLoading(false);
                    }, 1000);
                } else {
                    // 실제 API 호출 (여기서는 구현하지 않음)
                    setError('API가 아직 구현되지 않았습니다.');
                    setLoading(false);
                }
            } catch (err: any) {
                console.error('결과 로딩 중 오류:', err);
                setError('결과를 불러오는 중 오류가 발생했습니다.');
                setLoading(false);
            }
        };

        fetchResult();
    }, [quizId, isAuthenticated, navigate, tempResult]);

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
            {/* 결과 헤더 */}
            <div className="results-header" style={{
                backgroundColor: '#f5f5f5',
                padding: '2rem',
                borderRadius: '8px',
                marginBottom: '2rem',
                textAlign: 'center'
            }}>
                <h1 style={{ marginTop: 0, marginBottom: '1rem' }}>퀴즈 결과</h1>
                <h2 style={{ margin: '0 0 1.5rem', color: '#1976d2' }}>{result?.title}</h2>

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
              {result?.score}%
            </span>
                        <span style={{ fontSize: '0.9rem', color: '#666' }}>
              {result?.correctAnswers}/{result?.totalQuestions} 정답
            </span>
                    </div>
                </div>

                <p style={{ fontSize: '1.2rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>
                    {result ? getScoreFeedback(result.score) : ''}
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
                        <p style={{ margin: 0, fontWeight: 'bold' }}>{result ? formatTimeTaken(result.timeTaken) : ''}</p>
                    </div>

                    <div style={{ textAlign: 'center' }}>
                        <p style={{ margin: '0 0 0.25rem', color: '#666' }}>총 점수</p>
                        <p style={{ margin: 0, fontWeight: 'bold' }}>{result?.score}점</p>
                    </div>

                    <div style={{ textAlign: 'center' }}>
                        <p style={{ margin: '0 0 0.25rem', color: '#666' }}>완료 일시</p>
                        <p style={{ margin: 0, fontWeight: 'bold' }}>
                            {result ? new Date(result.completedAt).toLocaleString() : ''}
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
                {result?.questions.map((question, index) => (
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
        </div>
    );
};

export default QuizResultsPage;