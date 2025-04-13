package com.quizplatform.core.service.user;

import com.quizplatform.core.domain.quiz.Achievement;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.dto.user.*;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.question.QuestionAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.repository.user.AchievementRepository;
import com.quizplatform.core.repository.user.UserLevelHistoryRepository;
import com.quizplatform.core.repository.user.UserLevelRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final TagRepository tagRepository;
    private final AchievementRepository achievementRepository;
    private final UserLevelHistoryRepository userLevelHistoryRepository;
    private final EntityMapperService entityMapperService;
    
    /**
     * 사용자 ID로 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * 사용자 프로필 조회
     */
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findByIdWithStats(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));

        return entityMapperService.mapToUserProfileDto(user);
    }

    /**
     * 사용자 통계 조회
     */
    @Cacheable(value = "userStatistics", key = "#userId")
    public UserStatisticsDto getUserStatistics(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        // 퀴즈 시도 통계를 계산하는 로직
        long totalQuizzesTaken = quizAttemptRepository.countByUserId(userId);
        long totalQuizzesCompleted = quizAttemptRepository.countByUserIdAndIsCompletedTrue(userId);
        Double averageScore = quizAttemptRepository.getAverageScoreByUserId(userId);
        long totalCorrectAnswers = questionAttemptRepository.countCorrectAnswersByUserId(userId);
        long totalQuestions = questionAttemptRepository.countTotalQuestionsByUserId(userId);
        Double correctRate = totalQuestions > 0 ? (double) totalCorrectAnswers / totalQuestions * 100 : 0;
        Integer totalTimeTaken = quizAttemptRepository.getTotalTimeTakenByUserId(userId);
        Integer bestScore = quizAttemptRepository.getMaxScoreByUserId(userId);
        Integer worstScore = quizAttemptRepository.getMinScoreByUserId(userId);

        return new UserStatisticsDto(
                (int) totalQuizzesTaken,
                (int) totalQuizzesCompleted,
                averageScore != null ? averageScore : 0.0,
                (int) totalCorrectAnswers,
                (int) totalQuestions,
                correctRate,
                totalTimeTaken != null ? totalTimeTaken : 0,
                bestScore != null ? bestScore : 0,
                worstScore != null ? worstScore : 0
        );
    }

    /**
     * 최근 활동 조회
     */
    public List<RecentActivityDto> getRecentActivities(Long userId, int limit) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        List<RecentActivityDto> activities = new ArrayList<>();

        // Pageable 객체 생성 (0은 첫 페이지, limit은 페이지 크기)
        PageRequest pageRequest = PageRequest.of(0, limit);

        // 퀴즈 시도 활동
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        for (QuizAttempt attempt : recentAttempts) {
            activities.add(new RecentActivityDto(
                    attempt.getId(),
                    "QUIZ_ATTEMPT",
                    attempt.getQuiz().getId(),
                    attempt.getQuiz().getTitle(),
                    attempt.getScore(),
                    null,
                    null,
                    null,
                    formatDateTime(attempt.getCreatedAt())
            ));
        }

        // 업적 획득 활동
        List<Object[]> achievementRecords = achievementRepository.findRecentAchievementsByUserId(userId, limit);
        for (Object[] record : achievementRecords) {
            Long id = ((Number) record[0]).longValue();
            String achievementStr = (String) record[2];  // enum 문자열
            String achievementName = (String) record[3];
            LocalDateTime earnedAt = (LocalDateTime) record[4];

            // Achievement enum의 ordinal 값을 ID로 사용
            Long achievementId = null;
            try {
                Achievement achievement = Achievement.valueOf(achievementStr);
                achievementId = (long) achievement.ordinal();
            } catch (IllegalArgumentException e) {
                // 잘못된 enum 값이 있을 경우 로깅하고 계속 진행
                achievementId = -1L;  // 기본값 설정
            }

            activities.add(new RecentActivityDto(
                    id,
                    "ACHIEVEMENT_EARNED",
                    null,
                    null,
                    null,
                    achievementId,
                    achievementName,
                    null,
                    formatDateTime(earnedAt)
            ));
        }

        // 레벨업 활동
        List<Object[]> levelUpRecords = userLevelHistoryRepository.findRecentLevelUpsByUserId(userId, limit);
        for (Object[] record : levelUpRecords) {
            Long id = ((Number) record[0]).longValue();
            Integer oldLevel = ((Number) record[2]).intValue();
            Integer newLevel = ((Number) record[3]).intValue();
            LocalDateTime occurredAt = (LocalDateTime) record[4];

            activities.add(new RecentActivityDto(
                    id,
                    "LEVEL_UP",
                    null,
                    null,
                    null,
                    null,
                    null,
                    newLevel,
                    formatDateTime(occurredAt)
            ));
        }

        // 최신순 정렬 및 limit 적용
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDto::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 업적 조회
     */
    @Transactional // 읽기 전용 트랜잭션 해제
    public List<AchievementDto> getAchievements(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        // UserLevel 정보 조회 (없으면 새로 생성)
        UserLevel userLevel = userLevelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 사용자 조회
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
                    
                    // 로그 추가: 자동 UserLevel 생성
                    log.info("사용자 {} (ID: {})의 UserLevel이 없어 자동 생성합니다.", user.getUsername(), userId);
                    
                    // UserLevel 생성 및 저장
                    UserLevel newLevel = new UserLevel(user);
                    return userLevelRepository.save(newLevel);
                });

        // 사용자가 획득한 업적 목록
        Set<Achievement> earnedAchievements = userLevel.getAchievements();

        // 모든 업적 정보를 가져와 DTO로 변환
        List<AchievementDto> result = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            boolean isEarned = earnedAchievements.contains(achievement);
            String earnedAt = null;

            if (isEarned) {
                // 업적 획득 시간 조회 - 새로운 안전한 메서드 사용
                Optional<LocalDateTime> earnedAtTime = achievementRepository.findEarnedAtByUserIdAndAchievement(userId, achievement.name());
                if (earnedAtTime.isPresent()) {
                    earnedAt = formatDateTime(earnedAtTime.get());
                }
            }

            int progress = isEarned ? 100 : calculateAchievementProgress(userId, achievement);

            result.add(new AchievementDto(
                    (long) achievement.ordinal(),
                    achievement.getName(),
                    achievement.getDescription(),
                    achievement.getIconUrl(),
                    earnedAt,
                    progress,
                    achievement.getRequirementDescription()
            ));
        }

        return result;
    }

    /**
     * 주제별 성과 조회
     */
    public List<TopicPerformanceDto> getTopicPerformance(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        // 태그별 성과 데이터 조회 (태그별 퀴즈 시도, 평균 점수, 정답률)
        List<Object[]> tagPerformances = tagRepository.getTagPerformanceByUserId(userId);

        List<TopicPerformanceDto> result = new ArrayList<>();
        for (Object[] row : tagPerformances) {
            Long tagId = ((Number) row[0]).longValue();
            String tagName = (String) row[1];
            Long quizzesTaken = ((Number) row[2]).longValue();
            Double averageScore = (Double) row[3];
            Double correctRate = (Double) row[4];

            // 강점 여부 판단 (평균 점수가 75% 이상이면 강점으로 간주)
            boolean isStrength = averageScore >= 75.0;

            result.add(new TopicPerformanceDto(
                    tagId,
                    tagName,
                    quizzesTaken.intValue(),
                    averageScore,
                    correctRate,
                    isStrength
            ));
        }

        // 점수 순으로 정렬 (높은 것부터)
        return result.stream()
                .sorted(Comparator.comparing(TopicPerformanceDto::getAverageScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 프로필 정보 업데이트
     */
    @Transactional
    @CacheEvict(value = {"userProfile", "userStatistics"}, key = "#userId")
    public UserProfileDto updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));

        // 프로필 업데이트
        user.updateProfile(request.getUsername(), request.getProfileImage());
        userRepository.save(user);

        return getUserProfile(userId);
    }

    // 헬퍼 메서드

    private String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private int calculateAchievementProgress(Long userId, Achievement achievement) {
        // 업적 종류에 따른 진행도 계산 로직
        switch (achievement) {
            case FIRST_QUIZ_COMPLETED:
                long quizCount = quizAttemptRepository.countByUserId(userId);
                return quizCount > 0 ? 100 : 0;

            case PERFECT_SCORE:
                Integer bestScore = quizAttemptRepository.getMaxScoreByUserId(userId);
                return bestScore != null ? Math.min(bestScore, 100) : 0;

            case WINNING_STREAK_3:
            case WINNING_STREAK_5:
            case WINNING_STREAK_10:
                // 전체 승리 수로 대략적 진행도 표시
                User user = userRepository.findByIdWithStats(userId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                if (user.getBattleStats() == null) return 0;

                int targetStreak = 3;
                if (achievement == Achievement.WINNING_STREAK_5) targetStreak = 5;
                if (achievement == Achievement.WINNING_STREAK_10) targetStreak = 10;

                int currentStreak = user.getBattleStats().getCurrentStreak();
                return Math.min((currentStreak * 100) / targetStreak, 99); // 99%까지만 표시

            case DAILY_QUIZ_MASTER:
                // 연속 데일리 퀴즈 참여 일수 (7일 목표)
                int dailyStreak = quizAttemptRepository.getDailyQuizStreakByUserId(userId);
                return Math.min((dailyStreak * 100) / 7, 99); // 99%까지만 표시

            case QUICK_SOLVER:
                // 가장 빠른 풀이 시간 (초 단위, 30초 이내면 달성)
                Integer fastestTime = quizAttemptRepository.getFastestTimeTakenByUserId(userId);
                if (fastestTime == null) return 0;
                // 30초가 목표, 더 빠를수록 높은 진행도
                return fastestTime <= 30 ? 100 : Math.max(0, 100 - (fastestTime - 30) * 3);

            case KNOWLEDGE_SEEKER:
                // 다른 주제의 퀴즈 참여 수 (3개 목표)
                int topicCount = tagRepository.countDistinctTagsAttemptedByUserId(userId);
                return Math.min((topicCount * 100) / 3, 99); // 99%까지만 표시

            default:
                return 0;
        }
    }
}