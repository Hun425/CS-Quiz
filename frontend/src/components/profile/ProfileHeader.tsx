// src/components/profile/ProfileHeader.tsx
import React, { useState } from 'react';
import { UserProfile } from '../../types/user';
import { userApi } from '../../api/userApi';

interface ProfileHeaderProps {
    profile: UserProfile;
    isOwnProfile: boolean;
}

const ProfileHeader: React.FC<ProfileHeaderProps> = ({ profile, isOwnProfile }) => {
    const [editing, setEditing] = useState(false);
    const [username, setUsername] = useState(profile.username);
    const [profileImage, setProfileImage] = useState(profile.profileImage);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // 경험치 비율 계산 (0-100%)
    const expPercentage = Math.min(
        (profile.experience / profile.requiredExperience) * 100,
        100
    );

    // 프로필 업데이트 제출
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await userApi.updateProfile({
                username,
                profileImage
            });

            if (response.data.success) {
                setEditing(false);
                // 여기서 상태 관리 라이브러리를 통해 프로필 정보를 업데이트해야 할 수도 있습니다.
                // (예: useAuthStore.getState().updateUser({ username, profileImage });)
            } else {
                setError('프로필 업데이트에 실패했습니다.');
            }
        } catch (err) {
            console.error('프로필 업데이트 오류:', err);
            setError('프로필 업데이트 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="profile-header" style={{
            backgroundColor: '#f5f5f5',
            borderRadius: '8px',
            padding: '2rem',
            marginBottom: '2rem'
        }}>
            {editing ? (
                <form onSubmit={handleSubmit}>
                    <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
                        <div style={{ flex: '0 0 100px' }}>
                            <div style={{
                                width: '100px',
                                height: '100px',
                                borderRadius: '50%',
                                overflow: 'hidden',
                                backgroundColor: '#e0e0e0',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                marginBottom: '1rem'
                            }}>
                                {profileImage ? (
                                    <img
                                        src={profileImage}
                                        alt={username}
                                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                    />
                                ) : (
                                    <span style={{ fontSize: '2rem', color: '#666' }}>
                    {username.charAt(0).toUpperCase()}
                  </span>
                                )}
                            </div>

                            <div>
                                <label htmlFor="profile-image" style={{ display: 'block', marginBottom: '0.5rem' }}>
                                    프로필 이미지 URL
                                </label>
                                <input
                                    id="profile-image"
                                    type="text"
                                    value={profileImage || ''}
                                    onChange={(e) => setProfileImage(e.target.value)}
                                    placeholder="이미지 URL"
                                    style={{
                                        width: '100%',
                                        padding: '0.5rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc'
                                    }}
                                />
                            </div>
                        </div>

                        <div style={{ flex: '1' }}>
                            <div style={{ marginBottom: '1rem' }}>
                                <label htmlFor="username" style={{ display: 'block', marginBottom: '0.5rem' }}>
                                    사용자명
                                </label>
                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="사용자명"
                                    style={{
                                        width: '100%',
                                        padding: '0.5rem',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc'
                                    }}
                                    required
                                />
                            </div>

                            <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        backgroundColor: '#1976d2',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {loading ? '저장 중...' : '저장'}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setEditing(false);
                                        setUsername(profile.username);
                                        setProfileImage(profile.profileImage);
                                    }}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        backgroundColor: 'white',
                                        color: '#666',
                                        border: '1px solid #ccc',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    취소
                                </button>
                            </div>

                            {error && (
                                <p style={{ color: '#f44336', marginTop: '1rem' }}>{error}</p>
                            )}
                        </div>
                    </div>
                </form>
            ) : (
                <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
                    <div style={{ flex: '0 0 100px' }}>
                        <div style={{
                            width: '100px',
                            height: '100px',
                            borderRadius: '50%',
                            overflow: 'hidden',
                            backgroundColor: '#e0e0e0',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {profile.profileImage ? (
                                <img
                                    src={profile.profileImage}
                                    alt={profile.username}
                                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                />
                            ) : (
                                <span style={{ fontSize: '2rem', color: '#666' }}>
                  {profile.username.charAt(0).toUpperCase()}
                </span>
                            )}
                        </div>
                    </div>

                    <div style={{ flex: '1' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                                <h1 style={{ margin: '0 0 0.5rem' }}>{profile.username}</h1>
                                <p style={{ margin: '0 0 1rem', color: '#666' }}>{profile.email}</p>
                            </div>

                            {isOwnProfile && (
                                <button
                                    onClick={() => setEditing(true)}
                                    style={{
                                        padding: '0.5rem 1rem',
                                        backgroundColor: 'white',
                                        color: '#1976d2',
                                        border: '1px solid #1976d2',
                                        borderRadius: '4px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    프로필 수정
                                </button>
                            )}
                        </div>

                        <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem', flexWrap: 'wrap' }}>
                            <div>
                                <h3 style={{ margin: '0 0 0.25rem', fontSize: '1rem', color: '#666' }}>레벨</h3>
                                <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                    {profile.level}
                                </p>
                            </div>

                            <div>
                                <h3 style={{ margin: '0 0 0.25rem', fontSize: '1rem', color: '#666' }}>총 포인트</h3>
                                <p style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                                    {profile.totalPoints.toLocaleString()}
                                </p>
                            </div>

                            <div>
                                <h3 style={{ margin: '0 0 0.25rem', fontSize: '1rem', color: '#666' }}>가입일</h3>
                                <p style={{ margin: 0, fontSize: '1rem' }}>
                                    {new Date(profile.joinedAt).toLocaleDateString()}
                                </p>
                            </div>
                        </div>

                        <div style={{ marginTop: '1.5rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.9rem' }}>
                  경험치: {profile.experience} / {profile.requiredExperience}
                </span>
                                <span style={{ fontSize: '0.9rem' }}>
                  다음 레벨까지: {profile.requiredExperience - profile.experience}
                </span>
                            </div>
                            <div style={{
                                width: '100%',
                                height: '10px',
                                backgroundColor: '#e0e0e0',
                                borderRadius: '5px',
                                overflow: 'hidden'
                            }}>
                                <div style={{
                                    width: `${expPercentage}%`,
                                    height: '100%',
                                    backgroundColor: '#1976d2',
                                    borderRadius: '5px'
                                }}></div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfileHeader;
