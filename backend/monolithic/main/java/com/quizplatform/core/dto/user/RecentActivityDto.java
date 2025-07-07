package com.quizplatform.core.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 최근 활동 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecentActivityDto {
    private Long id;
    private String type; // QUIZ_ATTEMPT, ACHIEVEMENT_EARNED, LEVEL_UP
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Long achievementId;
    private String achievementName;
    private Integer newLevel;
    private String timestamp;
}