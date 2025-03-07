// src/pages/QuizListPage.tsx
import React, { useEffect, useState } from 'react';
import { quizApi } from '../api/quizApi';
import { tagApi } from '../api/tagApi';
import { QuizSummaryResponse, PageResponse, TagResponse } from '../types/api';
import QuizCard from '../components/quiz/QuizCard';
import TagSelector from '../components/tag/TagSelector';

const QuizListPage: React.FC = () => {
    // 상태 관리
    const [quizzes, setQuizzes] = useState<QuizSummaryResponse[]>([]);
    const [tags, setTags] = useState<TagResponse[]>([]);
    const [popularTags, setPopularTags] = useState<TagResponse[]>([]);
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
        fetchTags();
        fetchPopularTags();
        fetchQuizzes();
    }, []);

    // 페이지나 필터가 변경될 때 자동으로 데이터 로드
    useEffect(() => {
        if (selectedDifficulty || selectedQuizType) {
            fetchQuizzes();
        }
    }, [selectedDifficulty, selectedQuizType]);

    // 태그 데이터 가져오기
    const fetchTags = async () => {
        try {
            const response = await tagApi.getAllTags();
            if (response.data.success) {
                setTags(response.data.data);
            } else {
                console.error('태그 데이터를 불러오는 데 실패했습니다.');
            }
        } catch (err) {
            console.error('태그 로딩 중 오류:', err);
        }
    };

    // 인기 태그 가져오기
    const fetchPopularTags = async () => {
        try {
            const response = await tagApi.getPopularTags(10); // 상위 10개 태그만 가져옴
            if (response.data.success) {
                setPopularTags(response.data.data);
            }
        } catch (err) {
            console.error('인기 태그 로딩 중 오류:', err);
        }
    };

    // 퀴즈 데이터 가져오기
    const fetchQuizzes = async () => {
        try {
            setLoading(true);
            setError(null);

            // 검색 파라미터 구성
            const searchParams = {
                title: searchTitle || undefined,
                difficultyLevel: selectedDifficulty || undefined,
                quizType: selectedQuizType || undefined,
                tagIds: selectedTags.length > 0 ? selectedTags : undefined
            };

            console.log('검색 요청 파라미터:', searchParams);

            console.log(`API 요청 - 태그 IDs: ${selectedTags.join(', ')}`);
            const response = await quizApi.searchQuizzes(searchParams, page, size);

            if (response.data.success) {
                const pageData: PageResponse<QuizSummaryResponse> = response.data.data;
                setQuizzes(pageData.content);
                setTotalPages(pageData.totalPages);
                console.log('퀴즈 데이터 로드 성공:', pageData);
                console.log('불러온 퀴즈 제목:', pageData.content.map(quiz => quiz.title));
            } else {
                setError('퀴즈 데이터를 불러오는 데 실패했습니다.');
                console.error('API 응답 실패:', response.data.message);
            }
        } catch (err: any) {
            console.error('퀴즈 로딩 중 오류:', err);
            setError('퀴즈 데이터를 불러오는 중 오류가 발생했습니다: ' + (err.message || '알 수 없는 오류'));
        } finally {
            setLoading(false);
        }
    };

    // 검색 버튼 클릭 시 호출되는 함수
    const handleSearch = () => {
        setPage(0);  // 검색 시 첫 페이지로 리셋
        fetchQuizzes();
    };

    // 페이지 변경 핸들러
    const handlePageChange = (newPage: number) => {
        setPage(newPage);
        fetchQuizzes();  // 페이지 변경 시 데이터 다시 불러오기
    };

    // 태그 선택 변경 핸들러
    const handleTagChange = (tagIds: number[]) => {
        setSelectedTags(tagIds);
        setPage(0); // 페이지를 첫 페이지로 리셋
        // 태그 변경 시 자동으로 검색 실행
        setTimeout(() => {
            fetchQuizzes();
        }, 0);
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
                            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
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

                {/* 태그 필터 (TagSelector 컴포넌트 사용) */}
                <div style={{ marginBottom: '1rem' }}>
                    <TagSelector
                        selectedTagIds={selectedTags}
                        onChange={handleTagChange}
                        label="태그로 필터링"
                    />
                </div>

                {/* 인기 태그 버튼 */}
                <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem' }}>
                        인기 태그
                    </label>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        {popularTags.map((tag) => (
                            <button
                                key={tag.id}
                                onClick={() => {
                                    let newSelectedTags;
                                    if (selectedTags.includes(tag.id)) {
                                        newSelectedTags = selectedTags.filter(id => id !== tag.id);
                                    } else {
                                        newSelectedTags = [...selectedTags, tag.id];
                                    }
                                    setSelectedTags(newSelectedTags);
                                    setPage(0); // 페이지를 첫 페이지로 리셋

                                    // 태그 변경 후 자동으로 검색 실행
                                    setTimeout(() => {
                                        fetchQuizzes();
                                    }, 0);
                                }}
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