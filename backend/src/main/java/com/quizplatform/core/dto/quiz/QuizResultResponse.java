package com.quizplatform.core.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {
    // 퀴즈 정보
    private Long quizId;
    private String title;
    private Integer totalQuestions;

    // 결과 정보
    private Integer correctAnswers;
    private Integer score;
    private Integer totalPossibleScore;
    private Integer timeTaken;
    private LocalDateTime completedAt;

    // 경험치 정보
    private Integer experienceGained;
    private Integer newTotalExperience;

    // 각 문제별 결과
    private List<QuestionResultDto> questions;

    /**
     * 개별 문제 결과 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDto {
        private Long id;
        private String questionText;
        private String yourAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
        private Integer points;
    }
}