// src/components/profile/StatisticsSummary.tsx
import React from 'react';
import { UserStatistics } from '../../types/user';

interface StatisticsSummaryProps {
    statistics: UserStatistics;
}

const StatisticsSummary: React.FC<StatisticsSummaryProps> = ({ statistics }) => {
    return (
        <div className="statistics-summary" style={{
            backgroundColor: 'white',
            borderRadius: '8px',
            border: '1px solid #e0e0e0',
            padding: '1.5rem',
            marginBottom: '2rem'
        }}>
            <h2 style={{ marginTop: 0, borderBottom: '2px solid #1976d2', paddingBottom: '0.5rem' }}>
                통계 요약
            </h2>

            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                gap: '1.5rem',
                marginTop: '1rem'
            }}>
                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>퀴즈 참여</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                        {statistics.totalQuizzesTaken}회
                    </p>
                </div>

                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>완료한 퀴즈</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                        {statistics.totalQuizzesCompleted}회
                    </p>
                </div>

                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>평균 점수</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                        {statistics.averageScore.toFixed(1)}점
                    </p>
                </div>

                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>정답률</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                        {statistics.correctRate.toFixed(1)}%
                    </p>
                    <p style={{ margin: '0.25rem 0 0', fontSize: '0.8rem', color: '#666' }}>
                        ({statistics.totalCorrectAnswers}/{statistics.totalQuestions})
                    </p>
                </div>

                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>최고 점수</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold', color: '#4caf50' }}>
                        {statistics.bestScore}점
                    </p>
                </div>

                <div>
                    <h3 style={{ margin: '0 0 0.5rem', fontSize: '1rem', color: '#666' }}>최저 점수</h3>
                    <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold', color: '#f44336' }}>
                        {statistics.worstScore}점
                    </p>
                </div>
            </div>
        </div>
    );
};

export default StatisticsSummary;