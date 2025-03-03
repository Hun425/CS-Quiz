// src/pages/QuizPlayPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { quizApi } from '../api/quizApi';
import { QuizResponse, QuestionResponse, QuizSubmitRequest } from '../types/api';
import { useAuthStore } from '../store/authStore';

const QuizPlayPage: React.FC = () => {
    const { quizId } = useParams<{ quizId: string }>();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuthStore();

    const [quiz, setQuiz] = useState<QuizResponse | null>(null);
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState<number>(0);
    const [answers, setAnswers] = useState<Record<number, string>>({});
    const [timeLeft, setTimeLeft] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [quizStarted, setQuizStarted] = useState<boolean>(false);
    const [quizCompleted, setQuizCompleted] = useState<boolean>(false);
    const [quizAttemptId, setQuizAttemptId] = useState<number | null>(null);
    const [startTime, setStartTime] = useState<number | null>(null);
    const [submitting, setSubmitting] = useState<boolean>(false);

    // 인증 확인
    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: `/quizzes/${quizId}/play` } });
        }
    }, [isAuthenticated, navigate, quizId]);

    // 퀴즈 데이터 로드
    useEffect(() => {
        const fetchQuiz = async () => {
            if (!quizId) return;

            try {
                setLoading(true);
                const response = await quizApi.getPlayableQuiz(parseInt(quizId));

                if (response.data.success) {
                    const quizData = response.data.data;
                    setQuiz(quizData);

                    // 새로운 퀴즈 시도 ID 저장
                    if (response.data.data.attemptId) {
                        setQuizAttemptId(response.data.data.attemptId);
                    }

                    // 전체 퀴즈 시간을 초 단위로 설정
                    setTimeLeft(quizData.timeLimit * 60);
                } else {
                    setError('퀴즈를 불러오는 데 실패했습니다: ' + response.data.message);
                }
            } catch (err: any) {
                console.error('퀴즈 로딩 중 오류:', err);
                setError('퀴즈를 불러오는 중 오류가 발생했습니다: ' + (err.response?.data?.message || err.message));
            } finally {
                setLoading(false);
            }
        };

        fetchQuiz();
    }, [quizId]);

    // 타이머 설정
    useEffect(() => {
        if (!quizStarted || quizCompleted || timeLeft <= 0) return;

        const timer = setInterval(() => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    // 시간이 다 되면 자동으로 퀴즈 제출
                    handleSubmitQuiz();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [quizStarted, quizCompleted, timeLeft]);

    // 퀴즈 시작
    const handleStartQuiz = () => {
        setQuizStarted(true);
        setStartTime(Date.now());
    };

    // 답변 선택
    const handleAnswerSelect = (questionId: number, answer: string) => {
        setAnswers((prev) => ({
            ...prev,
            [questionId]: answer
        }));
    };

    // 다음 문제로 이동
    const handleNextQuestion = () => {
        if (quiz && currentQuestionIndex < quiz.questions.length - 1) {
            setCurrentQuestionIndex(currentQuestionIndex + 1);
        }
    };

    // 이전 문제로 이동
    const handlePrevQuestion = () => {
        if (currentQuestionIndex > 0) {
            setCurrentQuestionIndex(currentQuestionIndex - 1);
        }
    };

    // 퀴즈 제출
    const handleSubmitQuiz = async () => {
        console.log('Submitting quiz with:', { quiz, quizId, quizAttemptId, submitting });
        if (!quiz || !quizId || !quizAttemptId || submitting) return;

        setSubmitting(true);

        try {
            // 총 소요 시간 계산 (초)
            const timeTaken = startTime ? Math.floor((Date.now() - startTime) / 1000) : timeLeft;

            // 제출 데이터 구성
            const submitData: QuizSubmitRequest = {
                quizAttemptId: quizAttemptId,
                answers: answers,
                timeTaken: timeTaken
            };

            // API 호출
            const response = await quizApi.submitQuiz(parseInt(quizId), submitData);

            if (response.data.success) {
                setQuizCompleted(true);

                // 결과 페이지로 이동
                navigate(`/quizzes/${quizId}/results/${quizAttemptId}`);
            } else {
                setError('퀴즈 제출에 실패했습니다: ' + response.data.message);
                setSubmitting(false);
            }
        } catch (err: any) {
            console.error('퀴즈 제출 중 오류:', err);
            setError('퀴즈 제출 중 오류가 발생했습니다: ' + (err.response?.data?.message || err.message));
            setSubmitting(false);
        }
    };

    // 남은 시간 포맷팅
    const formatTimeLeft = () => {
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    // 현재 문제 렌더링
    const renderCurrentQuestion = () => {
        if (!quiz || !quiz.questions || quiz.questions.length === 0) {
            return <p>문제가 없습니다.</p>;
        }

        const currentQuestion = quiz.questions[currentQuestionIndex];

        return (
            <div className="question-container">
                <h2>문제 {currentQuestionIndex + 1}</h2>
                <p style={{ fontSize: '1.1rem', marginBottom: '1.5rem' }}>{currentQuestion.questionText}</p>

                {/* 코드 스니펫이 있는 경우 */}
                {currentQuestion.codeSnippet && (
                    <div className="code-snippet" style={{
                        backgroundColor: '#f5f5f5',
                        padding: '1rem',
                        borderRadius: '4px',
                        marginBottom: '1.5rem',
                        overflowX: 'auto',
                        fontFamily: 'monospace'
                    }}>
                        <pre style={{ margin: 0 }}>{currentQuestion.codeSnippet}</pre>
                    </div>
                )}

                {/* 다이어그램 데이터가 있는 경우 */}
                {currentQuestion.diagramData && (
                    <div className="diagram-container" style={{
                        marginBottom: '1.5rem',
                        textAlign: 'center'
                    }}>
                        {/* 여기에 다이어그램 렌더링 로직을 추가합니다. */}
                        <p>다이어그램 데이터: {currentQuestion.diagramData}</p>
                    </div>
                )}

                {/* 문제 유형에 따른 답변 UI */}
                {renderAnswerOptions(currentQuestion)}
            </div>
        );
    };

    // 답변 옵션 렌더링
    const renderAnswerOptions = (question: QuestionResponse) => {
        switch (question.questionType) {
            case 'MULTIPLE_CHOICE':
                return (
                    <div className="multiple-choice-options">
                        {question.options.map((option, index) => (
                            <div
                                key={index}
                                className="option"
                                style={{
                                    padding: '1rem',
                                    marginBottom: '0.5rem',
                                    border: `1px solid ${answers[question.id] === option ? '#1976d2' : '#e0e0e0'}`,
                                    borderRadius: '4px',
                                    cursor: 'pointer',
                                    backgroundColor: answers[question.id] === option ? '#e3f2fd' : 'white'
                                }}
                                onClick={() => handleAnswerSelect(question.id, option)}
                            >
                                <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                                    <input
                                        type="radio"
                                        name={`question-${question.id}`}
                                        value={option}
                                        checked={answers[question.id] === option}
                                        onChange={() => handleAnswerSelect(question.id, option)}
                                        style={{ marginRight: '0.5rem' }}
                                    />
                                    {option}
                                </label>
                            </div>
                        ))}
                    </div>
                );

            case 'TRUE_FALSE':
                return (
                    <div className="true-false-options">
                        {['True', 'False'].map((option) => (
                            <div
                                key={option}
                                className="option"
                                style={{
                                    padding: '1rem',
                                    marginBottom: '0.5rem',
                                    border: `1px solid ${answers[question.id] === option ? '#1976d2' : '#e0e0e0'}`,
                                    borderRadius: '4px',
                                    cursor: 'pointer',
                                    backgroundColor: answers[question.id] === option ? '#e3f2fd' : 'white'
                                }}
                                onClick={() => handleAnswerSelect(question.id, option)}
                            >
                                <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                                    <input
                                        type="radio"
                                        name={`question-${question.id}`}
                                        value={option}
                                        checked={answers[question.id] === option}
                                        onChange={() => handleAnswerSelect(question.id, option)}
                                        style={{ marginRight: '0.5rem' }}
                                    />
                                    {option}
                                </label>
                            </div>
                        ))}
                    </div>
                );

            case 'SHORT_ANSWER':
                return (
                    <div className="short-answer">
                        <textarea
                            value={answers[question.id] || ''}
                            onChange={(e) => handleAnswerSelect(question.id, e.target.value)}
                            placeholder="답변을 입력하세요..."
                            style={{
                                width: '100%',
                                padding: '1rem',
                                borderRadius: '4px',
                                border: '1px solid #e0e0e0',
                                fontSize: '1rem',
                                minHeight: '100px'
                            }}
                        />
                    </div>
                );

            case 'CODE_ANALYSIS':
            case 'DIAGRAM_BASED':
            default:
                return (
                    <div className="default-answer">
                        <textarea
                            value={answers[question.id] || ''}
                            onChange={(e) => handleAnswerSelect(question.id, e.target.value)}
                            placeholder="답변을 입력하세요..."
                            style={{
                                width: '100%',
                                padding: '1rem',
                                borderRadius: '4px',
                                border: '1px solid #e0e0e0',
                                fontSize: '1rem',
                                minHeight: '100px'
                            }}
                        />
                    </div>
                );
        }
    };

    // 로딩 상태 UI
    if (loading) {
        return (
            <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>퀴즈를 불러오는 중...</p>
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
                <button onClick={() => navigate(`/quizzes/${quizId}`)} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginTop: '0.5rem'
                }}>
                    퀴즈 상세 페이지로 돌아가기
                </button>
            </div>
        );
    }

    // 퀴즈 시작 전 UI
    if (!quizStarted) {
        return (
            <div className="quiz-intro-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <h1>{quiz?.title}</h1>
                <p style={{ marginBottom: '2rem' }}>{quiz?.description}</p>

                <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'left', marginBottom: '2rem' }}>
                    <h2>퀴즈 정보</h2>
                    <ul style={{ listStyle: 'none', padding: 0 }}>
                        <li style={{ padding: '0.5rem 0', borderBottom: '1px solid #e0e0e0' }}>
                            <strong>문제 수:</strong> {quiz?.questionCount}문제
                        </li>
                        <li style={{ padding: '0.5rem 0', borderBottom: '1px solid #e0e0e0' }}>
                            <strong>제한 시간:</strong> {quiz?.timeLimit}분
                        </li>
                        <li style={{ padding: '0.5rem 0', borderBottom: '1px solid #e0e0e0' }}>
                            <strong>난이도:</strong> {quiz?.difficultyLevel}
                        </li>
                    </ul>
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
        );
    }

    // 퀴즈 플레이 UI
    return (
        <div className="quiz-play-container">
            {/* 퀴즈 헤더 */}
            <div className="quiz-header" style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '1rem',
                backgroundColor: '#f5f5f5',
                borderRadius: '4px',
                marginBottom: '1.5rem'
            }}>
                <div>
                    <h1 style={{ margin: 0, fontSize: '1.3rem' }}>{quiz?.title}</h1>
                    <p style={{ margin: '0.5rem 0 0', fontSize: '0.9rem' }}>
                        문제 {currentQuestionIndex + 1} / {quiz?.questions.length}
                    </p>
                </div>

                <div className="timer" style={{
                    backgroundColor: timeLeft < 60 ? '#f44336' : '#1976d2',
                    color: 'white',
                    padding: '0.5rem 1rem',
                    borderRadius: '4px',
                    fontWeight: 'bold',
                    fontSize: '1.1rem'
                }}>
                    {formatTimeLeft()}
                </div>
            </div>

            {/* 현재 문제 */}
            {renderCurrentQuestion()}

            {/* 네비게이션 버튼 */}
            <div className="question-nav" style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginTop: '2rem'
            }}>
                <button
                    onClick={handlePrevQuestion}
                    disabled={currentQuestionIndex === 0}
                    style={{
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        border: '1px solid #1976d2',
                        backgroundColor: 'white',
                        color: '#1976d2',
                        cursor: currentQuestionIndex === 0 ? 'not-allowed' : 'pointer',
                        opacity: currentQuestionIndex === 0 ? 0.5 : 1
                    }}
                >
                    이전 문제
                </button>

                {currentQuestionIndex < (quiz?.questions.length || 0) - 1 ? (
                    <button
                        onClick={handleNextQuestion}
                        style={{
                            padding: '0.5rem 1rem',
                            borderRadius: '4px',
                            border: 'none',
                            backgroundColor: '#1976d2',
                            color: 'white',
                            cursor: 'pointer'
                        }}
                    >
                        다음 문제
                    </button>
                ) : (
                    <button
                        onClick={handleSubmitQuiz}
                        disabled={submitting}
                        style={{
                            padding: '0.5rem 1rem',
                            borderRadius: '4px',
                            border: 'none',
                            backgroundColor: submitting ? '#ccc' : '#4caf50',
                            color: 'white',
                            cursor: submitting ? 'not-allowed' : 'pointer',
                            fontWeight: 'bold'
                        }}
                    >
                        {submitting ? '제출 중...' : '퀴즈 제출하기'}
                    </button>
                )}
            </div>

            {/* 문제 인덱스 */}
            <div className="question-index" style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: '0.5rem',
                marginTop: '2rem'
            }}>
                {quiz?.questions.map((_, index) => (
                    <button
                        key={index}
                        onClick={() => setCurrentQuestionIndex(index)}
                        style={{
                            width: '40px',
                            height: '40px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            borderRadius: '50%',
                            border: 'none',
                            backgroundColor: index === currentQuestionIndex
                                ? '#1976d2'
                                : answers[quiz.questions[index].id]
                                    ? '#e3f2fd'
                                    : '#f5f5f5',
                            color: index === currentQuestionIndex
                                ? 'white'
                                : answers[quiz.questions[index].id]
                                    ? '#1976d2'
                                    : '#333',
                            cursor: 'pointer',
                            fontWeight: index === currentQuestionIndex ? 'bold' : 'normal'
                        }}
                    >
                        {index + 1}
                    </button>
                ))}
            </div>

            {/* 제출 경고 */}
            {currentQuestionIndex === (quiz?.questions.length || 0) - 1 && (
                <div style={{
                    marginTop: '2rem',
                    padding: '1rem',
                    backgroundColor: '#fff9c4',
                    borderRadius: '4px',
                    border: '1px solid #ffeb3b'
                }}>
                    <p style={{ margin: 0, fontWeight: 'bold', color: '#f57f17' }}>
                        제출 전 확인하세요!
                    </p>
                    <p style={{ margin: '0.5rem 0 0' }}>
                        퀴즈를 제출하면 더 이상 답변을 수정할 수 없습니다.
                        {Object.keys(answers).length < (quiz?.questions.length || 0) &&
                            ` 아직 ${(quiz?.questions.length || 0) - Object.keys(answers).length}개의 문제에 답하지 않았습니다.`}
                    </p>
                </div>
            )}
        </div>
    );
};

export default QuizPlayPage;