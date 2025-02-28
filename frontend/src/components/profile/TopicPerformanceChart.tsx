// src/components/profile/TopicPerformanceChart.tsx
import React from 'react';
import { TopicPerformance } from '../../types/user';

interface TopicPerformanceChartProps {
    topicPerformance: TopicPerformance[];
}

const TopicPerformanceChart: React.FC<TopicPerformanceChartProps> = ({ topicPerformance }) => {
    // 강점 주제와 약점 주제 분리
    const strengths = topicPerformance.filter(topic => topic.strength);
    const weaknesses = topicPerformance.filter(topic => !topic.strength);

    // 정답률에 따른 색상 계산
    const getBarColor = (rate: number, isStrength: boolean): string => {
        if (isStrength) {
            if (rate >= 80) return '#4caf50';
            if (rate >= 70) return '#8bc34a';
            return '#cddc39';
        } else {
            if (rate <= 40) return '#f44336';
            if (rate <= 60) return '#ff9800';
            return '#ffeb3b';
        }
    };

    // 주제별 성과 막대 렌더링
    const renderPerformanceBars = (topics: TopicPerformance[], isStrength: boolean) => {
        return topics.map((topic) => (
            <div key={topic.tagId} style={{ marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                    <div>{topic.tagName}</div>
                    <div>
                        {topic.averageScore.toFixed(1)}점 (정답률 {topic.correctRate.toFixed(1)}%)
                    </div>
                </div>
                <div style={{
                    width: '100%',
                    height: '12px',
                    backgroundColor: '#e0e0e0',
                    borderRadius: '6px',
                    overflow: 'hidden'
                }}>
                    <div style={{
                        width: `${topic.correctRate}%`,
                        height: '100%',
                        backgroundColor: getBarColor(topic.correctRate, isStrength),
                        transition: 'width 0.3s ease-in-out'
                    }}></div>
                </div>
                <div style={{ fontSize: '0.8rem', color: '#666', marginTop: '0.25rem' }}>
                    총 {topic.quizzesTaken}개 퀴즈 시도
                </div>
            </div>
        ));
    };

    return (
        <div className="topic-performance">
            {topicPerformance.length > 0 ? (
                <>
                    {/* 강점 주제 */}
                    {strengths.length > 0 && (
                        <div style={{ marginBottom: '2rem' }}>
                            <h3 style={{
                                color: '#388e3c',
                                display: 'flex',
                                alignItems: 'center',
                                marginBottom: '1rem'
                            }}>
                                <span style={{ marginRight: '0.5rem' }}>💪</span>
                                강점 주제
                            </h3>
                            {renderPerformanceBars(strengths, true)}
                        </div>
                    )}

                    {/* 약점 주제 */}
                    {weaknesses.length > 0 && (
                        <div>
                            <h3 style={{
                                color: '#d32f2f',
                                display: 'flex',
                                alignItems: 'center',
                                marginBottom: '1rem'
                            }}>
                                <span style={{ marginRight: '0.5rem' }}>📚</span>
                                개선 필요 주제
                            </h3>
                            {renderPerformanceBars(weaknesses, false)}
                        </div>
                    )}

                    {/* 학습 팁 */}
                    {weaknesses.length > 0 && (
                        <div style={{
                            marginTop: '1.5rem',
                            padding: '1rem',
                            backgroundColor: '#fff9c4',
                            borderRadius: '8px',
                            borderLeft: '4px solid #fbc02d'
                        }}>
                            <h4 style={{ margin: '0 0 0.5rem', color: '#f57f17' }}>학습 팁</h4>
                            <p style={{ margin: 0, fontSize: '0.9rem' }}>
                                개선이 필요한 주제에 더 많은 퀴즈를 풀어보세요.
                                틀린 문제는 복습하고, 관련 자료를 참고하면 실력 향상에 도움이 됩니다.
                            </p>
                        </div>
                    )}
                </>
            ) : (
                <div style={{
                    textAlign: 'center',
                    padding: '2rem',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '8px'
                }}>
                    <p>아직 충분한 퀴즈 데이터가 없습니다. 더 많은 퀴즈를 풀어보세요!</p>
                </div>
            )}
        </div>
    );
};

export default TopicPerformanceChart;