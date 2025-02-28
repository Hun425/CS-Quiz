// src/pages/QuizCreatePage.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { quizApi } from '../api/quizApi';
import { TagResponse, QuestionCreateRequest, QuizCreateRequest } from '../types/api';
import { useAuthStore } from '../store/authStore';

// 기본 빈 문제 객체
const emptyQuestion: QuestionCreateRequest = {
    questionType: 'MULTIPLE_CHOICE',
    questionText: '',
    options: ['', '', '', ''],
    correctAnswer: '',
    explanation: '',
    points: 10,
    difficultyLevel: 'INTERMEDIATE'
};

const QuizCreatePage: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuthStore();

    // 상태 관리
    const [title, setTitle] = useState<string>('');
    const [description, setDescription] = useState<string>('');
    const [quizType, setQuizType] = useState<'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM'>('CUSTOM');
    const [difficultyLevel, setDifficultyLevel] = useState<'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'>('INTERMEDIATE');
    const [timeLimit, setTimeLimit] = useState<number>(30);
    const [selectedTags, setSelectedTags] = useState<number[]>([]);
    const [questions, setQuestions] = useState<QuestionCreateRequest[]>([{ ...emptyQuestion }]);
    const [tags, setTags] = useState<TagResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<boolean>(false);

    // 인증 확인 및 태그 데이터 로드
    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: '/create-quiz' } });
            return;
        }

        // 임시 태그 데이터
        setTags([
            { id: 1, name: '알고리즘', description: '알고리즘 관련 퀴즈', quizCount: 10, synonyms: [] },
            { id: 2, name: '자료구조', description: '자료구조 관련 퀴즈', quizCount: 8, synonyms: [] },
            { id: 3, name: '네트워크', description: '네트워크 관련 퀴즈', quizCount: 5, synonyms: [] },
            { id: 4, name: '운영체제', description: '운영체제 관련 퀴즈', quizCount: 7, synonyms: [] },
            { id: 5, name: '데이터베이스', description: 'DB 관련 퀴즈', quizCount: 6, synonyms: [] }
        ]);
    }, [isAuthenticated, navigate]);

    // 문제 추가
    const addQuestion = () => {
        setQuestions([...questions, { ...emptyQuestion }]);
    };

    // 문제 삭제
    const removeQuestion = (index: number) => {
        if (questions.length > 1) {
            const updatedQuestions = [...questions];
            updatedQuestions.splice(index, 1);
            setQuestions(updatedQuestions);
        }
    };

    // 문제 필드 업데이트
    const updateQuestionField = (index: number, field: keyof QuestionCreateRequest, value: any) => {
        const updatedQuestions = [...questions];
        updatedQuestions[index] = {
            ...updatedQuestions[index],
            [field]: value
        };
        setQuestions(updatedQuestions);
    };

    // 옵션 업데이트
    const updateOption = (questionIndex: number, optionIndex: number, value: string) => {
        const updatedQuestions = [...questions];
        const options = [...updatedQuestions[questionIndex].options];
        options[optionIndex] = value;
        updatedQuestions[questionIndex].options = options;
        setQuestions(updatedQuestions);
    };

    // 옵션 추가
    const addOption = (questionIndex: number) => {
        const updatedQuestions = [...questions];
        updatedQuestions[questionIndex].options.push('');
        setQuestions(updatedQuestions);
    };

    // 옵션 삭제
    const removeOption = (questionIndex: number, optionIndex: number) => {
        const updatedQuestions = [...questions];
        if (updatedQuestions[questionIndex].options.length > 2) {
            updatedQuestions[questionIndex].options.splice(optionIndex, 1);
            setQuestions(updatedQuestions);
        }
    };

    // 태그 선택/해제 토글
    const toggleTagSelection = (tagId: number) => {
        if (selectedTags.includes(tagId)) {
            setSelectedTags(selectedTags.filter(id => id !== tagId));
        } else {
            setSelectedTags([...selectedTags, tagId]);
        }
    };

    // 퀴즈 생성 유효성 검사
    const validateQuiz = (): boolean => {
        if (!title || title.trim() === '') {
            setError('퀴즈 제목을 입력해주세요.');
            return false;
        }

        if (!description || description.trim() === '') {
            setError('퀴즈 설명을 입력해주세요.');
            return false;
        }

        if (selectedTags.length === 0) {
            setError('최소 하나 이상의 태그를 선택해주세요.');
            return false;
        }

        for (let i = 0; i < questions.length; i++) {
            const q = questions[i];
            if (!q.questionText || q.questionText.trim() === '') {
                setError(`문제 ${i + 1}의 문제 내용을 입력해주세요.`);
                return false;
            }

            if (q.questionType === 'MULTIPLE_CHOICE') {
                if (q.options.some(opt => !opt || opt.trim() === '')) {
                    setError(`문제 ${i + 1}의 모든 선택지를 입력해주세요.`);
                    return false;
                }
            }

            if (!q.correctAnswer || q.correctAnswer.trim() === '') {
                setError(`문제 ${i + 1}의 정답을 입력해주세요.`);
                return false;
            }

            if (!q.explanation || q.explanation.trim() === '') {
                setError(`문제 ${i + 1}의 해설을 입력해주세요.`);
                return false;
            }
        }

        return true;
    };

    // 퀴즈 생성 제출
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateQuiz()) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const quizData: QuizCreateRequest = {
                title,
                description,
                quizType,
                difficultyLevel,
                timeLimit,
                tagIds: selectedTags,
                questions
            };

            const response = await quizApi.createQuiz(quizData);

            if (response.data.success) {
                setSuccess(true);
                const newQuizId = response.data.data.id;
                // 3초 후 생성된 퀴즈 상세 페이지로 이동
                setTimeout(() => {
                    navigate(`/quizzes/${newQuizId}`);
                }, 3000);
            } else {
                setError('퀴즈 생성 중 오류가 발생했습니다.');
            }
        } catch (err: any) {
            console.error('퀴즈 생성 중 오류:', err);
            setError('퀴즈 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } finally {
            setLoading(false);
        }
    };

    // 문제 유형별 입력 폼 렌더링
    const renderQuestionTypeForm = (question: QuestionCreateRequest, index: number) => {
        switch (question.questionType) {
            case 'MULTIPLE_CHOICE':
                return (
                    <div className="question-options">
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            선택지
                        </label>
                        {question.options.map((option, optionIndex) => (
                            <div key={optionIndex} style={{ display: 'flex', marginBottom: '0.5rem' }}>
                                <input
                                    type="text"
                                    value={option}
                                    onChange={(e) => updateOption(index, optionIndex, e.target.value)}
                                    placeholder={`선택지 ${optionIndex + 1}`}
                                    style={{ flex: 1, padding: '0.5rem', marginRight: '0.5rem' }}
                                />
                                <button
                                    type="button"
                                    onClick={() => removeOption(index, optionIndex)}
                                    disabled={question.options.length <= 2}
                                    style={{
                                        padding: '0.5rem',
                                        backgroundColor: '#f44336',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: question.options.length <= 2 ? 'not-allowed' : 'pointer',
                                        opacity: question.options.length <= 2 ? 0.5 : 1
                                    }}
                                >
                                    삭제
                                </button>
                            </div>
                        ))}
                        <button
                            type="button"
                            onClick={() => addOption(index)}
                            style={{
                                padding: '0.5rem',
                                backgroundColor: '#4caf50',
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                marginTop: '0.5rem'
                            }}
                        >
                            선택지 추가
                        </button>

                        <div style={{ marginTop: '1rem' }}>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                정답 선택
                            </label>
                            <select
                                value={question.correctAnswer}
                                onChange={(e) => updateQuestionField(index, 'correctAnswer', e.target.value)}
                                style={{ width: '100%', padding: '0.5rem' }}
                            >
                                <option value="">정답 선택</option>
                                {question.options.map((option, optIndex) => (
                                    <option key={optIndex} value={option} disabled={!option}>
                                        {option || `선택지 ${optIndex + 1} (입력 필요)`}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                );

            case 'TRUE_FALSE':
                return (
                    <div className="true-false-options">
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            정답
                        </label>
                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <label style={{ display: 'flex', alignItems: 'center' }}>
                                <input
                                    type="radio"
                                    name={`correct-answer-${index}`}
                                    value="True"
                                    checked={question.correctAnswer === "True"}
                                    onChange={() => updateQuestionField(index, 'correctAnswer', 'True')}
                                    style={{ marginRight: '0.5rem' }}
                                />
                                참 (True)
                            </label>
                            <label style={{ display: 'flex', alignItems: 'center' }}>
                                <input
                                    type="radio"
                                    name={`correct-answer-${index}`}
                                    value="False"
                                    checked={question.correctAnswer === "False"}
                                    onChange={() => updateQuestionField(index, 'correctAnswer', 'False')}
                                    style={{ marginRight: '0.5rem' }}
                                />
                                거짓 (False)
                            </label>
                        </div>
                    </div>
                );

            case 'SHORT_ANSWER':
            case 'CODE_ANALYSIS':
            case 'DIAGRAM_BASED':
                return (
                    <div className="text-answer">
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            정답
                        </label>
                        <input
                            type="text"
                            value={question.correctAnswer}
                            onChange={(e) => updateQuestionField(index, 'correctAnswer', e.target.value)}
                            placeholder="정답을 입력하세요"
                            style={{ width: '100%', padding: '0.5rem' }}
                        />
                    </div>
                );

            default:
                return null;
        }
    };

    return (
        <div className="quiz-create-page">
            <h1>새 퀴즈 만들기</h1>

            {/* 성공 메시지 */}
            {success && (
                <div style={{
                    backgroundColor: '#e8f5e9',
                    color: '#2e7d32',
                    padding: '1rem',
                    borderRadius: '4px',
                    marginBottom: '1rem'
                }}>
                    <p>퀴즈가 성공적으로 생성되었습니다! 잠시 후 퀴즈 상세 페이지로 이동합니다.</p>
                </div>
            )}

            {/* 오류 메시지 */}
            {error && (
                <div style={{
                    backgroundColor: '#ffebee',
                    color: '#d32f2f',
                    padding: '1rem',
                    borderRadius: '4px',
                    marginBottom: '1rem'
                }}>
                    <p>{error}</p>
                </div>
            )}

            <form onSubmit={handleSubmit}>
                {/* 퀴즈 기본 정보 */}
                <div className="quiz-info-section" style={{
                    backgroundColor: '#f5f5f5',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '1.5rem'
                }}>
                    <h2 style={{ marginTop: 0 }}>퀴즈 정보</h2>

                    <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="quiz-title" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            제목
                        </label>
                        <input
                            id="quiz-title"
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="퀴즈 제목을 입력하세요"
                            style={{ width: '100%', padding: '0.5rem' }}
                            required
                        />
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="quiz-description" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            설명
                        </label>
                        <textarea
                            id="quiz-description"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="퀴즈에 대한 설명을 입력하세요"
                            style={{ width: '100%', padding: '0.5rem', minHeight: '100px' }}
                            required
                        />
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="quiz-type" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            퀴즈 유형
                        </label>
                        <select
                            id="quiz-type"
                            value={quizType}
                            onChange={(e) => setQuizType(e.target.value as any)}
                            style={{ width: '100%', padding: '0.5rem' }}
                        >
                            <option value="CUSTOM">커스텀 퀴즈</option>
                            <option value="TAG_BASED">태그 기반 퀴즈</option>
                            <option value="TOPIC_BASED">주제 기반 퀴즈</option>
                        </select>
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="difficulty-level" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            난이도
                        </label>
                        <select
                            id="difficulty-level"
                            value={difficultyLevel}
                            onChange={(e) => setDifficultyLevel(e.target.value as any)}
                            style={{ width: '100%', padding: '0.5rem' }}
                        >
                            <option value="BEGINNER">입문</option>
                            <option value="INTERMEDIATE">중급</option>
                            <option value="ADVANCED">고급</option>
                        </select>
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label htmlFor="time-limit" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            제한 시간 (분)
                        </label>
                        <input
                            id="time-limit"
                            type="number"
                            min="1"
                            max="120"
                            value={timeLimit}
                            onChange={(e) => setTimeLimit(parseInt(e.target.value))}
                            style={{ width: '100%', padding: '0.5rem' }}
                            required
                        />
                    </div>

                    <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                            태그
                        </label>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                            {tags.map((tag) => (
                                <button
                                    key={tag.id}
                                    type="button"
                                    onClick={() => toggleTagSelection(tag.id)}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        borderRadius: '4px',
                                        border: '1px solid #1976d2',
                                        backgroundColor: selectedTags.includes(tag.id) ? '#1976d2' : 'white',
                                        color: selectedTags.includes(tag.id) ? 'white' : '#1976d2',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {tag.name}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                {/* 문제 섹션 */}
                <div className="questions-section">
                    <h2>문제</h2>

                    {questions.map((question, index) => (
                        <div
                            key={index}
                            className="question-item"
                            style={{
                                backgroundColor: 'white',
                                padding: '1.5rem',
                                borderRadius: '8px',
                                marginBottom: '1.5rem',
                                border: '1px solid #e0e0e0'
                            }}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                                <h3 style={{ margin: 0 }}>문제 {index + 1}</h3>
                                <button
                                    type="button"
                                    onClick={() => removeQuestion(index)}
                                    disabled={questions.length === 1}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        backgroundColor: '#f44336',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: questions.length === 1 ? 'not-allowed' : 'pointer',
                                        opacity: questions.length === 1 ? 0.5 : 1
                                    }}
                                >
                                    문제 삭제
                                </button>
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor={`question-type-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    문제 유형
                                </label>
                                <select
                                    id={`question-type-${index}`}
                                    value={question.questionType}
                                    onChange={(e) => updateQuestionField(index, 'questionType', e.target.value)}
                                    style={{ width: '100%', padding: '0.5rem' }}
                                >
                                    <option value="MULTIPLE_CHOICE">객관식</option>
                                    <option value="TRUE_FALSE">참/거짓</option>
                                    <option value="SHORT_ANSWER">주관식</option>
                                    <option value="CODE_ANALYSIS">코드 분석</option>
                                    <option value="DIAGRAM_BASED">다이어그램 기반</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor={`question-text-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    문제 내용
                                </label>
                                <textarea
                                    id={`question-text-${index}`}
                                    value={question.questionText}
                                    onChange={(e) => updateQuestionField(index, 'questionText', e.target.value)}
                                    placeholder="문제 내용을 입력하세요"
                                    style={{ width: '100%', padding: '0.5rem', minHeight: '100px' }}
                                    required
                                />
                            </div>

                            {question.questionType === 'CODE_ANALYSIS' && (
                                <div style={{ marginBottom: '1rem' }}>
                                    <label htmlFor={`code-snippet-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                        코드 스니펫
                                    </label>
                                    <textarea
                                        id={`code-snippet-${index}`}
                                        value={question.codeSnippet || ''}
                                        onChange={(e) => updateQuestionField(index, 'codeSnippet', e.target.value)}
                                        placeholder="코드 스니펫을 입력하세요"
                                        style={{
                                            width: '100%',
                                            padding: '0.5rem',
                                            minHeight: '150px',
                                            fontFamily: 'monospace',
                                            backgroundColor: '#f5f5f5'
                                        }}
                                    />
                                </div>
                            )}

                            {question.questionType === 'DIAGRAM_BASED' && (
                                <div style={{ marginBottom: '1rem' }}>
                                    <label htmlFor={`diagram-data-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                        다이어그램 데이터
                                    </label>
                                    <textarea
                                        id={`diagram-data-${index}`}
                                        value={question.diagramData || ''}
                                        onChange={(e) => updateQuestionField(index, 'diagramData', e.target.value)}
                                        placeholder="다이어그램 데이터를 입력하세요"
                                        style={{ width: '100%', padding: '0.5rem', minHeight: '150px' }}
                                    />
                                </div>
                            )}

                            {/* 문제 유형별 입력 폼 */}
                            {renderQuestionTypeForm(question, index)}

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor={`explanation-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    해설
                                </label>
                                <textarea
                                    id={`explanation-${index}`}
                                    value={question.explanation}
                                    onChange={(e) => updateQuestionField(index, 'explanation', e.target.value)}
                                    placeholder="문제 해설을 입력하세요"
                                    style={{ width: '100%', padding: '0.5rem', minHeight: '100px' }}
                                    required
                                />
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor={`points-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    배점
                                </label>
                                <input
                                    id={`points-${index}`}
                                    type="number"
                                    min="1"
                                    max="100"
                                    value={question.points}
                                    onChange={(e) => updateQuestionField(index, 'points', parseInt(e.target.value))}
                                    style={{ width: '100%', padding: '0.5rem' }}
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor={`difficulty-${index}`} style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    문제 난이도
                                </label>
                                <select
                                    id={`difficulty-${index}`}
                                    value={question.difficultyLevel}
                                    onChange={(e) => updateQuestionField(index, 'difficultyLevel', e.target.value)}
                                    style={{ width: '100%', padding: '0.5rem' }}
                                >
                                    <option value="BEGINNER">입문</option>
                                    <option value="INTERMEDIATE">중급</option>
                                    <option value="ADVANCED">고급</option>
                                </select>
                            </div>
                        </div>
                    ))}

                    <button
                        type="button"
                        onClick={addQuestion}
                        style={{
                            padding: '0.75rem 1.5rem',
                            backgroundColor: '#4caf50',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            marginBottom: '2rem',
                            fontSize: '1rem',
                            fontWeight: 'bold'
                        }}
                    >
                        문제 추가
                    </button>
                </div>

                {/* 제출 버튼 */}
                <div style={{ marginTop: '2rem', textAlign: 'center' }}>
                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            padding: '1rem 2rem',
                            backgroundColor: loading ? '#ccc' : '#1976d2',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: loading ? 'not-allowed' : 'pointer',
                            fontSize: '1.1rem',
                            fontWeight: 'bold'
                        }}
                    >
                        {loading ? '처리 중...' : '퀴즈 생성하기'}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default QuizCreatePage;