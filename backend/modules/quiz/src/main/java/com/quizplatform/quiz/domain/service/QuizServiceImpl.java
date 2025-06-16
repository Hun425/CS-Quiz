package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuestionAttempt;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import com.quizplatform.quiz.domain.model.Tag;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizAttemptRepository;
import com.quizplatform.quiz.adapter.out.persistence.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 퀴즈 서비스 구현체
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizEventPublisher eventPublisher;
    private final TagService tagService;
    
    // 사용자 정보 캐시 (실제 구현에서는 Redis 등 분산 캐시 사용 권장)
    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();
    
    // 사용자 퀴즈 추천 설정 캐시
    private final Map<String, QuizRecommendationSettings> userRecommendations = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByCategory(String category) {
        return quizRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findByDifficulty(int difficulty) {
        return quizRepository.findByDifficulty(difficulty);
    }

    @Override
    @Transactional
    public QuizAttempt startQuizAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .userId(userId)
                .build();
        
        return quizAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public QuizAttempt submitQuizAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        
        // 퀴즈 시도 완료 처리
        boolean passed = attempt.complete();
        
        // 퀴즈 완료 이벤트 발행
        int experienceGained = calculateExperience(attempt);
        int pointsGained = calculatePoints(attempt);
        
        eventPublisher.publishQuizCompleted(
                attempt.getQuiz().getId().toString(),
                attempt.getUserId().toString(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                experienceGained,
                pointsGained,
                passed
        );
        
        // 업적 달성 여부 체크
        checkAndPublishAchievements(attempt);
        
        return quizAttemptRepository.save(attempt);
    }

    @Override
    public void handleNewUser(String userId, String username, String email) {
        // 사용자 정보 캐싱
        userCache.put(userId, new UserInfo(userId, username, email));
        
        // 초기 추천 설정 생성
        userRecommendations.put(userId, new QuizRecommendationSettings(1, 3));
        
        log.info("New user cached in Quiz module: {}", userId);
    }

    @Override
    public void adjustQuizRecommendationByLevel(String userId, int level) {
        // 레벨에 따른 퀴즈 추천 난이도 조정
        int recommendedDifficulty = Math.min(5, (level + 2) / 3);
        
        QuizRecommendationSettings settings = userRecommendations.getOrDefault(
                userId, new QuizRecommendationSettings(1, 3));
        
        settings.setDifficulty(recommendedDifficulty);
        userRecommendations.put(userId, settings);
        
        log.info("Adjusted quiz recommendation difficulty to {} for user {}", 
                recommendedDifficulty, userId);
    }
    
    /**
     * 획득 경험치 계산
     */
    private int calculateExperience(QuizAttempt attempt) {
        // 기본 경험치 = 문제당 10점 * 정답 개수
        int correctAnswers = (int) attempt.getQuestionAttempts().stream()
                .filter(QuestionAttempt::isCorrect)
                .count();
        
        int baseExp = correctAnswers * 10;
        
        // 난이도 보너스
        int difficultyBonus = attempt.getQuiz().getDifficulty() * 5;
        
        // 완료 보너스 (통과 시 추가 경험치)
        int completionBonus = attempt.isPassed() ? 50 : 0;
        
        return baseExp + difficultyBonus + completionBonus;
    }
    
    /**
     * 획득 포인트 계산
     */
    private int calculatePoints(QuizAttempt attempt) {
        // 기본 점수 = 경험치의 2배
        int basePoints = calculateExperience(attempt) * 2;
        
        // 정확도 보너스
        double accuracy = (double) attempt.getScore() / attempt.getTotalQuestions();
        int accuracyBonus = (int) (accuracy * 100);
        
        return basePoints + accuracyBonus;
    }
    
    /**
     * 업적 달성 체크 및 이벤트 발행
     */
    private void checkAndPublishAchievements(QuizAttempt attempt) {
        // 첫 퀴즈 완료 업적
        Map<String, Boolean> achievements = new HashMap<>();
        achievements.put("FIRST_QUIZ_COMPLETED", isFirstQuizCompleted(attempt.getUserId()));
        
        // 높은 점수 업적
        if (attempt.isPassed() && attempt.getScore() >= 90) {
            achievements.put("HIGH_SCORE", true);
        }
        
        // 달성한 업적에 대해 이벤트 발행
        achievements.forEach((type, achieved) -> {
            if (achieved) {
                eventPublisher.publishUserAchievement(
                        attempt.getUserId().toString(),
                        type,
                        getAchievementDescription(type),
                        getAchievementExperience(type),
                        getAchievementPoints(type)
                );
            }
        });
    }
    
    /**
     * 첫 퀴즈 완료 여부 확인
     */
    private boolean isFirstQuizCompleted(Long userId) {
        long count = quizAttemptRepository.countByUserIdAndCompleted(userId, true);
        return count == 1;
    }
    
    /**
     * 업적 설명 반환
     */
    private String getAchievementDescription(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> "첫 번째 퀴즈를 완료했습니다!";
            case "HIGH_SCORE" -> "90점 이상의 고득점을 달성했습니다!";
            default -> "업적을 달성했습니다!";
        };
    }
    
    /**
     * 업적 보너스 경험치 반환
     */
    private int getAchievementExperience(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> 50;
            case "HIGH_SCORE" -> 100;
            default -> 30;
        };
    }
    
    /**
     * 업적 보너스 포인트 반환
     */
    private int getAchievementPoints(String type) {
        return switch (type) {
            case "FIRST_QUIZ_COMPLETED" -> 100;
            case "HIGH_SCORE" -> 200;
            default -> 50;
        };
    }
    
    /**
     * 사용자 정보 내부 클래스
     */
    @lombok.Data
    private static class UserInfo {
        private final String userId;
        private final String username;
        private final String email;
        
        public UserInfo(String userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }
    }
    
    /**
     * 퀴즈 추천 설정 내부 클래스
     */
    @lombok.Data
    private static class QuizRecommendationSettings {
        private int difficulty;
        private int count;
        
        public QuizRecommendationSettings(int difficulty, int count) {
            this.difficulty = difficulty;
            this.count = count;
        }
    }
    
    // ===== 태그 관련 메서드 구현 =====
    
    /**
     * 퀴즈에 태그 추가
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz addTagToQuiz(Long quizId, Long tagId, Long currentUserId) {
        log.info("Adding tag {} to quiz {} by user {}", tagId, quizId, currentUserId);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        // 퀴즈 수정 권한 확인 (생성자 또는 관리자)
        if (!quiz.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        // 태그 조회 및 존재 여부 확인
        Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        
        // 태그 추가 가능 여부 확인
        if (!quiz.canAddMoreTags()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, 
                    String.format("퀴즈당 최대 %d개의 태그만 추가할 수 있습니다.", Tag.MAX_TAGS_PER_QUIZ));
        }
        
        // 이미 존재하는 태그인지 확인
        if (quiz.hasTag(tagId)) {
            log.debug("Tag {} already exists in quiz {}", tagId, quizId);
            return quiz;
        }
        
        // 태그 추가
        quiz.addTag(tag);
        
        return quizRepository.save(quiz);
    }
    
    /**
     * 퀴즈에서 태그 제거
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz removeTagFromQuiz(Long quizId, Long tagId, Long currentUserId) {
        log.info("Removing tag {} from quiz {} by user {}", tagId, quizId, currentUserId);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        // 퀴즈 수정 권한 확인 (생성자 또는 관리자)
        if (!quiz.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        // 태그가 존재하지 않으면 그대로 반환
        if (!quiz.hasTag(tagId)) {
            log.debug("Tag {} does not exist in quiz {}", tagId, quizId);
            return quiz;
        }
        
        // 태그 조회 및 제거
        Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        
        quiz.removeTag(tag);
        
        return quizRepository.save(quiz);
    }
    
    /**
     * 퀴즈의 모든 태그를 새로운 태그 목록으로 대체
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @param currentUserId 현재 사용자 ID
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz setQuizTags(Long quizId, Set<Long> tagIds, Long currentUserId) {
        log.info("Setting tags {} for quiz {} by user {}", tagIds, quizId, currentUserId);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        // 퀴즈 수정 권한 확인 (생성자 또는 관리자)
        if (!quiz.getCreatorId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        // 태그 개수 제한 확인
        if (tagIds.size() > Tag.MAX_TAGS_PER_QUIZ) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, 
                    String.format("퀴즈당 최대 %d개의 태그만 설정할 수 있습니다.", Tag.MAX_TAGS_PER_QUIZ));
        }
        
        // 모든 태그 존재 여부 확인
        Set<Tag> newTags = tagIds.stream()
                .map(tagId -> tagService.getTagById(tagId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                String.format("태그 ID %d를 찾을 수 없습니다.", tagId))))
                .collect(Collectors.toSet());
        
        // 태그 목록 설정
        quiz.setTags(newTags);
        
        return quizRepository.save(quiz);
    }
    
    /**
     * 특정 태그가 포함된 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @return 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findQuizzesByTag(Long tagId) {
        log.debug("Finding quizzes by tag: {}", tagId);
        
        // 활성화되고 공개된 퀴즈만 조회
        return quizRepository.findByTagIdAndPublishedAndActive(tagId, true, true);
    }
    
    /**
     * 특정 태그가 포함된 퀴즈 목록 조회 (태그 이름으로)
     * 
     * @param tagName 태그 이름
     * @return 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findQuizzesByTagName(String tagName) {
        log.debug("Finding quizzes by tag name: {}", tagName);
        
        return quizRepository.findByTagName(tagName).stream()
                .filter(Quiz::isPublished)
                .filter(Quiz::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 여러 태그가 모두 포함된 퀴즈 목록 조회 (AND 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @return 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findQuizzesWithAllTags(Set<Long> tagIds) {
        log.debug("Finding quizzes with all tags: {}", tagIds);
        
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return quizRepository.findByAllTags(new ArrayList<>(tagIds), tagIds.size()).stream()
                .filter(Quiz::isPublished)
                .filter(Quiz::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 여러 태그 중 하나라도 포함된 퀴즈 목록 조회 (OR 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @return 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findQuizzesWithAnyTags(Set<Long> tagIds) {
        log.debug("Finding quizzes with any tags: {}", tagIds);
        
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return quizRepository.findByAnyTags(new ArrayList<>(tagIds)).stream()
                .filter(Quiz::isPublished)
                .filter(Quiz::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 태그 기반 퀴즈 검색 (고급 필터링)
     * 
     * @param searchCriteria 검색 조건
     * @return 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> searchQuizzesWithTags(QuizTagSearchCriteria searchCriteria) {
        log.debug("Searching quizzes with criteria: {}", searchCriteria);
        
        QuizTagSearchCriteria criteria = searchCriteria.withDefaults();
        
        // 기본적인 태그 검색 수행
        List<Quiz> quizzes = performBasicTagSearch(criteria);
        
        // 태그 레벨 필터링
        quizzes = filterByTagLevel(quizzes, criteria.minTagLevel(), criteria.maxTagLevel());
        
        // 정렬 적용
        return applySorting(quizzes, criteria.sortBy(), criteria.ascending());
    }
    
    /**
     * 사용자의 취약 태그 기반 추천 퀴즈 조회
     * 
     * @param userId 사용자 ID
     * @param limit 조회할 퀴즈 수
     * @return 추천 퀴즈 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getRecommendedQuizzesForWeakTags(Long userId, int limit) {
        log.debug("Getting recommended quizzes for weak tags: userId={}, limit={}", userId, limit);
        
        // 사용자의 취약 태그 조회 (틀린 문제들의 태그)
        List<Long> weakTagIds = getWeakTagsForUser(userId);
        
        if (weakTagIds.isEmpty()) {
            log.debug("No weak tags found for user {}", userId);
            return Collections.emptyList();
        }
        
        // 취약 태그가 포함된 미시도 퀴즈 조회
        List<Quiz> recommendedQuizzes = quizRepository.findUnsolvedQuizzesWithTags(
                weakTagIds, userId, true, true);
        
        // 추천 점수 기반 정렬 (난이도, 인기도 등 고려)
        return recommendedQuizzes.stream()
                .sorted(this::compareQuizzesForRecommendation)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ===== 태그 관련 헬퍼 메서드 =====
    
    /**
     * 기본 태그 검색 수행
     * 
     * @param criteria 검색 조건
     * @return 퀴즈 목록
     */
    private List<Quiz> performBasicTagSearch(QuizTagSearchCriteria criteria) {
        List<Long> includeTagIds = criteria.includeTagIds() != null ? 
                new ArrayList<>(criteria.includeTagIds()) : null;
        List<Long> excludeTagIds = criteria.excludeTagIds() != null ? 
                new ArrayList<>(criteria.excludeTagIds()) : null;
        Long includeTagCount = includeTagIds != null ? (long) includeTagIds.size() : null;
        
        return quizRepository.findByTagCriteria(
                includeTagIds, includeTagCount, excludeTagIds,
                criteria.difficulty() != null ? Integer.parseInt(criteria.difficulty()) : null,
                criteria.category(), criteria.publishedOnly(), true);
    }
    
    /**
     * 태그 레벨 기반 필터링
     * 
     * @param quizzes 퀴즈 목록
     * @param minLevel 최소 레벨
     * @param maxLevel 최대 레벨
     * @return 필터링된 퀴즈 목록
     */
    private List<Quiz> filterByTagLevel(List<Quiz> quizzes, Integer minLevel, Integer maxLevel) {
        if (minLevel == null && maxLevel == null) {
            return quizzes;
        }
        
        return quizzes.stream()
                .filter(quiz -> quiz.getTags().stream()
                        .anyMatch(tag -> {
                            int level = tag.getLevel();
                            return (minLevel == null || level >= minLevel) &&
                                   (maxLevel == null || level <= maxLevel);
                        }))
                .collect(Collectors.toList());
    }
    
    /**
     * 퀴즈 목록 정렬 적용
     * 
     * @param quizzes 퀴즈 목록
     * @param sortBy 정렬 기준
     * @param ascending 오름차순 여부
     * @return 정렬된 퀴즈 목록
     */
    private List<Quiz> applySorting(List<Quiz> quizzes, String sortBy, boolean ascending) {
        Comparator<Quiz> comparator = switch (sortBy) {
            case "popularity" -> Comparator.comparing(Quiz::getAttemptCount);
            case "difficulty" -> Comparator.comparing(Quiz::getDifficulty);
            case "avgScore" -> Comparator.comparing(Quiz::getAvgScore);
            case "viewCount" -> Comparator.comparing(Quiz::getViewCount);
            default -> Comparator.comparing(Quiz::getCreatedAt);
        };
        
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        return quizzes.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 취약 태그 조회
     * 
     * @param userId 사용자 ID
     * @return 취약 태그 ID 목록
     */
    private List<Long> getWeakTagsForUser(Long userId) {
        // 사용자가 틀린 문제들의 태그 조회
        List<QuizAttempt> failedAttempts = quizAttemptRepository.findByUserIdAndCompleted(userId, true)
                .stream()
                .filter(attempt -> !attempt.isPassed())
                .collect(Collectors.toList());
        
        // 틀린 문제들의 태그 수집
        Map<Long, Integer> tagFailureCount = new HashMap<>();
        
        failedAttempts.forEach(attempt -> 
                attempt.getQuiz().getTags().forEach(tag -> 
                        tagFailureCount.merge(tag.getId(), 1, Integer::sum)));
        
        // 실패 횟수가 많은 태그 순으로 정렬하여 반환
        return tagFailureCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10) // 최대 10개의 취약 태그
                .collect(Collectors.toList());
    }
    
    /**
     * 추천용 퀴즈 비교자
     * 
     * @param q1 퀴즈 1
     * @param q2 퀴즈 2
     * @return 비교 결과
     */
    private int compareQuizzesForRecommendation(Quiz q1, Quiz q2) {
        // 1순위: 난이도 (낮은 순)
        int difficultyCompare = Integer.compare(q1.getDifficulty(), q2.getDifficulty());
        if (difficultyCompare != 0) return difficultyCompare;
        
        // 2순위: 인기도 (높은 순)
        int popularityCompare = Integer.compare(q2.getAttemptCount(), q1.getAttemptCount());
        if (popularityCompare != 0) return popularityCompare;
        
        // 3순위: 최신순
        return q2.getCreatedAt().compareTo(q1.getCreatedAt());
    }
    
    // ===== QuizApplicationService에서 필요한 추가 메서드 구현 =====
    
    /**
     * 특정 태그에 연결된 퀴즈 페이지 조회
     * 
     * @param tagId 태그 ID
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Quiz> getQuizzesByTag(Long tagId, Pageable pageable) {
        log.debug("Getting quizzes page by tag: tagId={}, page={}", tagId, pageable.getPageNumber());
        
        return quizRepository.findByTagIdAndPublishedAndActivePageable(tagId, true, true, pageable);
    }
    
    /**
     * 여러 태그 조건으로 퀴즈 페이지 조회
     * 
     * @param tagIds 태그 ID 목록
     * @param operator 논리 연산자 ("AND", "OR")
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Quiz> getQuizzesByTags(List<Long> tagIds, String operator, Pageable pageable) {
        log.debug("Getting quizzes page by tags: tagIds={}, operator={}, page={}", 
                 tagIds, operator, pageable.getPageNumber());
        
        if (tagIds == null || tagIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        if ("AND".equalsIgnoreCase(operator)) {
            return quizRepository.findByAllTagsPageable(tagIds, tagIds.size(), true, true, pageable);
        } else {
            return quizRepository.findByAnyTagsPageable(tagIds, true, true, pageable);
        }
    }
    
    /**
     * 고급 검색 (키워드 + 태그 조합) 페이지 조회
     * 
     * @param keyword 검색 키워드
     * @param tagIds 태그 ID 목록
     * @param category 카테고리
     * @param difficulty 난이도
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Quiz> advancedSearchQuizzes(String keyword, List<Long> tagIds, 
                                           String category, Integer difficulty, Pageable pageable) {
        log.debug("Advanced search quizzes: keyword={}, tagIds={}, category={}, difficulty={}, page={}", 
                 keyword, tagIds, category, difficulty, pageable.getPageNumber());
        
        return quizRepository.findByAdvancedCriteria(keyword, tagIds, category, difficulty, true, true, pageable);
    }
    
    /**
     * 퀴즈의 태그 목록 설정 (List 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz setQuizTags(Long quizId, List<Long> tagIds) {
        log.info("Setting quiz tags: quizId={}, tagIds={}", quizId, tagIds);
        
        return setQuizTags(quizId, tagIds != null ? new HashSet<>(tagIds) : Set.of(), null);
    }
    
    /**
     * 퀴즈에 태그 추가 (사용자 ID 없는 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz addTagToQuiz(Long quizId, Long tagId) {
        log.info("Adding tag to quiz: quizId={}, tagId={}", quizId, tagId);
        
        return addTagToQuiz(quizId, tagId, null);
    }
    
    /**
     * 퀴즈에서 태그 제거 (사용자 ID 없는 버전)
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @return 업데이트된 퀴즈
     */
    @Override
    @Transactional
    public Quiz removeTagFromQuiz(Long quizId, Long tagId) {
        log.info("Removing tag from quiz: quizId={}, tagId={}", quizId, tagId);
        
        return removeTagFromQuiz(quizId, tagId, null);
    }
} 