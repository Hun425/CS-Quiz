// src/components/profile/RecentActivities.tsx
import React from 'react';
import { Link } from 'react-router-dom';
import { RecentActivity } from '../../types/user';

interface RecentActivitiesProps {
    activities: RecentActivity[];
}

const RecentActivities: React.FC<RecentActivitiesProps> = ({ activities }) => {
    // 날짜 포맷팅
    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        const now = new Date();
        const diffMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

        if (diffMinutes < 1) return '방금 전';
        if (diffMinutes < 60) return `${diffMinutes}분 전`;

        const diffHours = Math.floor(diffMinutes / 60);
        if (diffHours < 24) return `${diffHours}시간 전`;

        const diffDays = Math.floor(diffHours / 24);
        if (diffDays < 7) return `${diffDays}일 전`;

        return date.toLocaleDateString('ko-KR');
    };

    // 활동 타입별 아이콘
    const getActivityIcon = (type: string): string => {
        switch (type) {
            case 'QUIZ_ATTEMPT': return '📝';
            case 'ACHIEVEMENT_EARNED': return '🏆';
            case 'LEVEL_UP': return '⭐';
            default: return '📌';
        }
    };

    // 활동 타입별 배경색
    const getActivityColor = (type: string): string => {
        switch (type) {
            case 'QUIZ_ATTEMPT': return '#e3f2fd';
            case 'ACHIEVEMENT_EARNED': return '#f9fbe7';
            case 'LEVEL_UP': return '#e8f5e9';
            default: return '#f5f5f5';
        }
    };

    // 활동 내용 포맷팅
    const formatActivityContent = (activity: RecentActivity): React.ReactNode => {
        switch (activity.type) {
            case 'QUIZ_ATTEMPT':
                return (
                    <>
                        <Link
                            to={`/quizzes/${activity.quizId}`}
                            style={{ color: '#1976d2', fontWeight: 'bold', textDecoration: 'none' }}
                        >
                            {activity.quizTitle}
                        </Link>
                        {' 퀴즈를 완료했습니다. '}
                        {activity.score !== undefined && (
                            <span style={{ fontWeight: 'bold' }}>
                {activity.score}점 획득
              </span>
                        )}
                    </>
                );

            case 'ACHIEVEMENT_EARNED':
                return (
                    <>
            <span style={{ fontWeight: 'bold' }}>
              {activity.achievementName}
            </span>
                        {' 업적을 획득했습니다!'}
                    </>
                );

            case 'LEVEL_UP':
                return (
                    <>
                        {'레벨 업! 이제 '}
                        <span style={{ fontWeight: 'bold' }}>
              레벨 {activity.newLevel}
            </span>
                        {' 입니다.'}
                    </>
                );

            default:
                return '알 수 없는 활동';
        }
    };

    return (
        <div className="recent-activities">
            {activities.length > 0 ? (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    {activities.map((activity) => (
                        <li
                            key={activity.id}
                            style={{
                                padding: '1rem',
                                backgroundColor: getActivityColor(activity.type),
                                borderRadius: '8px',
                                marginBottom: '0.75rem',
                                position: 'relative'
                            }}
                        >
                            <div style={{ display: 'flex', alignItems: 'flex-start' }}>
                                <div style={{
                                    fontSize: '1.5rem',
                                    marginRight: '0.75rem',
                                    marginTop: '0.1rem'
                                }}>
                                    {getActivityIcon(activity.type)}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ marginBottom: '0.25rem' }}>
                                        {formatActivityContent(activity)}
                                    </div>
                                    <div style={{ fontSize: '0.8rem', color: '#666' }}>
                                        {formatDate(activity.timestamp)}
                                    </div>
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            ) : (
                <div style={{
                    textAlign: 'center',
                    padding: '2rem',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '8px'
                }}>
                    <p>최근 활동이 없습니다.</p>
                </div>
            )}
        </div>
    );
};

export default RecentActivities;