package com.quizplatform.core.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 통계 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDto {
    private Integer totalQuizzesTaken;
    private Integer totalQuizzesCompleted;
    private Double averageScore;
    private Integer totalCorrectAnswers;
    private Integer totalQuestions;
    private Double correctRate;
    private Integer totalTimeTaken;
    private Integer bestScore;
    private Integer worstScore;
}