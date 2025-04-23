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

/**
 * 사용자 레벨, 경험치, 업적 관련 로직을 처리하는 서비스입니다.
 * 퀴즈 완료 또는 배틀 결과에 따라 경험치를 계산하고, 레벨을 갱신하며,
 * 관련 업적 달성 여부를 확인하고 부여합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LevelingService {

    private final UserLevelRepository userLevelRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;
    private final AchievementRepository achievementRepository; // 실제로는 UserAchievementHistoryRepository를 사용
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository; // User 엔티티 저장을 위해 필요할 수 있음

    /**
     * 퀴즈 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     * 계산된 경험치는 사용자에게 부여되며, 레벨업 발생 시 이력이 기록됩니다.
     * 퀴즈 완료와 관련된 여러 업적 달성 여부를 확인합니다.
     *
     * @param attempt 완료된 QuizAttempt 객체
     * @return 획득한 총 경험치
     */
    public int calculateQuizExp(QuizAttempt attempt) {
        // 기본 경험치, 점수 배수, 시간 보너스를 이용하여 총 경험치 계산
        int baseExp = attempt.getQuiz().getDifficultyLevel().getBaseExp();
        // 점수 기반 배율 (0.0 ~ 1.0)
        double scoreMultiplier = attempt.getScore() / 100.0;
        // 시간 보너스 배율 계산
        double timeBonus = calculateTimeBonus(attempt);

        int totalExp = (int) (baseExp * scoreMultiplier * timeBonus);

        // 사용자 경험치 업데이트
        User user = attempt.getUser();
        user.gainExperience(totalExp); // User 엔티티 내 경험치 증가 로직 호출

        // 사용자의 UserLevel 정보 조회 또는 생성
        UserLevel userLevel = userLevelRepository.findByUser(user)
                .orElseGet(() -> createNewUserLevel(user));

        // UserLevel 객체에 경험치 증가 및 레벨업 처리 위임
        int oldLevel = userLevel.getLevel();
        userLevel.gainExp(totalExp); // UserLevel 엔티티 내 경험치/레벨 처리 로직 호출

        // 레벨 변경이 있었으면 레벨업 이력 기록
        if (oldLevel != userLevel.getLevel()) {
            recordLevelUp(user, oldLevel, userLevel.getLevel());
            log.info("User {} leveled up! {} -> {}", user.getUsername(), oldLevel, userLevel.getLevel());
        }

        // 변경된 UserLevel 정보 저장
        userLevelRepository.save(userLevel);
        // 변경된 User 정보 저장 (JPA dirty checking으로 자동 저장될 수도 있지만 명시적으로 호출)
        // userRepository.save(user); // 필요 시 주석 해제

        // 퀴즈 관련 업적 체크
        checkAchievements(attempt, userLevel);

        log.info("User {} gained {} EXP from quiz '{}'. Current EXP: {}", user.getUsername(), totalExp, attempt.getQuiz().getTitle(), user.getExperience());
        return totalExp;
    }

    /**
     * 배틀 완료 후 경험치를 계산하고, 사용자 레벨을 갱신하며 업적을 체크합니다.
     * 배틀 결과(승리 여부, 정답률 등)를 기반으로 경험치를 계산합니다.
     * 레벨업 처리 및 배틀 관련 업적(연승 등)을 확인합니다.
     *
     * @param result 배틀 결과 정보를 담은 BattleResult 객체
     * @param user   경험치를 받을 대상 사용자
     * @return 획득한 총 경험치
     */
    public int calculateBattleExp(BattleResult result, User user) {
        // 배틀 결과에서 필요한 정보 추출
        boolean isWinner = result.getWinner() != null && result.getWinner().getUser().getId().equals(user.getId());
        int correctAnswers = result.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .mapToInt(p -> p.getCorrectAnswersCount())
                .findFirst()
                .orElse(0);

        int totalQuestions = result.getTotalQuestions();
        // 정답률 계산 (0.0 ~ 1.0)
        double correctRate = totalQuestions > 0 ? (double) correctAnswers / totalQuestions : 0;

        // 기본 배틀 경험치
        int baseExp = 50;

        // 정답률 보너스 (정답률 * 2배, 최대 2.0)
        double correctBonus = Math.max(1.0, correctRate * 2.0); // 최소 1배 보장

        // 승리 보너스 (승리 시 1.5배)
        double winBonus = isWinner ? 1.5 : 1.0;

        int totalExp = (int) (baseExp * correctBonus * winBonus);

        // 사용자 경험치 업데이트
        user.gainExperience(totalExp);

        // 사용자의 UserLevel 정보 조회 또는 생성
        UserLevel userLevel = userLevelRepository.findByUser(user)
                .orElseGet(() -> createNewUserLevel(user));

        // UserLevel 객체에 경험치 증가 및 레벨업 처리 위임
        int oldLevel = userLevel.getLevel();
        userLevel.gainExp(totalExp);

        // 레벨 변경이 있었으면 레벨업 이력 기록
        if (oldLevel != userLevel.getLevel()) {
            recordLevelUp(user, oldLevel, userLevel.getLevel());
            log.info("User {} leveled up! {} -> {}", user.getUsername(), oldLevel, userLevel.getLevel());
        }

        // 변경된 UserLevel 정보 저장
        userLevelRepository.save(userLevel);
        // 변경된 User 정보 저장
        // userRepository.save(user); // 필요 시 주석 해제

        // 배틀 관련 업적 체크 (승리자에게만 연승 체크 등)
        if (isWinner) {
            checkBattleAchievements(user, userLevel);
        }

        log.info("User {} gained {} EXP from battle. Current EXP: {}", user.getUsername(), totalExp, user.getExperience());
        return totalExp;
    }

    /**
     * 퀴즈 완료 시간에 따른 보너스 배율을 계산합니다.
     * 제한 시간 대비 소요 시간이 짧을수록 높은 배율을 반환합니다. (내부 헬퍼 메서드)
     *
     * @param attempt 완료된 QuizAttempt 객체
     * @return 시간 보너스 배수 (1.0, 1.2, 1.5)
     */
    private double calculateTimeBonus(QuizAttempt attempt) {
        int timeTaken = attempt.getTimeTaken(); // 소요 시간(초)
        // 퀴즈의 제한 시간(분)을 초 단위로 변환
        int timeLimit = attempt.getQuiz().getTimeLimit() * 60;

        if (timeLimit <= 0) return 1.0; // 시간 제한이 없는 경우 보너스 없음

        if (timeTaken < timeLimit / 2) { // 제한 시간의 50% 미만 소요 시
            return 1.5;
        } else if (timeTaken < timeLimit * 0.7) { // 제한 시간의 70% 미만 소요 시
            return 1.2;
        }
        return 1.0; // 그 외
    }

    /**
     * 특정 사용자에 대한 새로운 UserLevel 객체를 생성하고 저장합니다.
     * 사용자가 처음으로 경험치를 얻거나 레벨 관련 정보가 필요할 때 호출됩니다. (내부 헬퍼 메서드)
     *
     * @param user UserLevel을 생성할 사용자
     * @return 생성되고 저장된 UserLevel 객체
     */
    private UserLevel createNewUserLevel(User user) {
        log.info("Creating new UserLevel for user: {}", user.getUsername());
        UserLevel userLevel = new UserLevel(user);
        // UserLevel 객체를 먼저 저장하여 ID를 생성해야 할 수 있음 (관계 설정 방식에 따라)
        return userLevelRepository.save(userLevel);
    }

    /**
     * 사용자의 레벨업 정보를 UserLevelHistory에 기록합니다. (내부 헬퍼 메서드)
     *
     * @param user     레벨업한 사용자
     * @param oldLevel 이전 레벨
     * @param newLevel 새로운 레벨
     */
    private void recordLevelUp(User user, int oldLevel, int newLevel) {
        UserLevelHistory history = new UserLevelHistory(user, oldLevel, newLevel);
        userLevelHistoryRepository.save(history);
        log.debug("Recorded level up history for user {}: {} -> {}", user.getUsername(), oldLevel, newLevel);
    }

    /**
     * 퀴즈 완료 후 관련 업적 달성 여부를 확인하고, 달성 시 업적을 부여합니다. (내부 헬퍼 메서드)
     * 첫 퀴즈 완료, 만점, 빠른 해결, 지식 탐구자, 데일리 퀴즈 마스터 업적을 체크합니다.
     *
     * @param attempt   완료된 QuizAttempt 객체
     * @param userLevel 사용자의 UserLevel 객체
     */
    private void checkAchievements(QuizAttempt attempt, UserLevel userLevel) {
        User user = attempt.getUser();

        // 첫 퀴즈 완료 업적 (사용자의 총 퀴즈 시도 횟수가 1인지 확인)
        if (quizAttemptRepository.countByUserId(user.getId()) == 1) {
            awardAchievement(user, userLevel, Achievement.FIRST_QUIZ_COMPLETED);
        }

        // 만점 업적 (점수가 100점인지 확인)
        if (attempt.getScore() == 100) {
            awardAchievement(user, userLevel, Achievement.PERFECT_SCORE);
        }

        // 빠른 해결사 업적 (소요 시간이 30초 미만인지 확인)
        if (attempt.getTimeTaken() < 30) {
            awardAchievement(user, userLevel, Achievement.QUICK_SOLVER);
        }

        // 지식 탐구자 업적 (3가지 다른 주제 퀴즈 완료)
        checkKnowledgeSeekerAchievement(user, userLevel);

        // 데일리 퀴즈 마스터 업적 (7일 연속 데일리 퀴즈 완료)
        checkDailyQuizMasterAchievement(user, userLevel);
    }

    /**
     * 배틀 관련 업적 달성 여부를 확인하고, 달성 시 업적을 부여합니다. (내부 헬퍼 메서드)
     * 현재는 연승 관련 업적만 체크합니다.
     *
     * @param user      업적을 체크할 사용자
     * @param userLevel 사용자의 UserLevel 객체
     */
    private void checkBattleAchievements(User user, UserLevel userLevel) {
        // 현재 연승 횟수 계산
        int streak = calculateCurrentStreak(user);

        // 연승 횟수에 따라 업적 부여
        if (streak >= 10) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_10);
        } else if (streak >= 5) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_5);
        } else if (streak >= 3) {
            awardAchievement(user, userLevel, Achievement.WINNING_STREAK_3);
        }
    }

    /**
     * '지식 탐구자' 업적 달성 여부를 확인합니다. (내부 헬퍼 메서드)
     * 사용자가 완료한 퀴즈들의 태그(주제) 종류가 3가지 이상인지 확인합니다.
     *
     * @param user      업적을 체크할 사용자
     * @param userLevel 사용자의 UserLevel 객체
     */
    private void checkKnowledgeSeekerAchievement(User user, UserLevel userLevel) {
        // 사용자가 시도한 모든 퀴즈의 고유한 태그 ID 목록 조회
        List<Long> distinctTagIds = quizAttemptRepository.findByUser(user, null) // 페이징 없이 모든 시도 조회 (성능 고려 필요)
                .getContent() // Page 객체에서 내용(List<QuizAttempt>) 추출
                .stream()
                .filter(QuizAttempt::isCompleted) // 완료된 시도만 필터링
                .flatMap(attempt -> attempt.getQuiz().getTags().stream()) // 각 퀴즈의 태그 스트림으로 변환
                .map(tag -> tag.getId()) // 태그 ID 추출
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        // 고유한 태그 종류가 3개 이상이면 업적 부여
        if (distinctTagIds.size() >= 3) {
            awardAchievement(user, userLevel, Achievement.KNOWLEDGE_SEEKER);
        }
    }

    /**
     * '데일리 퀴즈 마스터' 업적 달성 여부를 확인합니다. (내부 헬퍼 메서드)
     * 최근 7일 동안 매일 데일리 퀴즈를 완료했는지 확인합니다.
     *
     * @param user      업적을 체크할 사용자
     * @param userLevel 사용자의 UserLevel 객체
     */
    private void checkDailyQuizMasterAchievement(User user, UserLevel userLevel) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7); // 7일 전 시점 계산

        // 최근 7일간 사용자의 모든 퀴즈 시도 조회
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserAndDateRange(
                user, sevenDaysAgo, now);

        // 7일간 매일 데일리 퀴즈를 완료했는지 확인
        boolean completedDailyEveryDay = true;
        for (int i = 0; i < 7; i++) {
            LocalDate targetDate = now.minusDays(i).toLocalDate(); // 확인할 날짜 (오늘, 어제, ..., 6일 전)

            // 해당 날짜에 완료된 'DAILY' 타입 퀴즈 시도가 있는지 확인
            boolean completedDailyForDate = recentAttempts.stream()
                    .filter(attempt -> "DAILY".equals(attempt.getQuiz().getQuizType().name())) // 데일리 퀴즈 필터링
                    .filter(QuizAttempt::isCompleted) // 완료된 시도 필터링
                    // 완료 시간(EndTime)의 날짜가 targetDate와 같은지 확인
                    .anyMatch(attempt -> attempt.getEndTime() != null && attempt.getEndTime().toLocalDate().equals(targetDate));

            // 하루라도 데일리 퀴즈를 완료하지 않았다면 플래그를 false로 변경하고 반복 중단
            if (!completedDailyForDate) {
                completedDailyEveryDay = false;
                break;
            }
        }

        // 7일 연속 완료했다면 업적 부여
        if (completedDailyEveryDay) {
            awardAchievement(user, userLevel, Achievement.DAILY_QUIZ_MASTER);
        }
    }

    /**
     * 사용자에게 특정 업적을 부여합니다. (내부 헬퍼 메서드)
     * 이미 획득한 업적인지 확인 후, 미획득 시 UserLevel에 추가하고 이력을 저장합니다.
     * 업적에 따른 경험치 보상도 부여합니다.
     *
     * @param user        업적을 받을 사용자
     * @param userLevel   사용자의 UserLevel 객체
     * @param achievement 부여할 업적 Enum 값
     */
    private void awardAchievement(User user, UserLevel userLevel, Achievement achievement) {
        // 이미 획득한 업적인지 확인 (Set을 사용하므로 contains는 O(1) 시간 복잡도)
        if (userLevel.getAchievements().contains(achievement)) {
            log.trace("User {} already has achievement: {}", user.getUsername(), achievement.name());
            return; // 이미 획득했으면 아무것도 하지 않음
        }

        log.info("Awarding achievement '{}' to user {}", achievement.name(), user.getUsername());
        // UserLevel의 업적 목록에 추가
        userLevel.getAchievements().add(achievement);
        // 변경된 UserLevel 저장 (업적 목록 변경 반영)
        userLevelRepository.save(userLevel);

        // 업적 획득 이력(UserAchievementHistory) 생성 및 저장
        UserAchievementHistory achievementHistory = new UserAchievementHistory(user, achievement);
        // UserAchievementHistory 저장은 UserLevelRepository와 다른 Repository를 사용해야 함
        achievementRepository.save(achievementHistory);

        // 업적 획득에 따른 경험치 보상 계산 및 부여
        int expReward = calculateAchievementExpReward(achievement);
        if (expReward > 0) {
            log.info("Granting {} EXP reward for achievement '{}' to user {}", expReward, achievement.name(), user.getUsername());
            user.gainExperience(expReward); // User 엔티티 경험치 증가
            userLevel.gainExp(expReward);   // UserLevel 엔티티 경험치/레벨 재계산 (레벨업 가능성)
            // 경험치 변경으로 인한 User/UserLevel 엔티티 저장 필요
            userLevelRepository.save(userLevel);
            // userRepository.save(user); // 필요 시 주석 해제
        }
    }

    /**
     * 주어진 업적에 해당하는 경험치 보상을 반환합니다. (내부 헬퍼 메서드)
     *
     * @param achievement 경험치 보상을 계산할 업적 Enum 값
     * @return 해당 업적의 경험치 보상량
     */
    private int calculateAchievementExpReward(Achievement achievement) {
        // 각 업적별 보상 경험치 정의
        switch (achievement) {
            case FIRST_QUIZ_COMPLETED: return 50;
            case PERFECT_SCORE: return 100;
            case WINNING_STREAK_3: return 75;
            case WINNING_STREAK_5: return 150;
            case WINNING_STREAK_10: return 300;
            case DAILY_QUIZ_MASTER: return 200;
            case QUICK_SOLVER: return 75;
            case KNOWLEDGE_SEEKER: return 150;
            // 다른 업적 추가 시 여기에 보상 정의
            default: return 0; // 정의되지 않은 업적은 보상 없음
        }
    }

    /**
     * 사용자의 현재 배틀 연승 횟수를 반환합니다. (내부 헬퍼 메서드)
     * 사용자의 UserBattleStats 객체에서 현재 연승 정보를 가져옵니다.
     *
     * @param user 연승 횟수를 조회할 사용자
     * @return 현재 연승 횟수 (UserBattleStats가 없거나 연승 정보가 없으면 0)
     */
    private int calculateCurrentStreak(User user) {
        // 사용자의 배틀 통계(UserBattleStats) 객체 조회
        if (user.getBattleStats() != null) {
            // 통계 객체에서 현재 연승(currentStreak) 값 반환
            return user.getBattleStats().getCurrentStreak();
        }
        // 배틀 통계 정보가 없는 경우 연승 0으로 간주
        return 0;
    }
}