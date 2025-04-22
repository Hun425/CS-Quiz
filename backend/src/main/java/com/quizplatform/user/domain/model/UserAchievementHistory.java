package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserAchievementHistory 도메인 모델
 */
@Getter
public class UserAchievementHistory {

    private Long id;
    private Long userId;
    private String achievementName;
    private LocalDateTime earnedAt;

    @Builder
    public UserAchievementHistory(Long id, Long userId, String achievementName, LocalDateTime earnedAt) {
        this.id = id;
        this.userId = userId;
        this.achievementName = achievementName;
        this.earnedAt = earnedAt;
    }
} 