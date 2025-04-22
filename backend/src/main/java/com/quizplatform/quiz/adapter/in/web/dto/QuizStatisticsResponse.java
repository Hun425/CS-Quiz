package com.quizplatform.quiz.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 통계 응답 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizStatisticsResponse {

    private Long quizId;
    private String quizTitle;
    private Integer totalAttempts;
    private Double averageScore;
    private Double passRate;
    private Integer passingScore;
    private Integer maxPossibleScore;
    
    private List<QuestionStatistics> questionStatistics = new ArrayList<>();
    
    /**
     * 문제별 통계 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionStatistics {
        private Long questionId;
        private String questionText;
        private Double correctRate;
        private Integer points;
    }
}
