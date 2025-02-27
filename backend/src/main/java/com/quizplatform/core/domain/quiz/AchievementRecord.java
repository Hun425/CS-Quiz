package com.quizplatform.core.domain.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class AchievementRecord {
    private Long id;
    private Long userId;
    private Long achievementId;
    private String achievementName;
    private ZonedDateTime earnedAt;
}
