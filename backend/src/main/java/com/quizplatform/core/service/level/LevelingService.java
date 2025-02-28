package com.quizplatform.core.service.level;

import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.repository.user.UserLevelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelingService {

    private final UserLevelRepository userLevelRepository;

    /**
     * 퀴즈 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     *
     * @param attempt 완료된 QuizAttempt 객체
     */
    public void calculateQuizExp(QuizAttempt attempt) {
        // 기본 경험치, 점수 배수, 시간 보너스를 이용하여 총 경험치 계산
        int baseExp = attempt.getQuiz().getDifficultyLevel().getBaseExp();
        double scoreMultiplier = attempt.getScore() / 100.0;
        double timeBonus = calculateTimeBonus(attempt);

        int totalExp = (int) (baseExp * scoreMultiplier * timeBonus);

        // 사용자의 UserLevel을 가져오고, 없으면 예외 처리
        UserLevel userLevel = userLevelRepository.findByUser(attempt.getUser())
                .orElseThrow(() -> new IllegalStateException("UserLevel not found for user: " + attempt.getUser().getId()));

        // 경험치 증가 및 레벨업 처리
        userLevel.gainExp(totalExp);
        userLevelRepository.save(userLevel);

        // 업적 체크
        checkAchievements(attempt, userLevel);
    }

    /**
     * 퀴즈 완료에 따른 시간 보너스 계산.
     *
     * @param attempt QuizAttempt 객체
     * @return 시간 보너스 배수
     */
    private double calculateTimeBonus(QuizAttempt attempt) {
        int timeTaken = attempt.getTimeTaken();
        int timeLimit = attempt.getQuiz().getTimeLimit() * 60; // 시간 제한을 초 단위로 변환

        if (timeTaken < timeLimit / 2) {
            return 1.5; // 제한 시간의 절반보다 적게 걸렸다면 1.5배
        } else if (timeTaken < timeLimit * 0.7) {
            return 1.2; // 제한 시간의 70%보다 적게 걸렸다면 1.2배
        }
        return 1.0; // 그 외에는 기본 보너스 없음
    }

    /**
     * 퀴즈 완료 후 업적을 체크하여 해당 업적을 부여합니다.
     *
     * @param attempt   QuizAttempt 객체
     * @param userLevel UserLevel 객체
     */
    private void checkAchievements(QuizAttempt attempt, UserLevel userLevel) {
        User user = attempt.getUser();

        // 첫 퀴즈 완료 업적 부여
        if (user.getQuizAttempts().size() == 1) {
            userLevel.getAchievements().add(Achievement.FIRST_QUIZ_COMPLETED);
        }

        // 만점 업적 부여
        if (attempt.getScore() == 100) {
            userLevel.getAchievements().add(Achievement.PERFECT_SCORE);
        }

        // 연승 업적 체크
        checkWinningStreak(user, userLevel);
    }

    /**
     * 연승 업적을 체크하여 해당 업적을 부여합니다.
     *
     * @param user      User 객체
     * @param userLevel UserLevel 객체
     */
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

    /**
     * 사용자의 현재 연승(연속 만점 또는 기타 승리 조건)을 계산합니다.
     * TODO: 실제 연승 계산 로직을 구현해야 합니다.
     *
     * @param user User 객체
     * @return 현재 연승 횟수
     */
    private int calculateCurrentStreak(User user) {
        // TODO: 연승 계산 로직 구현
        return 0;
    }
}
