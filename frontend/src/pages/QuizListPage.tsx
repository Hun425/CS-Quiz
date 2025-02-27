// src/pages/QuizListPage.tsx
import React, { useEffect, useState } from 'react';
import { quizApi } from '../api/quizApi';
import { QuizSummaryResponse, PageResponse, TagResponse } from '../types/api';
import QuizCard from '../components/quiz/QuizCard';

const QuizListPage: React.FC = () => {
    // 상태 관리
    const [quizzes, setQuizzes] = useState<QuizSummaryResponse[]>([]);
    const [tags, setTags] = useState<TagResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // 페이지네이션 상태
    const [page, setPage] = useState<number>(0);
    const [size, setSize] = useState<number>(9);
    const [totalPages, setTotalPages] = useState<number>(0);

    // 필터 상태
    const [searchTitle, setSearchTitle] = useState<string>('');
    const [selectedDifficulty, setSelectedDifficulty] = useState<string>('');
    const [selectedQuizType, setSelectedQuizType] = useState<string>('');
    const [selectedTags, setSelectedTags] = useState<number[]>([]);

    // 초기 데이터 로드
    useEffect(() => {
        const fetchTags = async () => {
            try {
                // 여기서는 태그 API를 별도로 호출하는 것을 가정합니다.
                // 실제로는 태그 API를 구현해야 합니다.
                // 예시로 더미 데이터를 사용합니다.
                setTags([
                    { id: 1, name: '알고리즘', description: '알고리즘 관련 퀴즈', quizCount: 10, synonyms: [] },
                    { id: 2, name: '자료구조', description: '자료구조 관련 퀴즈', quizCount: 8, synonyms: [] },
                    { id: 3, name: '네트워크', description: '네트워크 관련 퀴즈', quizCount: 5, synonyms: [] },
                    { id: 4, name: '운영체제', description: '운영체제 관련 퀴즈', quizCount: 7, synonyms: [] },
                    { id: 5, name: '데이터베이스', description: 'DB 관련 퀴즈', quizCount: 6, synonyms: [] }
                ]);
            } catch (err) {
                console.error('태그 로딩 중 오류:', err);
            }
        };

        fetchTags();
        fetchQuizzes();
    }, []);

    // 필터링 적용 시 퀴즈 다시 로드
    useEffect(() => {
        fetchQuizzes();
    }, [page, size, selectedDifficulty, selectedQuizType, selectedTags]);

    // 검색 버튼 클릭 시 호출되는 함수
    const handleSearch = () => {
        setPage(0); // 검색 시 첫 페이지로 리셋
        fetchQuizzes();
    };

    // 퀴즈 데이터 가져오기
    const fetchQuizzes = async () => {
        try {
            setLoading(true);

            // 검색 파라미터 구성
            const searchParams = {
                title: searchTitle || undefined,
                difficultyLevel: selectedDifficulty || undefined,
                quizType: selectedQuizType || undefined,
                tagIds: selectedTags.length > 0 ? selectedTags : undefined
            };

            const response = await quizApi.searchQuizzes(searchParams, page, size);

            if (response.data.success) {
                const pageData: PageResponse<QuizSummaryResponse> = response.data.data;
                setQuizzes(pageData.content);
                setTotalPages(pageData.totalPages);
            } else {
                setError('퀴즈 데이터를 불러오는 데 실패했습니다.');
            }
        } catch (err: any) {
            console.error('퀴즈 로딩 중 오류:', err);
            setError('퀴즈 데이터를 불러오는 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
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

    // 페이지 변경 핸들러
    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    return (
        <div className="quiz-list-page">
            <h1>퀴즈 목록</h1>

            {/* 필터 섹션 */}
            <div className="filter-section" style={{
                backgroundColor: '#f5f5f5',
                padding: '1rem',
                borderRadius: '8px',
                marginBottom: '1.5rem'
            }}>
                <h2 style={{ marginTop: 0, fontSize: '1.2rem' }}>필터 및 검색</h2>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                    gap: '1rem',
                    marginBottom: '1rem'
                }}>
                    {/* 제목 검색 */}
                    <div>
                        <label htmlFor="title-search" style={{ display: 'block', marginBottom: '0.5rem' }}>
                            제목 검색
                        </label>
                        <input
                            id="title-search"
                            type="text"
                            value={searchTitle}
                            onChange={(e) => setSearchTitle(e.target.value)}
                            placeholder="퀴즈 제목 검색..."
                            style={{
                                width: '100%',
                                padding: '0.5rem',
                                borderRadius: '4px',
                                border: '1px solid #ccc'
                            }}
                        />
                    </div>

                    {/* 난이도 필터 */}
                    <div>
                        <label htmlFor="difficulty-filter" style={{ display: 'block', marginBottom: '0.5rem' }}>
                            난이도
                        </label>
                        <select
                            id="difficulty-filter"
                            value={selectedDifficulty}
                            onChange={(e) => setSelectedDifficulty(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '0.5rem',
                                borderRadius: '4px',
                                border: '1px solid #ccc'
                            }}
                        >
                            <option value="">모든 난이도</option>
                            <option value="BEGINNER">입문</option>
                            <option value="INTERMEDIATE">중급</option>
                            <option value="ADVANCED">고급</option>
                        </select>
                    </div>

                    {/* 퀴즈 유형 필터 */}
                    <div>
                        <label htmlFor="quiz-type-filter" style={{ display: 'block', marginBottom: '0.5rem' }}>
                            퀴즈 유형
                        </label>
                        <select
                            id="quiz-type-filter"
                            value={selectedQuizType}
                            onChange={(e) => setSelectedQuizType(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '0.5rem',
                                borderRadius: '4px',
                                border: '1px solid #ccc'
                            }}
                        >
                            <option value="">모든 유형</option>
                            <option value="DAILY">데일리 퀴즈</option>
                            <option value="TAG_BASED">태그 기반</option>
                            <option value="TOPIC_BASED">주제 기반</option>
                            <option value="CUSTOM">커스텀</option>
                        </select>
                    </div>
                </div>

                {/* 태그 필터 */}
                <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem' }}>
                        태그
                    </label>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        {tags.map((tag) => (
                            <button
                                key={tag.id}
                                onClick={() => toggleTagSelection(tag.id)}
                                style={{
                                    padding: '0.3rem 0.6rem',
                                    borderRadius: '4px',
                                    border: '1px solid #1976d2',
                                    backgroundColor: selectedTags.includes(tag.id) ? '#1976d2' : 'white',
                                    color: selectedTags.includes(tag.id) ? 'white' : '#1976d2',
                                    cursor: 'pointer'
                                }}
                            >
                                {tag.name} ({tag.quizCount})
                            </button>
                        ))}
                    </div>
                </div>

                {/* 검색 버튼 */}
                <button
                    onClick={handleSearch}
                    style={{
                        backgroundColor: '#1976d2',
                        color: 'white',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        border: 'none',
                        cursor: 'pointer',
                        fontWeight: 'bold'
                    }}
                >
                    검색
                </button>
            </div>

            {/* 로딩 및 에러 상태 처리 */}
            {loading ? (
                <div className="loading-container" style={{ textAlign: 'center', padding: '2rem' }}>
                    <p>퀴즈를 불러오는 중...</p>
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
                    <button onClick={fetchQuizzes} style={{
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
                    {/* 퀴즈 목록 */}
                    {quizzes.length > 0 ? (
                        <div className="quiz-grid" style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                            gap: '1.5rem',
                            marginBottom: '2rem'
                        }}>
                            {quizzes.map((quiz) => (
                                <QuizCard key={quiz.id} quiz={quiz} />
                            ))}
                        </div>
                    ) : (
                        <div style={{
                            textAlign: 'center',
                            padding: '2rem',
                            backgroundColor: '#f5f5f5',
                            borderRadius: '8px',
                            marginBottom: '2rem'
                        }}>
                            <p>검색 조건에 맞는 퀴즈가 없습니다.</p>
                        </div>
                    )}

                    {/* 페이지네이션 */}
                    {totalPages > 0 && (
                        <div className="pagination" style={{
                            display: 'flex',
                            justifyContent: 'center',
                            marginTop: '2rem'
                        }}>
                            <button
                                onClick={() => handlePageChange(page - 1)}
                                disabled={page === 0}
                                style={{
                                    padding: '0.5rem 1rem',
                                    borderRadius: '4px',
                                    border: '1px solid #ccc',
                                    backgroundColor: page === 0 ? '#f5f5f5' : 'white',
                                    cursor: page === 0 ? 'not-allowed' : 'pointer',
                                    marginRight: '0.5rem'
                                }}
                            >
                                이전
                            </button>

                            {[...Array(totalPages)].map((_, index) => (
                                <button
                                    key={index}
                                    onClick={() => handlePageChange(index)}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc',
                                        backgroundColor: page === index ? '#1976d2' : 'white',
                                        color: page === index ? 'white' : '#333',
                                        cursor: 'pointer',
                                        marginRight: '0.5rem'
                                    }}
                                >
                                    {index + 1}
                                </button>
                            ))}

                            <button
                                onClick={() => handlePageChange(page + 1)}
                                disabled={page === totalPages - 1}
                                style={{
                                    padding: '0.5rem 1rem',
                                    borderRadius: '4px',
                                    border: '1px solid #ccc',
                                    backgroundColor: page === totalPages - 1 ? '#f5f5f5' : 'white',
                                    cursor: page === totalPages - 1 ? 'not-allowed' : 'pointer'
                                }}
                            >
                                다음
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default QuizListPage;