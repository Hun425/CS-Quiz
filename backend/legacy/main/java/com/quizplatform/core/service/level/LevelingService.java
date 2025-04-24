package com.quizplatform.core.service.level;


import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserAchievementHistory;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.domain.user.UserLevelHistory;
import com.quizplatform.core.dto.battle.BattleResult;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.user.AchievementRepository;
import com.quizplatform.core.repository.user.UserLevelHistoryRepository;
import com.quizplatform.core.repository.user.UserLevelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LevelingService {

    private final UserLevelRepository userLevelRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;
    private final AchievementRepository achievementRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    /**
     * 퀴즈 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     *
     * @param attempt 완료된 QuizAttempt 객체
     * @return 획득한 경험치
     */
    public int calculateQuizExp(QuizAttempt attempt) {
        // 기본 경험치, 점수 배수, 시간 보너스를 이용하여 총 경험치 계산
        int baseExp = attempt.getQuiz().getDifficultyLevel().getBaseExp();
        double scoreMultiplier = attempt.getScore() / 100.0;
        double timeBonus = calculateTimeBonus(attempt);

        int totalExp = (int) (baseExp * scoreMultiplier * timeBonus);

        // 사용자 업데이트
        User user = attempt.getUser();
        user.gainExperience(totalExp);

        // 사용자의 UserLevel을 가져오고, 없으면 생성
        UserLevel userLevel = userLevelRepository.findByUser(user)
                .orElseGet(() -> createNewUserLevel(user));

        // 경험치 증가 및 레벨업 처리
        int oldLevel = userLevel.getLevel();
        userLevel.gainExp(totalExp);

        // 레벨 변경이 있었으면 내역 기록
        if (oldLevel != userLevel.getLevel()) {
            recordLevelUp(user, oldLevel, userLevel.getLevel());
        }

        userLevelRepository.save(userLevel);

        // 업적 체크
        checkAchievements(attempt, userLevel);

        return totalExp;
    }

    /**
     * 배틀 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     *
     * @param result 배틀 결과
     * @param user 사용자
     * @return 획득한 경험치
     */
    public int calculateBattleExp(BattleResult result, User user) {
        // 배틀에 기반한 경험치 계산
        boolean isWinner = result.getWinner().getUser().getId().equals(user.getId());
        int correctAnswers = result.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .mapToInt(p -> p.getCorrectAnswersCount())
                .findFirst()
                .orElse(0);

        int totalQuestions = result.getTotalQuestions();
        double correctRate = totalQuestions > 0 ? (double) correctAnswers / totalQuestions : 0;

        // 기본 경험치
        int baseExp = 50;

        // 정답률 보너스
        double correctBonus = correctRate * 2.0; // 최대 2배

        // 승리 보너스
        double winBonus = isWinner ? 1.5 : 1.0;

        int totalExp = (int) (baseExp * correctBonus * winBonus);

        // 사용자 업데이트
        user.gainExperience(totalExp);

        // 사용자의 UserLevel을 가져오고, 없으면 생성
        UserLevel userLevel = userLevelRepository.findByUser(user)
                .orElseGet(() -> createNewUserLevel(user));

        // 경험치 증가 및 레벨업 처리
        int oldLevel = userLevel.getLevel();
        userLevel.gainExp(totalExp);

        // 레벨 변경이 있었으면 내역 기록
        if (oldLevel != userLevel.getLevel()) {
            recordLevelUp(user, oldLevel, userLevel.getLevel());
        }

        userLevelRepository.save(userLevel);

        // 배틀 관련 업적 체크
        if (isWinner) {
            checkBattleAchievements(user, userLevel);
        }

        return totalExp;
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
     * 새로운 UserLevel 객체 생성
     */
    private UserLevel createNewUserLevel(User user) {
        UserLevel userLevel = new UserLevel(user);
        return userLevelRepository.save(userLevel);
    }

    /**
     * 레벨업 기록 저장
     */
    private void recordLevelUp(User user, int oldLevel, int newLevel) {
        UserLevelHistory history = new UserLevelHistory(user, oldLevel, newLevel);
        userLevelHistoryRepository.save(history);
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
        if (quizAttemptRepository.countByUserId(user.getId()) == 1) {
            awardAchievement(user, userLevel, Achievement.FIRST_QUIZ_COMPLETED);
        }

        // 만점 업적 부여
        if (attempt.getScore() == 100) {
            awardAchievement(user, userLevel, Achievement.PERFECT_SCORE);
        }

        // 빠른 해결사 업적 체크
        if (attempt.getTimeTaken() < 30) {
            awardAchievement(user, userLevel, Achievement.QUICK_SOLVER);
        }

        // 지식 탐구자 업적 체크 (3가지 다른 주제의 퀴즈)
        checkKnowledgeSeekerAchievement(user, userLevel);

        // 데일리 퀴즈 마스터 업적 체크
        checkDailyQuizMasterAchievement(user, userLevel);
    }

    /**
     * 배틀 관련 업적 체크
     */
    private void checkBattleAchievements(User user, UserLevel userLevel) {
        // 연승 업적 체크
        int streak = calculateCurrentStreak(user);

        if (streak >= 10) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_10);
        } else if (streak >= 5) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_5);
        } else if (streak >= 3) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_3);
        }
    }

    /**
     * 지식 탐구자 업적 체크 (3가지 다른 주제의 퀴즈)
     */
    private void checkKnowledgeSeekerAchievement(User user, UserLevel userLevel) {
        // 이 사용자가 퀴즈를 시도한 모든 퀴즈의 태그 ID를 가져옵니다.
        List<Long> distinctTagIds = quizAttemptRepository.findByUser(user, null)
                .getContent()
                .stream()
                .flatMap(attempt -> attempt.getQuiz().getTags().stream())
                .map(tag -> tag.getId())
                .distinct()
                .collect(Collectors.toList());

        if (distinctTagIds.size() >= 3) {
            awardAchievement(user, userLevel, Achievement.KNOWLEDGE_SEEKER);
        }
    }

    /**
     * 데일리 퀴즈 마스터 업적 체크 (7일 연속으로 데일리 퀴즈를 완료)
     */
    private void checkDailyQuizMasterAchievement(User user, UserLevel userLevel) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 지난 7일간의 시도 조회
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserAndDateRange(
                user, sevenDaysAgo, now);

        // 날짜별로 Daily 퀴즈 시도 여부를 체크
        boolean hasDailyQuizForEachDay = true;
        for (int i = 0; i < 7; i++) {
            LocalDate targetDate = now.minusDays(i).toLocalDate();

            boolean hasCompletedDailyForDate = recentAttempts.stream()
                    .filter(attempt -> attempt.getQuiz().getQuizType().name().equals("DAILY"))
                    .filter(attempt -> attempt.isCompleted())
                    .anyMatch(attempt -> attempt.getEndTime().toLocalDate().equals(targetDate));

            if (!hasCompletedDailyForDate) {
                hasDailyQuizForEachDay = false;
                break;
            }
        }

        if (hasDailyQuizForEachDay) {
            awardAchievement(user, userLevel, Achievement.DAILY_QUIZ_MASTER);
        }
    }

    /**
     * 업적 부여
     */
    private void awardAchievement(User user, UserLevel userLevel, Achievement achievement) {
        // 이미 획득한 업적인지 확인
        if (userLevel.getAchievements().contains(achievement)) {
            return;
        }

        // 업적 추가
        userLevel.getAchievements().add(achievement);
        userLevelRepository.save(userLevel);

        // 업적 획득 이력 저장
        UserAchievementHistory achievementHistory = new UserAchievementHistory(user, achievement);
        achievementRepository.save(achievementHistory);

        // 경험치 보상 부여 (업적마다 다른 경험치 부여 가능)
        int expReward = calculateAchievementExpReward(achievement);
        if (expReward > 0) {
            user.gainExperience(expReward);
        }
    }

    /**
     * 업적에 따른 경험치 보상 계산
     */
    private int calculateAchievementExpReward(Achievement achievement) {
        switch (achievement) {
            case FIRST_QUIZ_COMPLETED:
                return 50;
            case PERFECT_SCORE:
                return 100;
            case WINNING_STREAK_3:
                return 75;
            case WINNING_STREAK_5:
                return 150;
            case WINNING_STREAK_10:
                return 300;
            case DAILY_QUIZ_MASTER:
                return 200;
            case QUICK_SOLVER:
                return 75;
            case KNOWLEDGE_SEEKER:
                return 150;
            default:
                return 0;
        }
    }

    /**
     * 사용자의 현재 연승(연속 승리)을 계산합니다.
     *
     * @param user User 객체
     * @return 현재 연승 횟수
     */
    private int calculateCurrentStreak(User user) {
        // UserBattleStats 에서 currentStreak 값을 가져옵니다.
        if (user.getBattleStats() != null) {
            return user.getBattleStats().getCurrentStreak();
        }
        return 0;
    }
}