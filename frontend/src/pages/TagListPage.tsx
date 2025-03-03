// src/pages/TagListPage.tsx
import React, { useEffect, useState } from 'react';
import { tagApi } from '../api/tagApi';
import { TagResponse, PageResponse } from '../types/api';
import { useAuthStore } from '../store/authStore';

const TagListPage: React.FC = () => {
    const [tags, setTags] = useState<TagResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [editMode, setEditMode] = useState<boolean>(false);
    const [selectedTag, setSelectedTag] = useState<TagResponse | null>(null);
    const [tagName, setTagName] = useState<string>('');
    const [tagDescription, setTagDescription] = useState<string>('');
    const [selectedParentId, setSelectedParentId] = useState<number | null>(null);
    const [synonyms, setSynonyms] = useState<string[]>([]);
    const [newSynonym, setNewSynonym] = useState<string>('');
    const [rootTags, setRootTags] = useState<TagResponse[]>([]);

    const { user } = useAuthStore();
    const isAdmin = user?.role === 'ADMIN';

    // 태그 데이터 로드
    useEffect(() => {
        fetchTags();
        if (isAdmin) {
            fetchRootTags();
        }
    }, [page, searchTerm]);

    const fetchTags = async () => {
        try {
            setLoading(true);
            const response = await tagApi.searchTags(searchTerm, page);
            if (response.data.success) {
                const pageData: PageResponse<TagResponse> = response.data.data;
                setTags(pageData.content);
                setTotalPages(pageData.totalPages);
            } else {
                setError('태그 데이터를 불러오는 데 실패했습니다.');
            }
        } catch (err: any) {
            console.error('태그 로딩 중 오류:', err);
            setError('태그 데이터를 불러오는 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const fetchRootTags = async () => {
        try {
            const response = await tagApi.getRootTags();
            if (response.data.success) {
                setRootTags(response.data.data);
            }
        } catch (err) {
            console.error('루트 태그 로딩 중 오류:', err);
        }
    };

    const handleSearch = () => {
        setPage(0);  // 검색 시 첫 페이지로 리셋
        fetchTags();
    };

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    const handleSelectTag = (tag: TagResponse) => {
        setSelectedTag(tag);
        setTagName(tag.name);
        setTagDescription(tag.description || '');
        setSelectedParentId(null);  // 초기값은 null로 설정
        setSynonyms(tag.synonyms ? Array.from(tag.synonyms) : []);
        setEditMode(true);
    };

    const handleAddSynonym = () => {
        if (newSynonym && !synonyms.includes(newSynonym)) {
            setSynonyms([...synonyms, newSynonym]);
            setNewSynonym('');
        }
    };

    const handleRemoveSynonym = (synonym: string) => {
        setSynonyms(synonyms.filter(s => s !== synonym));
    };

    const handleNewTag = () => {
        setSelectedTag(null);
        setTagName('');
        setTagDescription('');
        setSelectedParentId(null);
        setSynonyms([]);
        setEditMode(true);
    };

    const handleCancelEdit = () => {
        setEditMode(false);
    };

    const handleSaveTag = async () => {
        if (!tagName.trim()) {
            setError('태그 이름은 필수입니다.');
            return;
        }

        try {
            setLoading(true);

            const tagData = {
                name: tagName,
                description: tagDescription,
                parentId: selectedParentId,
                synonyms: synonyms
            };

            let response;
            if (selectedTag) {
                // 태그 수정
                response = await tagApi.updateTag(selectedTag.id, tagData);
            } else {
                // 새 태그 생성
                response = await tagApi.createTag(tagData);
            }

            if (response.data.success) {
                setEditMode(false);
                fetchTags();  // 태그 목록 새로고침
            } else {
                setError('태그 저장에 실패했습니다.');
            }
        } catch (err: any) {
            console.error('태그 저장 중 오류:', err);
            setError('태그 저장 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteTag = async (tagId: number) => {
        if (!window.confirm('정말로 이 태그를 삭제하시겠습니까?')) {
            return;
        }

        try {
            setLoading(true);
            const response = await tagApi.deleteTag(tagId);

            if (response.data.success) {
                fetchTags();  // 태그 목록 새로고침
                if (selectedTag?.id === tagId) {
                    setSelectedTag(null);
                    setEditMode(false);
                }
            } else {
                setError('태그 삭제에 실패했습니다.');
            }
        } catch (err: any) {
            console.error('태그 삭제 중 오류:', err);
            setError('태그 삭제 중 오류가 발생했습니다: ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="tag-list-page">
            <h1>태그 관리</h1>

            {/* 검색 및 필터 섹션 */}
            <div className="search-section" style={{
                backgroundColor: '#f5f5f5',
                padding: '1rem',
                borderRadius: '8px',
                marginBottom: '1.5rem'
            }}>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        placeholder="태그 이름으로 검색..."
                        style={{
                            flex: 1,
                            padding: '0.5rem',
                            borderRadius: '4px',
                            border: '1px solid #ccc'
                        }}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    />
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
                    {isAdmin && (
                        <button
                            onClick={handleNewTag}
                            style={{
                                backgroundColor: '#4caf50',
                                color: 'white',
                                padding: '0.5rem 1rem',
                                borderRadius: '4px',
                                border: 'none',
                                cursor: 'pointer',
                                fontWeight: 'bold'
                            }}
                        >
                            새 태그 생성
                        </button>
                    )}
                </div>
            </div>

            {/* 오류 메시지 표시 */}
            {error && (
                <div className="error-message" style={{
                    backgroundColor: '#ffebee',
                    color: '#d32f2f',
                    padding: '1rem',
                    borderRadius: '4px',
                    marginBottom: '1rem'
                }}>
                    <p>{error}</p>
                    <button
                        onClick={() => setError(null)}
                        style={{
                            backgroundColor: 'transparent',
                            color: '#d32f2f',
                            border: 'none',
                            padding: '0.25rem',
                            cursor: 'pointer',
                            textDecoration: 'underline'
                        }}
                    >
                        닫기
                    </button>
                </div>
            )}

            <div style={{ display: 'flex', gap: '2rem' }}>
                {/* 태그 목록 */}
                <div style={{ flex: '1' }}>
                    {loading && !editMode ? (
                        <div style={{ textAlign: 'center', padding: '2rem' }}>
                            <p>태그 로딩 중...</p>
                        </div>
                    ) : tags.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '2rem', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>
                            <p>조건에 맞는 태그가 없습니다.</p>
                        </div>
                    ) : (
                        <div className="tag-list" style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                            gap: '1rem'
                        }}>
                            {tags.map((tag) => (
                                <div
                                    key={tag.id}
                                    className="tag-card"
                                    style={{
                                        border: '1px solid #e0e0e0',
                                        borderRadius: '4px',
                                        padding: '1rem',
                                        backgroundColor: 'white',
                                        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        justifyContent: 'space-between'
                                    }}
                                >
                                    <div>
                                        <h3 style={{ margin: '0 0 0.5rem 0' }}>{tag.name}</h3>
                                        <p style={{ margin: '0 0 0.5rem 0', color: '#666', minHeight: '3em' }}>
                                            {tag.description || '설명 없음'}
                                        </p>
                                        <div style={{ fontSize: '0.9rem', color: '#666' }}>
                                            <span>퀴즈: {tag.quizCount}개</span>
                                            {tag.synonyms && tag.synonyms.length > 0 && (
                                                <div style={{ marginTop: '0.5rem' }}>
                                                    <span>동의어: </span>
                                                    {Array.from(tag.synonyms).map((synonym, index) => (
                                                        <span key={index} style={{
                                                            display: 'inline-block',
                                                            backgroundColor: '#e3f2fd',
                                                            padding: '0.2rem 0.5rem',
                                                            borderRadius: '20px',
                                                            fontSize: '0.8rem',
                                                            marginRight: '0.3rem',
                                                            marginBottom: '0.3rem'
                                                        }}>
                                                            {synonym}
                                                        </span>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    {isAdmin && (
                                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                                            <button
                                                onClick={() => handleSelectTag(tag)}
                                                style={{
                                                    flex: 1,
                                                    backgroundColor: '#1976d2',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    padding: '0.5rem',
                                                    cursor: 'pointer'
                                                }}
                                            >
                                                편집
                                            </button>
                                            <button
                                                onClick={() => handleDeleteTag(tag.id)}
                                                style={{
                                                    flex: 1,
                                                    backgroundColor: '#f44336',
                                                    color: 'white',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    padding: '0.5rem',
                                                    cursor: 'pointer'
                                                }}
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))}
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
                </div>

                {/* 태그 편집 폼 */}
                {editMode && isAdmin && (
                    <div className="tag-edit-form" style={{
                        flex: '1',
                        backgroundColor: '#f5f5f5',
                        padding: '1.5rem',
                        borderRadius: '8px',
                        position: 'sticky',
                        top: '20px'
                    }}>
                        <h2>{selectedTag ? '태그 편집' : '새 태그 생성'}</h2>
                        <form onSubmit={(e) => { e.preventDefault(); handleSaveTag(); }}>
                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor="tag-name" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    태그 이름 *
                                </label>
                                <input
                                    id="tag-name"
                                    type="text"
                                    value={tagName}
                                    onChange={(e) => setTagName(e.target.value)}
                                    required
                                    style={{
                                        width: '100%',
                                        padding: '0.5rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc'
                                    }}
                                />
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor="tag-description" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    태그 설명
                                </label>
                                <textarea
                                    id="tag-description"
                                    value={tagDescription}
                                    onChange={(e) => setTagDescription(e.target.value)}
                                    rows={4}
                                    style={{
                                        width: '100%',
                                        padding: '0.5rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc'
                                    }}
                                />
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor="parent-tag" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    부모 태그
                                </label>
                                <select
                                    id="parent-tag"
                                    value={selectedParentId || ''}
                                    onChange={(e) => setSelectedParentId(e.target.value ? Number(e.target.value) : null)}
                                    style={{
                                        width: '100%',
                                        padding: '0.5rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc'
                                    }}
                                >
                                    <option value="">없음 (최상위 태그)</option>
                                    {rootTags.map((tag) => (
                                        // 자기 자신을 부모로 선택할 수 없도록 제외
                                        selectedTag?.id !== tag.id && (
                                            <option key={tag.id} value={tag.id}>{tag.name}</option>
                                        )
                                    ))}
                                </select>
                            </div>

                            <div style={{ marginBottom: '1rem' }}>
                                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                                    동의어
                                </label>
                                <div style={{ display: 'flex', marginBottom: '0.5rem' }}>
                                    <input
                                        type="text"
                                        value={newSynonym}
                                        onChange={(e) => setNewSynonym(e.target.value)}
                                        placeholder="동의어 추가"
                                        style={{
                                            flex: 1,
                                            padding: '0.5rem',
                                            borderRadius: '4px',
                                            border: '1px solid #ccc',
                                            marginRight: '0.5rem'
                                        }}
                                    />
                                    <button
                                        type="button"
                                        onClick={handleAddSynonym}
                                        style={{
                                            backgroundColor: '#4caf50',
                                            color: 'white',
                                            padding: '0.5rem',
                                            borderRadius: '4px',
                                            border: 'none',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        추가
                                    </button>
                                </div>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                    {synonyms.map((synonym, index) => (
                                        <div
                                            key={index}
                                            style={{
                                                backgroundColor: '#e3f2fd',
                                                padding: '0.25rem 0.5rem',
                                                borderRadius: '20px',
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '0.5rem'
                                            }}
                                        >
                                            <span>{synonym}</span>
                                            <button
                                                type="button"
                                                onClick={() => handleRemoveSynonym(synonym)}
                                                style={{
                                                    backgroundColor: 'transparent',
                                                    border: 'none',
                                                    color: '#f44336',
                                                    cursor: 'pointer',
                                                    padding: '0',
                                                    fontSize: '1rem',
                                                    lineHeight: '1'
                                                }}
                                            >
                                                ✕
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
                                <button
                                    type="submit"
                                    style={{
                                        flex: 1,
                                        backgroundColor: '#4caf50',
                                        color: 'white',
                                        padding: '0.75rem',
                                        borderRadius: '4px',
                                        border: 'none',
                                        cursor: 'pointer',
                                        fontWeight: 'bold'
                                    }}
                                >
                                    저장
                                </button>
                                <button
                                    type="button"
                                    onClick={handleCancelEdit}
                                    style={{
                                        flex: 1,
                                        backgroundColor: '#f5f5f5',
                                        color: '#333',
                                        padding: '0.75rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc',
                                        cursor: 'pointer',
                                        fontWeight: 'bold'
                                    }}
                                >
                                    취소
                                </button>
                            </div>
                        </form>
                    </div>
                )}
            </div>
        </div>
    );
};

export default TagListPage;