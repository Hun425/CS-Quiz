package com.quizplatform.core.service.user.impl;

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
import com.quizplatform.core.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserService 인터페이스의 구현체
 * 사용자 프로필 조회, 통계 계산, 최근 활동, 업적, 주제별 성과 조회 및 프로필 업데이트 기능을 구현합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final TagRepository tagRepository;
    private final AchievementRepository achievementRepository; // UserAchievementHistoryRepository
    private final UserLevelHistoryRepository userLevelHistoryRepository;
    private final EntityMapperService entityMapperService;

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfileDto getUserProfile(Long userId) {
        // 보안 체크: 본인 또는 관리자만 프로필 조회 가능
        validateUserAccess(userId);
        
        // 개선: DTO 직접 조회로 변경하여 N+1 문제 해결
        return userRepository.findUserProfileDtoById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));
    }
    
    /**
     * 사용자 접근 권한을 검증합니다.
     * 본인 또는 관리자만 접근할 수 있습니다.
     */
    private void validateUserAccess(Long targetUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "현재 사용자를 찾을 수 없습니다"));
            
            // 본인이거나 관리자인 경우만 허용
            boolean isOwner = currentUser.getId().equals(targetUserId);
            boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
            
            if (!isOwner && !isAdmin) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "다른 사용자의 프로필에 접근할 수 없습니다");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "userStatistics", key = "#userId") // 결과를 캐시에 저장
    public UserStatisticsDto getUserStatistics(Long userId) {
        // 개선: 단일 최적화 쿼리로 변경하여 N+1 문제 해결
        Map<String, Object> stats = userRepository.getUserStatisticsData(userId);
        if (stats == null || stats.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }
        
        // null 값 처리 (결과가 null인 경우 기본값 사용)
        int totalQuizzesTaken = stats.get("total_quizzes") != null ? ((Number) stats.get("total_quizzes")).intValue() : 0;
        int totalQuizzesCompleted = stats.get("completed_quizzes") != null ? ((Number) stats.get("completed_quizzes")).intValue() : 0;
        double averageScore = stats.get("avg_score") != null ? ((Number) stats.get("avg_score")).doubleValue() : 0.0;
        int totalCorrectAnswers = stats.get("correct_answers") != null ? ((Number) stats.get("correct_answers")).intValue() : 0;
        int totalQuestions = stats.get("total_questions") != null ? ((Number) stats.get("total_questions")).intValue() : 0;
        double correctRate = stats.get("correct_rate") != null ? ((Number) stats.get("correct_rate")).doubleValue() : 0.0;
        int totalTimeTaken = stats.get("total_time") != null ? ((Number) stats.get("total_time")).intValue() : 0;
        int bestScore = stats.get("best_score") != null ? ((Number) stats.get("best_score")).intValue() : 0;
        int worstScore = stats.get("worst_score") != null ? ((Number) stats.get("worst_score")).intValue() : 0;
        
        // 계산된 통계 정보로 DTO 생성하여 반환
        return new UserStatisticsDto(
                totalQuizzesTaken,
                totalQuizzesCompleted,
                averageScore,
                totalCorrectAnswers,
                totalQuestions,
                correctRate,
                totalTimeTaken,
                bestScore,
                worstScore
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecentActivityDto> getRecentActivities(Long userId, int limit) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        List<RecentActivityDto> activities = new ArrayList<>();

        // 페이징 처리를 위한 Pageable 객체 생성 (최신순 정렬은 각 Repository 쿼리에서 처리)
        PageRequest pageRequest = PageRequest.of(0, limit);

        // 1. 최근 퀴즈 시도 활동 조회
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
        for (QuizAttempt attempt : recentAttempts) {
            activities.add(new RecentActivityDto(
                    attempt.getId(), // 활동 고유 ID (QuizAttempt ID)
                    "QUIZ_ATTEMPT",  // 활동 유형
                    attempt.getQuiz().getId(), // 관련 퀴즈 ID
                    attempt.getQuiz().getTitle(), // 관련 퀴즈 제목
                    attempt.getScore(), // 획득 점수
                    null, // 업적 ID (해당 없음)
                    null, // 업적 이름 (해당 없음)
                    null, // 레벨 (해당 없음)
                    formatDateTime(attempt.getCreatedAt()) // 활동 발생 시각
            ));
        }

        // 2. 최근 업적 획득 활동 조회
        // Repository는 Object[] 형태로 결과를 반환할 수 있음: [이력ID, 사용자ID, 업적Enum명, 업적명, 획득시각]
        List<Object[]> achievementRecords = achievementRepository.findRecentAchievementsByUserId(userId, limit);
        for (Object[] record : achievementRecords) {
            Long historyId = ((Number) record[0]).longValue(); // 이력 테이블의 ID
            String achievementStr = (String) record[2]; // 업적 Enum 이름
            String achievementName = (String) record[3]; // 업적 명칭
            LocalDateTime earnedAt = (LocalDateTime) record[4]; // 획득 시각

            // 업적 Enum 이름(achievementStr)을 사용하여 업적 ID(ordinal) 결정
            Long achievementId = -1L; // 기본값
            try {
                Achievement achievement = Achievement.valueOf(achievementStr);
                achievementId = (long) achievement.ordinal(); // Enum의 순서(ordinal)를 ID로 사용
            } catch (IllegalArgumentException e) {
                log.warn("알 수 없는 업적 Enum 값 '{}' 발견. historyId={}", achievementStr, historyId);
            }

            activities.add(new RecentActivityDto(
                    historyId, // 활동 고유 ID (UserAchievementHistory ID)
                    "ACHIEVEMENT_EARNED", // 활동 유형
                    null, // 퀴즈 ID (해당 없음)
                    null, // 퀴즈 제목 (해당 없음)
                    null, // 점수 (해당 없음)
                    achievementId, // 업적 ID (ordinal 값)
                    achievementName, // 업적 이름
                    null, // 레벨 (해당 없음)
                    formatDateTime(earnedAt) // 활동 발생 시각
            ));
        }

        // 3. 최근 레벨업 활동 조회
        // Repository는 Object[] 형태로 결과를 반환할 수 있음: [이력ID, 사용자ID, 이전레벨, 새레벨, 발생시각]
        List<Object[]> levelUpRecords = userLevelHistoryRepository.findRecentLevelUpsByUserId(userId, limit);
        for (Object[] record : levelUpRecords) {
            Long historyId = ((Number) record[0]).longValue(); // 이력 테이블의 ID
            // Integer oldLevel = ((Number) record[2]).intValue(); // 이전 레벨 (필요 시 사용)
            Integer newLevel = ((Number) record[3]).intValue(); // 도달한 새 레벨
            LocalDateTime occurredAt = (LocalDateTime) record[4]; // 발생 시각

            activities.add(new RecentActivityDto(
                    historyId, // 활동 고유 ID (UserLevelHistory ID)
                    "LEVEL_UP", // 활동 유형
                    null, // 퀴즈 ID (해당 없음)
                    null, // 퀴즈 제목 (해당 없음)
                    null, // 점수 (해당 없음)
                    null, // 업적 ID (해당 없음)
                    null, // 업적 이름 (해당 없음)
                    newLevel, // 도달 레벨
                    formatDateTime(occurredAt) // 활동 발생 시각
            ));
        }

        // 4. 모든 활동 내역을 타임스탬프 기준 내림차순(최신순)으로 정렬하고 개수 제한
        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional // UserLevel 자동 생성 시 쓰기 작업이 필요할 수 있으므로 readOnly 해제
    public List<AchievementDto> getAchievements(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        // 사용자의 UserLevel 정보 조회 (없으면 자동 생성 로직 포함)
        UserLevel userLevel = userLevelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // UserLevel이 없는 경우, User 엔티티를 조회하여 UserLevel 생성
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "UserLevel 생성 중 사용자를 찾을 수 없습니다: " + userId));
                    log.info("사용자 {} (ID: {})의 UserLevel 정보가 없어 새로 생성합니다.", user.getUsername(), userId);
                    UserLevel newLevel = new UserLevel(user);
                    return userLevelRepository.save(newLevel); // 생성된 UserLevel 저장 및 반환
                });

        // 사용자가 획득한 업적 목록 (UserLevel 엔티티 내 Set)
        Set<Achievement> earnedAchievements = userLevel.getAchievements();

        List<AchievementDto> result = new ArrayList<>();
        // 모든 Achievement Enum 값 순회
        for (Achievement achievement : Achievement.values()) {
            boolean isEarned = earnedAchievements.contains(achievement); // 획득 여부 확인
            String earnedAt = null; // 획득 시각 (획득 시에만 설정)

            // 획득한 업적인 경우, 획득 시각 조회
            if (isEarned) {
                Optional<LocalDateTime> earnedAtTime = achievementRepository.findEarnedAtByUserIdAndAchievement(userId, achievement.name());
                earnedAt = earnedAtTime.map(this::formatDateTime).orElse(null); // 조회된 시간 포맷팅
            }

            // 업적 진행도 계산 (획득 시 100%, 미획득 시 계산 로직 호출)
            int progress = isEarned ? 100 : calculateAchievementProgress(userId, achievement);

            // AchievementDto 생성 및 리스트 추가
            result.add(new AchievementDto(
                    (long) achievement.ordinal(), // Enum 순서를 ID로 사용
                    achievement.getName(), // 업적 명칭
                    achievement.getDescription(), // 업적 설명
                    achievement.getIconUrl(), // 아이콘 URL
                    earnedAt, // 획득 시각 (문자열)
                    progress, // 진행도 (%)
                    achievement.getRequirementDescription() // 달성 조건 설명
            ));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopicPerformanceDto> getTopicPerformance(Long userId) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId);
        }

        // Repository를 통해 태그별 성과 데이터 조회 (JPQL 또는 Native Query 결과)
        // 결과 형태: List<Object[]>, 각 Object[] = [tagId, tagName, quizzesTaken, averageScore, correctRate]
        List<Object[]> tagPerformances = tagRepository.getTagPerformanceByUserId(userId);

        List<TopicPerformanceDto> result = new ArrayList<>();
        for (Object[] row : tagPerformances) {
            // 각 row에서 데이터 추출 및 타입 변환
            Long tagId = ((Number) row[0]).longValue();
            String tagName = (String) row[1];
            Long quizzesTaken = ((Number) row[2]).longValue();
            // DB에서 Double 또는 BigDecimal 등으로 반환될 수 있으므로 null 체크 후 형변환
            Double averageScore = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            Double correctRate = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            // 강점 여부 판단 (예: 평균 점수가 75점 이상이면 강점)
            boolean isStrength = averageScore >= 75.0;

            // TopicPerformanceDto 생성 및 리스트 추가
            result.add(new TopicPerformanceDto(
                    tagId,
                    tagName,
                    quizzesTaken.intValue(),
                    averageScore,
                    correctRate,
                    isStrength
            ));
        }

        // 평균 점수(averageScore) 기준 내림차순(높은 점수 순)으로 정렬
        return result.stream()
                .sorted(Comparator.comparing(TopicPerformanceDto::getAverageScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional // 쓰기 작업이므로 readOnly 해제
    @CacheEvict(value = {"userProfile", "userStatistics"}, key = "#userId", allEntries = false) // 업데이트 시 관련 캐시 제거
    public UserProfileDto updateProfile(Long userId, UserProfileUpdateRequest request) {
        // 사용자 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userId));

        // User 엔티티의 프로필 업데이트 메서드 호출
        user.updateProfile(request.getUsername(), request.getProfileImage());
        // 변경된 User 엔티티 저장 (JPA dirty checking으로 자동 저장될 수도 있지만 명시적 호출 권장)
        userRepository.save(user);
        log.info("User profile updated for userId: {}", userId);

        // 업데이트된 프로필 정보를 다시 조회하여 반환
        return getUserProfile(userId);
    }

    // --- 헬퍼 메서드 ---

    /**
     * ZonedDateTime 객체를 ISO 오프셋 날짜/시간 형식의 문자열로 변환합니다. (내부 헬퍼 메서드)
     * 입력이 null이면 null을 반환합니다.
     *
     * @param dateTime 변환할 ZonedDateTime 객체
     * @return ISO 8601 오프셋 날짜/시간 형식 문자열 (예: "2023-10-27T10:15:30+09:00") 또는 null
     */
    private String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * LocalDateTime 객체를 ISO 로컬 날짜/시간 형식의 문자열로 변환합니다. (내부 헬퍼 메서드)
     * 입력이 null이면 null을 반환합니다.
     *
     * @param dateTime 변환할 LocalDateTime 객체
     * @return ISO 8601 로컬 날짜/시간 형식 문자열 (예: "2023-10-27T10:15:30") 또는 null
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * 특정 사용자의 특정 업적에 대한 현재 진행률(%)을 계산합니다. (내부 헬퍼 메서드)
     * 업적 종류에 따라 필요한 데이터를 조회하여 계산 로직을 적용합니다.
     *
     * @param userId      진행률을 계산할 사용자 ID
     * @param achievement 진행률을 계산할 업적 Enum 값
     * @return 업적 진행률 (0 ~ 100 사이의 정수)
     */
    private int calculateAchievementProgress(Long userId, Achievement achievement) {
        // 업적 종류별로 분기하여 진행도 계산
        switch (achievement) {
            case FIRST_QUIZ_COMPLETED:
                // 사용자의 총 퀴즈 시도 횟수 조회
                long quizCount = quizAttemptRepository.countByUserId(userId);
                // 시도 횟수가 0보다 크면 100%, 아니면 0%
                return quizCount > 0 ? 100 : 0;

            case PERFECT_SCORE:
                // 사용자의 최고 점수 조회
                Integer bestScore = quizAttemptRepository.getMaxScoreByUserId(userId);
                // 최고 점수가 null이 아니면 점수(최대 100), null이면 0%
                return bestScore != null ? Math.min(bestScore, 100) : 0;

            case WINNING_STREAK_3:
            case WINNING_STREAK_5:
            case WINNING_STREAK_10:
                // 목표 연승 횟수 설정
                int targetStreak;
                if (achievement == Achievement.WINNING_STREAK_5) targetStreak = 5;
                else if (achievement == Achievement.WINNING_STREAK_10) targetStreak = 10;
                else targetStreak = 3;

                // 사용자 배틀 통계에서 현재 연승 횟수 조회
                User userWithStats = userRepository.findByIdWithStats(userId).orElse(null);
                if (userWithStats == null || userWithStats.getBattleStats() == null) return 0;
                int currentStreak = userWithStats.getBattleStats().getCurrentStreak();

                // (현재 연승 / 목표 연승) * 100, 최대 99%까지만 표시 (100%는 달성 시점)
                return Math.min((currentStreak * 100) / targetStreak, 99);

            case DAILY_QUIZ_MASTER:
                // 사용자의 현재 연속 데일리 퀴즈 완료 일수 조회
                int dailyStreak = quizAttemptRepository.getDailyQuizStreakByUserId(userId);
                // (현재 연속 일수 / 7일) * 100, 최대 99%까지만 표시
                return Math.min((dailyStreak * 100) / 7, 99);

            case QUICK_SOLVER:
                // 사용자의 가장 빠른 퀴즈 완료 시간(초) 조회
                Integer fastestTime = quizAttemptRepository.getFastestTimeTakenByUserId(userId);
                if (fastestTime == null) return 0;
                // 30초 이내면 100%, 초과 시 시간에 반비례하여 감소 (최소 0%)
                return fastestTime <= 30 ? 100 : Math.max(0, 100 - (fastestTime - 30) * 3);

            case KNOWLEDGE_SEEKER:
                // 사용자가 시도한 퀴즈의 고유 태그(주제) 개수 조회
                int topicCount = tagRepository.countDistinctTagsAttemptedByUserId(userId);
                // (현재 주제 수 / 3개) * 100, 최대 99%까지만 표시
                return Math.min((topicCount * 100) / 3, 99);

            default:
                // 정의되지 않은 업적은 진행도 0%
                return 0;
        }
    }
} 