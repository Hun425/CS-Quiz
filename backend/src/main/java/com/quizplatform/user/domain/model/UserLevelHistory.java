package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserLevelHistory 도메인 모델
 */
@Getter
public class UserLevelHistory {

    private Long id;
    private Long userId;
    private int previousLevel;
    private int level;
    private LocalDateTime createdAt; // 생성 시간 필드명 일치

    @Builder
    public UserLevelHistory(Long id, Long userId, int previousLevel, int level, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.previousLevel = previousLevel;
        this.level = level;
        this.createdAt = createdAt;
    }
} 