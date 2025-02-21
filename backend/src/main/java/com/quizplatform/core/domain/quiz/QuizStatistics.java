package com.quizplatform.core.domain.quiz;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class QuizStatistics {
    private int totalAttempts;
    private double averageScore;
    private double completionRate;
    private int averageTimeSeconds;
    private Map<DifficultyLevel, Integer> difficultyDistribution;
    private List<QuestionStatistic> questionStatistics;

    public static QuizStatistics from(Quiz quiz) {
        return QuizStatistics.builder()
                .totalAttempts(quiz.getAttemptCount())
                .averageScore(quiz.getAvgScore())
                .completionRate(calculateCompletionRate(quiz))
                .averageTimeSeconds(calculateAverageTime(quiz))
                .difficultyDistribution(calculateDifficultyDistribution(quiz))
                .questionStatistics(createQuestionStatistics(quiz))
                .build();
    }

    private static double calculateCompletionRate(Quiz quiz) {
        // 구현 필요
        return 0.0;
    }

    private static int calculateAverageTime(Quiz quiz) {
        // 구현 필요
        return 0;
    }

    private static Map<DifficultyLevel, Integer> calculateDifficultyDistribution(Quiz quiz) {
        // 구현 필요
        return new HashMap<>();
    }

    private static List<QuestionStatistic> createQuestionStatistics(Quiz quiz) {
        // 구현 필요
        return new ArrayList<>();
    }

    @Getter
    @Builder
    public static class QuestionStatistic {
        private Long questionId;
        private int correctAnswers;
        private int totalAttempts;
        private double correctRate;
        private int averageTimeSeconds;
    }
}