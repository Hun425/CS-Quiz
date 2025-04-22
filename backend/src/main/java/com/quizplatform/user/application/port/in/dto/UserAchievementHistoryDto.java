package com.quizplatform.user.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 업적 획득 이력 DTO
 */
@Getter
public class UserAchievementHistoryDto {

    private final Long id;
    private final Long userId;
    private final String achievementName;
    private final LocalDateTime earnedAt;

    @Builder
    public UserAchievementHistoryDto(Long id, Long userId, String achievementName, LocalDateTime earnedAt) {
        this.id = id;
        this.userId = userId;
        this.achievementName = achievementName;
        this.earnedAt = earnedAt;
    }

    // 필요시 도메인 모델에서 DTO로 변환하는 정적 메소드 추가 가능
    // public static UserAchievementHistoryDto fromDomain(UserAchievementHistory domain) { ... }
} 