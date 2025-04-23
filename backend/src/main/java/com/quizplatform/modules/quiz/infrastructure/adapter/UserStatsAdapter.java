package com.quizplatform.modules.quiz.infrastructure.adapter;

import com.quizplatform.modules.quiz.infrastructure.repository.QuestionAttemptRepository;
import com.quizplatform.modules.quiz.infrastructure.repository.QuizAttemptRepository;
import com.quizplatform.modules.quiz.infrastructure.repository.TagRepository; // Needed for distinct tag count
import com.quizplatform.modules.user.application.port.out.UserAchievementCalculationPort;
import com.quizplatform.modules.user.application.port.out.UserQuizStatsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component; // Or @Service

import java.util.Optional;

@Component // Register as a Spring Bean
@RequiredArgsConstructor
public class UserStatsAdapter implements UserQuizStatsPort, UserAchievementCalculationPort {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final TagRepository tagRepository; // Needed for KNOWLEDGE_SEEKER achievement

    // --- UserQuizStatsPort Implementation ---

    @Override
    public long countTotalAttempts(Long userId) {
        return quizAttemptRepository.countByUserId(userId);
    }

    @Override
    public long countCompletedAttempts(Long userId) {
        return quizAttemptRepository.countByUserIdAndIsCompletedTrue(userId);
    }

    @Override
    public Double getAverageScore(Long userId) {
        // Repository returns null if no attempts, handle appropriately
        return quizAttemptRepository.getAverageScoreByUserId(userId);
    }

    @Override
    public long countTotalCorrectAnswers(Long userId) {
        return questionAttemptRepository.countCorrectAnswersByUserId(userId);
    }

    @Override
    public long countTotalAnsweredQuestions(Long userId) {
        return questionAttemptRepository.countTotalQuestionsByUserId(userId);
    }

    @Override
    public Integer getTotalTimeTakenSeconds(Long userId) {
        // Repository returns null if no attempts
        return quizAttemptRepository.getTotalTimeTakenByUserId(userId);
    }

    @Override
    public Integer getBestScore(Long userId) {
        // Repository returns null if no attempts
        return quizAttemptRepository.getMaxScoreByUserId(userId);
    }

    @Override
    public Integer getWorstScore(Long userId) {
        // Repository returns null if no attempts
        return quizAttemptRepository.getMinScoreByUserId(userId);
    }

    // --- UserAchievementCalculationPort Implementation ---

    @Override
    public long countUserQuizAttempts(Long userId) {
        // Same as countTotalAttempts from UserQuizStatsPort
        return quizAttemptRepository.countByUserId(userId);
    }

    @Override
    public Optional<Integer> getUserBestQuizScore(Long userId) {
        // Wrap the result from repository (which can be null) in Optional
        return Optional.ofNullable(quizAttemptRepository.getMaxScoreByUserId(userId));
    }

    @Override
    public int getUserDailyQuizStreak(Long userId) {
        // Repository method likely returns int or Integer, handle potential null if Integer
        Integer streak = quizAttemptRepository.getDailyQuizStreakByUserId(userId);
        return streak != null ? streak : 0;
    }

    @Override
    public Optional<Integer> getUserFastestQuizTimeSeconds(Long userId) {
        // Wrap the result from repository (which can be null) in Optional
        return Optional.ofNullable(quizAttemptRepository.getFastestTimeTakenByUserId(userId));
    }

    @Override
    public int countUserDistinctAttemptedTags(Long userId) {
        // Assuming TagRepository provides this method
        return tagRepository.countDistinctTagsAttemptedByUserId(userId);
    }
} 