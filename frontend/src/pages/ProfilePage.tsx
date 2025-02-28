// src/pages/ProfilePage.tsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { userApi } from '../api/userApi';
import { useAuthStore } from '../store/authStore';
import {
    UserProfile,
    UserStatistics,
    RecentActivity,
    Achievement,
    TopicPerformance
} from '../types/user';
import ProfileHeader from '../components/profile/ProfileHeader';
import StatisticsSummary from '../components/profile/StatisticsSummary';
import RecentActivities from '../components/profile/RecentActivities';
import AchievementGallery from '../components/profile/AchievementGallery';
import TopicPerformanceChart from '../components/profile/TopicPerformanceChart';

const ProfilePage: React.FC = () => {
    const { userId } = useParams<{ userId?: string }>();
    const { user: currentUser, isAuthenticated } = useAuthStore();

    const effectiveUserId = userId || '';
    const isOwnProfile = isAuthenticated &&
        (effectiveUserId === '' || (currentUser?.id.toString() === effectiveUserId));

    // 상태 관리
    const [profile, setProfile] = useState<UserProfile | null>(null);
    const [statistics, setStatistics] = useState<UserStatistics | null>(null);
    const [activities, setActivities] = useState<RecentActivity[]>([]);
    const [achievements, setAchievements] = useState<Achievement[]>([]);
    const [topicPerformance, setTopicPerformance] = useState<TopicPerformance[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // 데이터 로드
    useEffect(() => {
        const fetchProfileData = async (): Promise<void> => {
            try {
                setLoading(true);
                const userIdParam = userId ? parseInt(userId) : undefined;

                // 모든 API 요청을 병렬로 실행
                const [
                    profileRes,
                    statsRes,
                    activitiesRes,
                    achievementsRes,
                    topicRes
                ] = await Promise.all([
                    userApi.getUserProfile(userIdParam),
                    userApi.getUserStatistics(userIdParam),
                    userApi.getRecentActivities(userIdParam),
                    userApi.getAchievements(userIdParam),
                    userApi.getTopicPerformance(userIdParam)
                ]);

                // 응답 데이터 설정
                if (profileRes.data.success) {
                    setProfile(profileRes.data.data);
                }

                if (statsRes.data.success) {
                    setStatistics(statsRes.data.data);
                }

                if (activitiesRes.data.success) {
                    setActivities(activitiesRes.data.data);
                }

                if (achievementsRes.data.success) {
                    setAchievements(achievementsRes.data.data);
                }

                if (topicRes.data.success) {
                    setTopicPerformance(topicRes.data.data);
                }
            } catch (err: unknown) {
                console.error('프로필 데이터 로딩 중 오류:', err);

                if (err instanceof Error) {
                    setError(`프로필 정보를 불러오는 중 오류가 발생했습니다: ${err.message}`);
                } else {
                    setError('프로필 정보를 불러오는 중 오류가 발생했습니다.');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchProfileData();
    }, [userId]);

    // 로딩 상태 처리
    if (loading) {
        return (
            <div className="profile-loading" style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '50vh'
            }}>
                <div style={{
                    display: 'inline-block',
                    width: '40px',
                    height: '40px',
                    border: '4px solid rgba(0, 0, 0, 0.1)',
                    borderTopColor: '#1976d2',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite'
                }}></div>
                <style>
                    {`
            @keyframes spin {
              to { transform: rotate(360deg); }
            }
          `}
                </style>
            </div>
        );
    }

    // 에러 상태 처리
    if (error) {
        return (
            <div className="profile-error" style={{
                padding: '2rem',
                backgroundColor: '#ffebee',
                color: '#d32f2f',
                borderRadius: '8px',
                margin: '2rem 0'
            }}>
                <h2>오류가 발생했습니다</h2>
                <p>{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    style={{
                        backgroundColor: '#d32f2f',
                        color: 'white',
                        border: 'none',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '1rem'
                    }}
                >
                    새로고침
                </button>
            </div>
        );
    }

    // 프로필 데이터가 없는 경우
    if (!profile) {
        return (
            <div className="profile-not-found" style={{ textAlign: 'center', padding: '3rem' }}>
                <h2>프로필을 찾을 수 없습니다</h2>
                <p>요청하신 사용자 프로필을 찾을 수 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="profile-page">
            {/* 프로필 헤더 (사용자 정보, 레벨, 경험치) */}
            <ProfileHeader profile={profile} isOwnProfile={isOwnProfile} />

            {/* 통계 요약 */}
            {statistics && (
                <StatisticsSummary statistics={statistics} />
            )}

            <div className="profile-content" style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '2rem',
                marginTop: '2rem'
            }}>
                {/* 주제별 성과 */}
                <div className="profile-section">
                    <h2 style={{ borderBottom: '2px solid #1976d2', paddingBottom: '0.5rem' }}>
                        주제별 성과
                    </h2>
                    <TopicPerformanceChart topicPerformance={topicPerformance} />
                </div>

                {/* 최근 활동 */}
                <div className="profile-section">
                    <h2 style={{ borderBottom: '2px solid #1976d2', paddingBottom: '0.5rem' }}>
                        최근 활동
                    </h2>
                    <RecentActivities activities={activities} />
                </div>
            </div>

            {/* 업적 갤러리 */}
            <div className="profile-section" style={{ marginTop: '2rem' }}>
                <h2 style={{ borderBottom: '2px solid #1976d2', paddingBottom: '0.5rem' }}>
                    획득한 업적
                </h2>
                <AchievementGallery achievements={achievements} />
            </div>
        </div>
    );
};

export default ProfilePage;