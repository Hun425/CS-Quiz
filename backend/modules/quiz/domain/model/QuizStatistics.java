package domain.model;

import lombok.Builder;
import lombok.Getter;
import domain.model.DifficultyLevel;
import java.util.List;
import java.util.Map;

/**
 * 퀴즈 통계 도메인 모델
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class QuizStatistics {
    private final Long quizId;
    private final int totalAttempts;
    private final double averageScore;
    private final double completionRate;
    private final int averageTimeSeconds;
    private final Map<DifficultyLevel, Integer> difficultyDistribution;
    private final List<QuestionStatistic> questionStatistics;

    @Builder
    public QuizStatistics(
            Long quizId,
            int totalAttempts,
            double averageScore,
            double completionRate,
            int averageTimeSeconds,
            Map<DifficultyLevel, Integer> difficultyDistribution,
            List<QuestionStatistic> questionStatistics
    ) {
        this.quizId = quizId;
        this.totalAttempts = totalAttempts;
        this.averageScore = averageScore;
        this.completionRate = completionRate;
        this.averageTimeSeconds = averageTimeSeconds;
        this.difficultyDistribution = difficultyDistribution;
        this.questionStatistics = questionStatistics;
    }

    /**
     * 문제별 통계 내부 클래스
     */
    @Getter
    public static class QuestionStatistic {
        private final Long questionId;
        private final int correctAnswers;
        private final int totalAttempts;
        private final double correctRate;
        private final int averageTimeSeconds;

        @Builder
        public QuestionStatistic(
                Long questionId,
                int correctAnswers,
                int totalAttempts,
                double correctRate,
                int averageTimeSeconds
        ) {
            this.questionId = questionId;
            this.correctAnswers = correctAnswers;
            this.totalAttempts = totalAttempts;
            this.correctRate = correctRate;
            this.averageTimeSeconds = averageTimeSeconds;
        }
    }
}