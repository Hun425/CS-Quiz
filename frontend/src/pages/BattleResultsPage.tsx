// src/pages/BattleResultsPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { battleApi } from '../api/battleApi';
import { BattleEndResponse } from '../types/battle';

/**
 * 배틀 결과 페이지 컴포넌트
 * 배틀 종료 후 최종 결과와 통계를 보여줍니다.
 */
const BattleResultsPage: React.FC = () => {
    const { roomId } = useParams<{ roomId: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const { isAuthenticated } = useAuthStore();

    // 결과 데이터는 location.state에서 가져옵니다 (배틀룸에서 전달)
    const [result, setResult] = useState<BattleEndResponse | null>(
        location.state?.result || null
    );

    const [loading, setLoading] = useState<boolean>(!location.state?.result);
    const [error, setError] = useState<string | null>(null);

    // 자동 새로고침 기능 추가 - 컴포넌트 최상위에 추가
    useEffect(() => {
        // 결과가 없을 때만 자동 새로고침 설정
        if (isAuthenticated && roomId && !result && !loading && !error) {
            const intervalId = setInterval(() => {
                console.log('자동으로 배틀 결과 새로고침 시도');
                fetchBattleResult();
            }, 5000); // 5초마다 새로고침

            return () => clearInterval(intervalId);
        }
    }, [isAuthenticated, roomId, result, loading, error]);

    // 인증 확인
    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login', { state: { from: location.pathname } });
            return;
        }

        // location.state에 결과가 없을 경우 API 요청으로 가져옵니다
        if (!result && roomId) {
            fetchBattleResult();
        }
    }, [isAuthenticated, roomId, result]);

    // fetchBattleResult 함수 개선
    const fetchBattleResult = async () => {
        if (!roomId) return;

        try {
            setLoading(true);
            console.log('배틀 결과 불러오기 시도:', roomId);

            // 먼저 배틀룸 상태 확인
            const roomResponse = await battleApi.getBattleRoom(parseInt(roomId));

            if (roomResponse.data.success) {
                const roomStatus = roomResponse.data.data.status;
                console.log('배틀룸 상태:', roomStatus);

                // 배틀이 아직 진행 중인 경우
                if (roomStatus === 'IN_PROGRESS') {
                    setError('배틀이 아직 진행 중입니다. 배틀이 종료된 후 결과를 확인할 수 있습니다.');
                    setLoading(false);
                    return;
                }

                // 배틀이 종료된 경우에만 결과 조회
                if (roomStatus === 'FINISHED') {
                    try {
                        // 배틀 결과 API 호출
                        const response = await battleApi.getBattleResult(parseInt(roomId));

                        if (response.data.success) {
                            setResult(response.data.data);
                            setError(null);
                        } else {
                            // 결과를 찾을 수 없지만 배틀은 종료됨
                            setError('배틀은 종료되었으나 결과 데이터를 찾을 수 없습니다. 잠시 후 다시 시도해주세요.');
                        }
                    } catch (err: any) {
                        console.error('결과 로딩 중 오류:', err);

                        // 더 구체적인 오류 메시지 제공
                        if (err.response?.status === 404) {
                            setError('결과 데이터가 아직 생성되지 않았습니다. 잠시 후 다시 시도해주세요.');
                        } else {
                            setError('결과를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                        }
                    }
                } else {
                    setError(`예상치 못한 배틀 상태: ${roomStatus}`);
                }
            } else {
                setError('배틀룸 정보를 불러오는 데 실패했습니다: ' + roomResponse.data.message);
            }
        } catch (err: any) {
            console.error('결과 로딩 중 오류:', err);
            setError('결과를 불러오는 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 로딩 상태 UI
    if (loading) {
        return (
            <div className="loading-container" style={{
                textAlign: 'center',
                padding: '2rem',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '50vh'
            }}>
                <div style={{
                    width: '50px',
                    height: '50px',
                    border: '5px solid #f3f3f3',
                    borderTop: '5px solid #1976d2',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite',
                    marginBottom: '1rem'
                }}></div>
                <p style={{ fontSize: '1.2rem' }}>결과를 불러오는 중...</p>
                <style>{`
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                `}</style>
            </div>
        );
    }

    // 에러 상태 UI
    if (error) {
        return (
            <div className="error-container" style={{
                backgroundColor: '#ffebee',
                color: '#d32f2f',
                padding: '2rem',
                borderRadius: '8px',
                marginBottom: '1rem',
                maxWidth: '600px',
                margin: '2rem auto',
                textAlign: 'center',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h2 style={{ marginTop: 0 }}>오류가 발생했습니다</h2>
                <p style={{ marginBottom: '1.5rem' }}>{error}</p>
                <button onClick={() => navigate('/battles')} style={{
                    backgroundColor: '#1976d2',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    fontWeight: 'bold',
                    boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                }}>
                    배틀 목록으로 돌아가기
                </button>
            </div>
        );
    }

    // 결과 없음 UI
    if (!result) {
        return (
            <div className="not-found-container" style={{
                textAlign: 'center',
                padding: '2rem',
                maxWidth: '600px',
                margin: '2rem auto',
                backgroundColor: '#f5f5f5',
                borderRadius: '8px',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h2 style={{ marginTop: 0 }}>결과를 불러오는 중...</h2>
                <p style={{ marginBottom: '1rem' }}>배틀 결과를 불러오고 있습니다. 잠시만 기다려주세요.</p>
                <p style={{ marginBottom: '1.5rem', color: '#666' }}>
                    만약 결과가 계속 표시되지 않는다면, 배틀이 아직 진행 중이거나 서버에서 결과를 처리하는 중일 수 있습니다.
                </p>

                <div style={{ marginBottom: '2rem' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        border: '4px solid #f3f3f3',
                        borderTop: '4px solid #1976d2',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        margin: '0 auto 1rem'
                    }}></div>
                    <p>잠시 후 자동으로 결과가 표시됩니다...</p>
                </div>

                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                    <button onClick={() => navigate('/battles')} style={{
                        backgroundColor: '#f44336',
                        color: 'white',
                        border: 'none',
                        padding: '0.75rem 1.5rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '1rem',
                        fontWeight: 'bold',
                        boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                    }}>
                        배틀 목록으로 돌아가기
                    </button>

                    <button onClick={fetchBattleResult} style={{
                        backgroundColor: '#1976d2',
                        color: 'white',
                        border: 'none',
                        padding: '0.75rem 1.5rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '1rem',
                        fontWeight: 'bold',
                        boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
                    }}>
                        결과 다시 불러오기
                    </button>
                </div>

                <style>{`
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `}</style>
            </div>
        );
    }


    // 시간 형식 변환 (초 -> 분:초)
    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}분 ${remainingSeconds}초`;
    };

    // 날짜 형식 변환
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString();
    };

    // 결과 UI
    return (
        <div className="battle-results-page" style={{
            maxWidth: '1000px',
            margin: '0 auto',
            padding: '2rem 1rem'
        }}>
            {/* 결과 헤더 */}
            <div style={{
                backgroundColor: '#1976d2',
                color: 'white',
                padding: '2rem',
                borderRadius: '8px',
                marginBottom: '2rem',
                textAlign: 'center',
                boxShadow: '0 4px 15px rgba(0,0,0,0.15)'
            }}>
                <h1 style={{ margin: '0 0 1rem', fontSize: '2rem' }}>대결 결과</h1>
                <p style={{ margin: '0', fontSize: '1.2rem', opacity: 0.9 }}>
                    총 {result.totalQuestions}문제 • {formatTime(result.timeTakenSeconds)} 소요
                </p>
                <p style={{ margin: '0.5rem 0 0', fontSize: '0.9rem', opacity: 0.8 }}>
                    완료 시간: {formatDate(result.endTime)}
                </p>
            </div>

            {/* 최종 순위 */}
            <div className="results-summary" style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '8px',
                marginBottom: '2rem',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h2 style={{
                    borderBottom: '2px solid #1976d2',
                    paddingBottom: '0.5rem',
                    marginBottom: '1.5rem'
                }}>최종 순위</h2>

                {/* 우승자 표시 */}
                {result.results.filter(p => p.isWinner).length > 0 && (
                    <div className="winner-section" style={{
                        backgroundColor: '#fff9c4',
                        padding: '1.5rem',
                        borderRadius: '8px',
                        marginBottom: '2rem',
                        textAlign: 'center',
                        position: 'relative',
                        overflow: 'hidden'
                    }}>
                        <div style={{
                            position: 'absolute',
                            top: '0',
                            left: '0',
                            width: '100%',
                            height: '5px',
                            background: 'linear-gradient(to right, #ffd700, #ffeb3b, #ffd700)'
                        }}></div>

                        <h3 style={{ margin: '0 0 1rem', color: '#ff6f00' }}>
                            <span style={{ fontSize: '1.5rem', marginRight: '0.5rem' }}>🏆</span>
                            우승자
                        </h3>

                        {result.results.filter(p => p.isWinner).map(winner => (
                            <div key={winner.userId} style={{ marginBottom: '1rem' }}>
                                <p style={{
                                    margin: '0 0 0.5rem',
                                    fontSize: '1.5rem',
                                    fontWeight: 'bold',
                                    color: '#ff6f00'
                                }}>
                                    {winner.username}
                                </p>
                                <p style={{ margin: '0', fontSize: '1.2rem' }}>
                                    점수: <strong>{winner.finalScore}점</strong> •
                                    정답률: <strong>{Math.round((winner.correctAnswers / result.totalQuestions) * 100)}%</strong>
                                </p>
                            </div>
                        ))}

                        <p style={{
                            margin: '1rem 0 0',
                            color: '#795548'
                        }}>
                            <span style={{ marginRight: '0.5rem' }}>✨</span>
                            경험치 {result.results.find(p => p.isWinner)?.experienceGained || 0}점을 획득했습니다!
                        </p>
                    </div>
                )}

                {/* 참가자 순위 표 */}
                <div className="participants-ranking">
                    {/* 테이블 헤더 */}
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: '50px 2fr 1fr 1fr 1fr',
                        backgroundColor: '#f5f5f5',
                        padding: '0.75rem 1rem',
                        borderRadius: '8px 8px 0 0',
                        fontWeight: 'bold',
                        color: '#666'
                    }}>
                        <div>순위</div>
                        <div>이름</div>
                        <div style={{ textAlign: 'center' }}>점수</div>
                        <div style={{ textAlign: 'center' }}>정답</div>
                        <div style={{ textAlign: 'center' }}>경험치</div>
                    </div>

                    {/* 테이블 내용 */}
                    {result.results
                        .sort((a, b) => b.finalScore - a.finalScore)
                        .map((participant, index) => (
                            <div
                                key={participant.userId}
                                style={{
                                    display: 'grid',
                                    gridTemplateColumns: '50px 2fr 1fr 1fr 1fr',
                                    padding: '1rem',
                                    borderBottom: '1px solid #e0e0e0',
                                    backgroundColor: participant.isWinner ? '#fff8e1' : 'white',
                                }}
                            >
                                <div style={{
                                    fontWeight: 'bold',
                                    color: index === 0 ? '#ff6f00' : '#666'
                                }}>
                                    {index + 1}
                                </div>
                                <div style={{
                                    fontWeight: 'bold',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '0.5rem'
                                }}>
                                    {participant.isWinner && <span style={{ fontSize: '1.2rem' }}>🏆</span>}
                                    {participant.username}
                                </div>
                                <div style={{
                                    textAlign: 'center',
                                    fontWeight: 'bold',
                                    color: '#1976d2'
                                }}>
                                    {participant.finalScore}
                                </div>
                                <div style={{ textAlign: 'center' }}>
                                    {participant.correctAnswers}/{result.totalQuestions}
                                </div>
                                <div style={{
                                    textAlign: 'center',
                                    color: '#4caf50',
                                    fontWeight: 'bold'
                                }}>
                                    +{participant.experienceGained}
                                </div>
                            </div>
                        ))}
                </div>
            </div>

            {/* 문제별 결과 (선택적) */}
            {result.results.length > 0 && result.results[0].questionResults && (
                <div className="question-results" style={{
                    backgroundColor: 'white',
                    padding: '1.5rem',
                    borderRadius: '8px',
                    marginBottom: '2rem',
                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                }}>
                    <h3 style={{
                        borderBottom: '2px solid #1976d2',
                        paddingBottom: '0.5rem',
                        marginBottom: '1.5rem'
                    }}>문제별 결과</h3>

                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
                        gap: '1rem'
                    }}>
                        {result.results[0].questionResults?.map((question, index) => (
                            <div key={question.questionId} style={{
                                backgroundColor: '#f5f5f5',
                                padding: '1rem',
                                borderRadius: '8px',
                                border: '1px solid #e0e0e0'
                            }}>
                                <h4 style={{ margin: '0 0 0.5rem' }}>문제 {index + 1}</h4>

                                <div style={{
                                    display: 'flex',
                                    flexWrap: 'wrap',
                                    gap: '0.5rem',
                                    marginTop: '1rem'
                                }}>
                                    {result.results.map(participant => {
                                        const participantResult = participant.questionResults?.find(
                                            q => q.questionId === question.questionId
                                        );

                                        return participantResult ? (
                                            <div key={participant.userId} style={{
                                                flex: '1',
                                                minWidth: '120px',
                                                backgroundColor: 'white',
                                                padding: '0.5rem',
                                                borderRadius: '4px',
                                                border: `1px solid ${participantResult.isCorrect ? '#4caf50' : '#f44336'}`
                                            }}>
                                                <div style={{ fontWeight: 'bold' }}>{participant.username}</div>
                                                <div style={{
                                                    color: participantResult.isCorrect ? '#4caf50' : '#f44336',
                                                    fontSize: '0.9rem'
                                                }}>
                                                    {participantResult.isCorrect ? '정답' : '오답'} • {participantResult.earnedPoints}점
                                                </div>
                                                <div style={{ fontSize: '0.8rem', color: '#666' }}>
                                                    {participantResult.timeSpentSeconds}초 소요
                                                </div>
                                            </div>
                                        ) : null;
                                    })}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* 액션 버튼 */}
            <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '2rem' }}>
                <button
                    onClick={() => navigate('/battles')}
                    style={{
                        padding: '0.75rem 1.5rem',
                        backgroundColor: '#1976d2',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        fontSize: '1rem',
                        cursor: 'pointer',
                        boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.5rem'
                    }}
                >
                    <span>배틀 목록으로</span>
                    <span style={{ fontSize: '1.2rem' }}>→</span>
                </button>
            </div>
        </div>
    );
};

export default BattleResultsPage;