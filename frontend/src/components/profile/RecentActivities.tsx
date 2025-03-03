// src/components/profile/RecentActivities.tsx
import React from 'react';
import { RecentActivity } from '../../types/user';
import { Link } from 'react-router-dom';

interface RecentActivitiesProps {
    activities: RecentActivity[];
}

const RecentActivities: React.FC<RecentActivitiesProps> = ({ activities }) => {
    // 활동 유형에 따른 아이콘 및 색상
    const getActivityIcon = (type: string) => {
        switch (type) {
            case 'QUIZ_ATTEMPT':
                return '📝';
            case 'ACHIEVEMENT_EARNED':
                return '🏆';
            case 'LEVEL_UP':
                return '⬆️';
            default:
                return '🔔';
        }
    };

    const getActivityColor = (type: string) => {
        switch (type) {
            case 'QUIZ_ATTEMPT':
                return '#1976d2';
            case 'ACHIEVEMENT_EARNED':
                return '#f57c00';
            case 'LEVEL_UP':
                return '#4caf50';
            default:
                return '#757575';
        }
    };

    // 활동 내용 텍스트 생성
    const getActivityText = (activity: RecentActivity) => {
        switch (activity.type) {
            case 'QUIZ_ATTEMPT':
                return (
                    <>
                        <Link to={`/quizzes/${activity.quizId}`} style={{ color: '#1976d2', textDecoration: 'none', fontWeight: 'bold' }}>
                            {activity.quizTitle}
                        </Link>
                        <span> 퀴즈를 완료했습니다. 점수: </span>
                        <strong>{activity.score}점</strong>
                    </>
                );
            case 'ACHIEVEMENT_EARNED':
                return (
                    <>
                        <span>업적 </span>
                        <strong>{activity.achievementName}</strong>
                        <span>을(를) 획득했습니다!</span>
                    </>
                );
            case 'LEVEL_UP':
                return (
                    <>
                        <span>레벨이 </span>
                        <strong>{activity.newLevel}레벨</strong>
                        <span>로 올랐습니다!</span>
                    </>
                );
            default:
                return <span>알 수 없는 활동</span>;
        }
    };

    // 날짜 포맷팅
    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    };

    return (
        <div className="recent-activities">
            {activities.length === 0 ? (
                <p style={{ padding: '1rem', backgroundColor: '#f5f5f5', borderRadius: '8px', textAlign: 'center' }}>
                    최근 활동이 없습니다.
                </p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                    {activities.map((activity) => (
                        <li key={activity.id} style={{
                            borderBottom: '1px solid #e0e0e0',
                            padding: '1rem 0',
                            display: 'flex',
                            gap: '1rem',
                            alignItems: 'flex-start'
                        }}>
                            <div style={{
                                width: '40px',
                                height: '40px',
                                borderRadius: '50%',
                                backgroundColor: getActivityColor(activity.type),
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '1.2rem'
                            }}>
                                {getActivityIcon(activity.type)}
                            </div>

                            <div style={{ flex: '1' }}>
                                <div style={{ marginBottom: '0.25rem' }}>
                                    {getActivityText(activity)}
                                </div>
                                <div style={{ fontSize: '0.8rem', color: '#666' }}>
                                    {formatDate(activity.timestamp)}
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default RecentActivities;