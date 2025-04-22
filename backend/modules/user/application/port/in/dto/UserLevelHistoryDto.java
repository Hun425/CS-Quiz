package com.quizplatform.user.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 레벨 변경 이력 DTO
 */
@Getter
public class UserLevelHistoryDto {

    private final Long id;
    private final Long userId;
    private final int previousLevel;
    private final int level;
    private final LocalDateTime createdAt;

    @Builder
    public UserLevelHistoryDto(Long id, Long userId, int previousLevel, int level, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.previousLevel = previousLevel;
        this.level = level;
        this.createdAt = createdAt;
    }

    // 필요시 도메인 모델에서 DTO로 변환하는 정적 메소드 추가 가능
    // public static UserLevelHistoryDto fromDomain(UserLevelHistory domain) { ... }
} 