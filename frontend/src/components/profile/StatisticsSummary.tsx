// src/components/profile/StatisticsSummary.tsx
import React from 'react';
import { UserStatistics } from '../../types/user';

interface StatisticsSummaryProps {
    statistics: UserStatistics;
}

const StatisticsSummary: React.FC<StatisticsSummaryProps> = ({ statistics }) => {
    // 시간 포맷팅 (초 -> 시간:분:초)
    const formatTime = (seconds: number): string => {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;

        return [
            hours > 0 ? `${hours}시간` : '',
            minutes > 0 ? `${minutes}분` : '',
            `${secs}초`
        ].filter(Boolean).join(' ');
    };

    // 정답률 색상 계산
    const getCorrectRateColor = (rate: number): string => {
        if (rate >= 80) return '#4caf50'; // 높음 (초록)
        if (rate >= 60) return '#ff9800'; // 중간 (주황)
        return '#f44336'; // 낮음 (빨강)
    };

    return (
        <div className="statistics-summary" style={{
            padding: '1.5rem',
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            marginBottom: '2rem'
        }}>
            <h2 style={{ margin: '0 0 1.5rem', textAlign: 'center' }}>학습 통계</h2>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                gap: '1.5rem',
            }}>
                {/* 퀴즈 완료 수 */}
                <div className="stat-item" style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '0.25rem' }}>
                        퀴즈 완료
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1976d2' }}>
                        {statistics.totalQuizzesCompleted}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                        총 {statistics.totalQuizzesTaken}개 시도
                    </div>
                </div>

                {/* 평균 점수 */}
                <div className="stat-item" style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '0.25rem' }}>
                        평균 점수
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1976d2' }}>
                        {statistics.averageScore.toFixed(1)}
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                        최고: {statistics.bestScore}, 최저: {statistics.worstScore}
                    </div>
                </div>

                {/* 정답률 */}
                <div className="stat-item" style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '0.25rem' }}>
                        정답률
                    </div>
                    <div style={{
                        fontSize: '2rem',
                        fontWeight: 'bold',
                        color: getCorrectRateColor(statistics.correctRate)
                    }}>
                        {statistics.correctRate.toFixed(1)}%
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                        {statistics.totalCorrectAnswers} / {statistics.totalQuestions} 문제
                    </div>
                </div>

                {/* 총 학습 시간 */}
                <div className="stat-item" style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '0.25rem' }}>
                        총 학습 시간
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1976d2' }}>
                        {Math.floor(statistics.totalTimeTaken / 3600)}h
                    </div>
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>
                        {formatTime(statistics.totalTimeTaken)}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StatisticsSummary;