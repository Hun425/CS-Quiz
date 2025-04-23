package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.entity.QuizStatistics;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuizStatisticsResponse {
    private int totalAttempts;
    private double averageScore;
    private double completionRate;
    private int averageTimeSeconds;
    private Map<DifficultyLevel, Integer> difficultyDistribution;
    private List<QuestionStatistics> questionStatistics;

    @Getter
    @Builder
    public static class QuestionStatistics {
        private Long questionId;
        private int correctAnswers;
        private int totalAttempts;
        private double correctRate;
        private int averageTimeSeconds;
    }

    /**
     * 도메인 객체 QuizStatistics에서 DTO로 변환하는 메서드
     * (아래 코드는 QuizStatistics 도메인 객체가 해당 getter들을 제공한다고 가정)
     */
    public static QuizStatisticsResponse from(QuizStatistics statistics) {
        return QuizStatisticsResponse.builder()
                .totalAttempts(statistics.getTotalAttempts())
                .averageScore(statistics.getAverageScore())
                .completionRate(statistics.getCompletionRate())
                .averageTimeSeconds(statistics.getAverageTimeSeconds())
                .difficultyDistribution(statistics.getDifficultyDistribution())
                .questionStatistics(
                        statistics.getQuestionStatistics().stream()
                                .map(qs -> QuestionStatistics.builder()
                                        .questionId(qs.getQuestionId())
                                        .correctAnswers(qs.getCorrectAnswers())
                                        .totalAttempts(qs.getTotalAttempts())
                                        .correctRate(qs.getCorrectRate())
                                        .averageTimeSeconds(qs.getAverageTimeSeconds())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }
}
