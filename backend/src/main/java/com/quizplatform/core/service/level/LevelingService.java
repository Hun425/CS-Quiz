package com.quizplatform.core.service.level;

import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.repository.level.UserLevelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelingService {
    private final UserLevelRepository userLevelRepository;

    // 퀴즈 완료 시 경험치 계산
    public void calculateQuizExp(QuizAttempt attempt) {
        int baseExp = attempt.getQuiz().getDifficultyLevel().getBaseExp();
        double scoreMultiplier = attempt.getScore() / 100.0;
        double timeBonus = calculateTimeBonus(attempt);

        int totalExp = (int) (baseExp * scoreMultiplier * timeBonus);

        UserLevel userLevel = userLevelRepository.findByUser(attempt.getUser());
        userLevel.gainExp(totalExp);
        userLevelRepository.save(userLevel);

        checkAchievements(attempt, userLevel);
    }

    // 시간 보너스 계산
    private double calculateTimeBonus(QuizAttempt attempt) {
        int timeTaken = attempt.getTimeTaken();
        int timeLimit = attempt.getQuiz().getTimeLimit() * 60; // 초 단위로 변환

        if (timeTaken < timeLimit / 2) {
            return 1.5; // 50% 이상 남기고 완료시 1.5배
        } else if (timeTaken < timeLimit * 0.7) {
            return 1.2; // 30% 이상 남기고 완료시 1.2배
        }
        return 1.0;
    }

    // 업적 체크
    private void checkAchievements(QuizAttempt attempt, UserLevel userLevel) {
        // 첫 퀴즈 완료 업적
        if (attempt.getUser().getQuizAttempts().size() == 1) {
            userLevel.getAchievements().add(Achievement.FIRST_QUIZ_COMPLETED);
        }

        // 만점 업적
        if (attempt.getScore() == 100) {
            userLevel.getAchievements().add(Achievement.PERFECT_SCORE);
        }

        // 연승 업적 체크
        checkWinningStreak(attempt.getUser(), userLevel);
    }

    // 연승 체크
    private void checkWinningStreak(User user, UserLevel userLevel) {
        int streak = calculateCurrentStreak(user);

        if (streak >= 10) {
            userLevel.getAchievements().add(Achievement.WINNING_STREAK_10);
        } else if (streak >= 5) {
            userLevel.getAchievements().add(Achievement.WINNING_STREAK_5);
        } else if (streak >= 3) {
            userLevel.getAchievements().add(Achievement.WINNING_STREAK_3);
        }
    }

    private int calculateCurrentStreak(User user) {
        // TODO: 연승 계산 로직 구현
        return 0;
    }
}