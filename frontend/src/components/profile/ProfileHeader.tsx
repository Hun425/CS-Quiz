// src/components/profile/ProfileHeader.tsx
import React, { useState } from 'react';
import { UserProfile } from '../../types/user';
import { userApi } from '../../api/userApi';

interface ProfileHeaderProps {
    profile: UserProfile;
    isOwnProfile?: boolean;
}

const ProfileHeader: React.FC<ProfileHeaderProps> = ({ profile, isOwnProfile }) => {
    const [editing, setEditing] = useState<boolean>(false);
    const [username, setUsername] = useState<string>(profile.username);
    const [saving, setSaving] = useState<boolean>(false);

    // 경험치 백분율 계산
    const experiencePercentage = Math.min(
        Math.round((profile.experience / profile.requiredExperience) * 100),
        100
    );

    // 프로필 수정 처리
    const handleSaveProfile = async (): Promise<void> => {
        if (!isOwnProfile) return;

        try {
            setSaving(true);
            await userApi.updateProfile({ username });
            setEditing(false);
        } catch (err) {
            console.error('프로필 저장 중 오류:', err);
        } finally {
            setSaving(false);
        }
    };

    // 가입일 포맷팅
    const formatJoinDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className="profile-header" style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            padding: '2rem',
            backgroundColor: '#f5f5f5',
            borderRadius: '8px',
            marginBottom: '2rem'
        }}>
            {/* 프로필 이미지 및 기본 정보 */}
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1.5rem' }}>
                {profile.profileImage ? (
                    <img
                        src={profile.profileImage}
                        alt={profile.username}
                        style={{
                            width: '120px',
                            height: '120px',
                            borderRadius: '50%',
                            objectFit: 'cover',
                            border: '4px solid white',
                            boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                        }}
                    />
                ) : (
                    <div style={{
                        width: '120px',
                        height: '120px',
                        borderRadius: '50%',
                        backgroundColor: '#1976d2',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '3rem',
                        fontWeight: 'bold',
                        border: '4px solid white',
                        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
                    }}>
                        {profile.username.charAt(0).toUpperCase()}
                    </div>
                )}

                <div style={{ marginLeft: '2rem' }}>
                    {editing ? (
                        <div>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                style={{
                                    fontSize: '1.5rem',
                                    padding: '0.5rem',
                                    borderRadius: '4px',
                                    border: '1px solid #ccc',
                                    marginBottom: '0.5rem'
                                }}
                            />
                            <div>
                                <button
                                    onClick={handleSaveProfile}
                                    disabled={saving}
                                    style={{
                                        backgroundColor: '#1976d2',
                                        color: 'white',
                                        border: 'none',
                                        padding: '0.5rem 1rem',
                                        borderRadius: '4px',
                                        cursor: saving ? 'not-allowed' : 'pointer',
                                        marginRight: '0.5rem'
                                    }}
                                >
                                    {saving ? '저장 중...' : '저장'}
                                </button>
                                <button
                                    onClick={() => {
                                        setUsername(profile.username);
                                        setEditing(false);
                                    }}
                                    style={{
                                        backgroundColor: '#f5f5f5',
                                        border: '1px solid #ccc',
                                        padding: '0.5rem 1rem',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    취소
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            <h1 style={{ margin: '0 0 0.5rem', fontSize: '2rem' }}>
                                {profile.username}
                                {isOwnProfile && (
                                    <button
                                        onClick={() => setEditing(true)}
                                        style={{
                                            backgroundColor: 'transparent',
                                            border: 'none',
                                            fontSize: '0.9rem',
                                            color: '#666',
                                            marginLeft: '0.5rem',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        ✏️ 수정
                                    </button>
                                )}
                            </h1>
                            <p style={{ margin: '0 0 0.5rem', color: '#666' }}>
                                가입일: {formatJoinDate(profile.joinedAt)}
                            </p>
                            <p style={{ margin: 0, color: '#666' }}>
                                이메일: {profile.email}
                            </p>
                        </>
                    )}
                </div>
            </div>

            {/* 레벨 및 경험치 표시 */}
            <div style={{ width: '100%', maxWidth: '600px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <div style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>
                        레벨 {profile.level}
                    </div>
                    <div>
                        {profile.experience} / {profile.requiredExperience} XP
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
                        width: `${experiencePercentage}%`,
                        height: '100%',
                        backgroundColor: '#4caf50',
                        transition: 'width 0.3s ease-in-out'
                    }}></div>
                </div>
                <div style={{
                    textAlign: 'center',
                    marginTop: '0.5rem',
                    fontSize: '0.9rem',
                    color: '#666'
                }}>
                    다음 레벨까지 {profile.requiredExperience - profile.experience} XP 남음
                </div>
            </div>

            {/* 포인트 배지 */}
            <div style={{
                backgroundColor: '#1976d2',
                color: 'white',
                borderRadius: '20px',
                padding: '0.5rem 1rem',
                fontWeight: 'bold',
                marginTop: '1rem'
            }}>
                총 {profile.totalPoints.toLocaleString()} 포인트 획득
            </div>
        </div>
    );
};

export default ProfileHeader;