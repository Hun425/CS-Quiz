package com.quizplatform.core.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 업적 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String earnedAt;
    private Integer progress;
    private String requirementDescription;
}
