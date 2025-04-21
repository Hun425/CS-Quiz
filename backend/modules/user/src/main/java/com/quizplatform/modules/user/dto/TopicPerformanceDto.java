package com.quizplatform.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 주제별 성과 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TopicPerformanceDto {
    private Long tagId;
    private String tagName;
    private Integer quizzesTaken;
    private Double averageScore;
    private Double correctRate;
    private Boolean strength;
}