// src/components/profile/AchievementGallery.tsx
import React, { useState } from 'react';
import { Achievement } from '../../types/user';

interface AchievementGalleryProps {
    achievements: Achievement[];
}

const AchievementGallery: React.FC<AchievementGalleryProps> = ({ achievements }) => {
    const [selectedAchievement, setSelectedAchievement] = useState<Achievement | null>(null);

    // 업적 획득 여부에 따른 스타일 처리
    const getAchievementStyle = (achievement: Achievement) => {
        return {
            opacity: achievement.earnedAt ? 1 : 0.4,
            filter: achievement.earnedAt ? 'none' : 'grayscale(80%)',
            cursor: 'pointer',
            transition: 'transform 0.2s, box-shadow 0.2s',
            transform: selectedAchievement?.id === achievement.id ? 'scale(1.05)' : 'scale(1)',
            boxShadow: selectedAchievement?.id === achievement.id
                ? '0 4px 12px rgba(0,0,0,0.15)'
                : '0 2px 4px rgba(0,0,0,0.1)'
        };
    };

    // 날짜 포맷팅
    const formatDate = (dateString: string | null): string => {
        if (!dateString) return '미획득';

        return new Date(dateString).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    return (
        <div className="achievement-gallery">
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
                gap: '1.5rem',
                marginBottom: selectedAchievement ? '2rem' : 0
            }}>
                {achievements.map((achievement) => (
                    <div
                        key={achievement.id}
                        className="achievement-item"
                        style={{
                            textAlign: 'center',
                            padding: '1rem',
                            backgroundColor: 'white',
                            borderRadius: '8px',
                            border: selectedAchievement?.id === achievement.id ? '2px solid #1976d2' : '1px solid #e0e0e0',
                            ...getAchievementStyle(achievement)
                        }}
                        onClick={() => setSelectedAchievement(
                            selectedAchievement?.id === achievement.id ? null : achievement
                        )}
                    >
                        <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>
                            {achievement.iconUrl ? (
                                <img
                                    src={achievement.iconUrl}
                                    alt={achievement.name}
                                    style={{ width: '64px', height: '64px' }}
                                />
                            ) : (
                                '🏆'
                            )}
                        </div>
                        <div style={{ fontWeight: 'bold', marginBottom: '0.5rem' }}>
                            {achievement.name}
                        </div>
                        {achievement.earnedAt ? (
                            <div style={{
                                backgroundColor: '#e8f5e9',
                                color: '#388e3c',
                                fontSize: '0.8rem',
                                padding: '0.25rem 0.5rem',
                                borderRadius: '4px',
                                display: 'inline-block'
                            }}>
                                획득함
                            </div>
                        ) : (
                            <div style={{
                                backgroundColor: '#f5f5f5',
                                color: '#666',
                                fontSize: '0.8rem',
                                padding: '0.25rem 0.5rem',
                                borderRadius: '4px',
                                display: 'inline-block'
                            }}>
                                {achievement.progress}% 완료
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* 선택된 업적 상세 설명 */}
            {selectedAchievement && (
                <div style={{
                    padding: '1.5rem',
                    backgroundColor: '#f9f9f9',
                    borderRadius: '8px',
                    marginTop: '1.5rem',
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '1.5rem'
                }}>
                    <div style={{ fontSize: '3rem' }}>
                        {selectedAchievement.iconUrl ? (
                            <img
                                src={selectedAchievement.iconUrl}
                                alt={selectedAchievement.name}
                                style={{ width: '80px', height: '80px' }}
                            />
                        ) : (
                            '🏆'
                        )}
                    </div>
                    <div style={{ flex: 1 }}>
                        <h3 style={{ margin: '0 0 0.75rem' }}>{selectedAchievement.name}</h3>
                        <p style={{ margin: '0 0 1rem', color: '#333' }}>
                            {selectedAchievement.description}
                        </p>
                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <div>
                                <strong>달성 조건:</strong> {selectedAchievement.requirementDescription}
                            </div>
                            <div>
                                <strong>획득일:</strong> {formatDate(selectedAchievement.earnedAt)}
                            </div>
                        </div>
                        {!selectedAchievement.earnedAt && (
                            <div style={{ marginTop: '1rem' }}>
                                <div style={{
                                    width: '100%',
                                    height: '8px',
                                    backgroundColor: '#e0e0e0',
                                    borderRadius: '4px',
                                    overflow: 'hidden'
                                }}>
                                    <div style={{
                                        width: `${selectedAchievement.progress}%`,
                                        height: '100%',
                                        backgroundColor: '#1976d2',
                                        transition: 'width 0.3s ease-in-out'
                                    }}></div>
                                </div>
                                <div style={{ textAlign: 'right', fontSize: '0.9rem', marginTop: '0.25rem' }}>
                                    {selectedAchievement.progress}% 완료
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {achievements.length === 0 && (
                <div style={{
                    textAlign: 'center',
                    padding: '2rem',
                    backgroundColor: '#f5f5f5',
                    borderRadius: '8px'
                }}>
                    <p>획득한 업적이 없습니다. 퀴즈를 풀면서 업적을 획득해보세요!</p>
                </div>
            )}
        </div>
    );
};

export default AchievementGallery;