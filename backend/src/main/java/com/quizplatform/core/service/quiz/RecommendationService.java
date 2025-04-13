package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 퀴즈 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final TagRepository tagRepository;
    private final DailyQuizService dailyQuizService;
    private final EntityMapperService entityMapperService;
    
    // 사용자별 최근 퀴즈 분석 기간 (일)
    private static final int USER_HISTORY_DAYS = 30;
    // 사용자 데이터가 부족한 경우의 최소 시도 횟수
    private static final int MIN_ATTEMPTS_FOR_ANALYSIS = 3;
    // 인기 퀴즈 가중치 계수
    private static final double VIEW_WEIGHT = 0.4;
    private static final double ATTEMPT_WEIGHT = 0.3;
    private static final double SCORE_WEIGHT = 0.3;

    /**
     * 사용자 맞춤형 퀴즈 추천
     */
    public List<QuizSummaryResponse> getPersonalizedRecommendations(User user, int limit) {
        log.debug("사용자 맞춤형 퀴즈 추천 시작 - userId: {}, limit: {}", user.getId(), limit);
        
        // 1. 사용자의 최근 퀴즈 시도 분석
        List<QuizAttempt> recentAttempts = quizAttemptRepository
                .findRecentAttempts(user, LocalDateTime.now().minusDays(USER_HISTORY_DAYS));
        
        // 시도 횟수가 충분한지 확인
        if (recentAttempts.size() < MIN_ATTEMPTS_FOR_ANALYSIS) {
            log.debug("사용자 데이터 부족, 인기 퀴즈 추천으로 대체 - 시도 횟수: {}", recentAttempts.size());
            return getPopularQuizzes(limit);
        }
        
        // 2. 사용자가 잘하는 태그와 적절한 난이도 분석
        Map<Tag, UserPerformance> tagPerformance = analyzeUserPerformance(recentAttempts);
        DifficultyLevel recommendedDifficulty = calculateRecommendedDifficulty(recentAttempts);
        
        log.debug("사용자 분석 결과 - 추천 난이도: {}, 분석된 태그 수: {}", 
                recommendedDifficulty, tagPerformance.size());
        
        // 3. 사용자가 시도하지 않은 퀴즈 ID 목록 구하기
        Set<Long> attemptedQuizIds = recentAttempts.stream()
                .map(attempt -> attempt.getQuiz().getId())
                .collect(Collectors.toSet());
        
        // 4. 태그 기반 추천 퀴즈 후보 생성
        List<QuizWithScore> candidates = createPersonalizedCandidates(
                tagPerformance, recommendedDifficulty, attemptedQuizIds);
        
        // 5. 최종 추천 퀴즈 선택
        List<Quiz> recommendedQuizzes = selectFinalRecommendations(candidates, limit);
        
        log.debug("최종 추천 퀴즈 수: {}", recommendedQuizzes.size());
        
        // 6. 응답 DTO 변환
        return entityMapperService.mapToQuizSummaryResponseList(recommendedQuizzes);
    }
    
    /**
     * 인기 퀴즈 추천
     */
    public List<QuizSummaryResponse> getPopularQuizzes(int limit) {
        log.debug("인기 퀴즈 추천 시작 - limit: {}", limit);
        
        // 1. 조회수, 시도 횟수, 평균 점수를 고려하여 인기 퀴즈 목록 조회
        Sort sort = Sort.by(Sort.Direction.DESC, "viewCount");
        Pageable pageable = PageRequest.of(0, limit * 2, sort);
        
        // 2. 일반 퀴즈 중 공개된 퀴즈만 조회
        List<Quiz> popularQuizzes = quizRepository.findByQuizTypeAndIsPublicTrue(QuizType.REGULAR)
                .stream()
                .filter(quiz -> quiz.getQuestionCount() >= 3) // 최소 3문제 이상
                .sorted((q1, q2) -> {
                    // 인기도 점수 계산 및 정렬
                    double score1 = calculatePopularityScore(q1);
                    double score2 = calculatePopularityScore(q2);
                    return Double.compare(score2, score1); // 내림차순
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        log.debug("인기 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", popularQuizzes.size());
        
        // 3. 응답 DTO 변환
        return entityMapperService.mapToQuizSummaryResponseList(popularQuizzes);
    }
    
    /**
     * 카테고리(태그) 기반 추천 퀴즈
     */
    public List<QuizSummaryResponse> getCategoryRecommendations(Long tagId, int limit) {
        log.debug("카테고리 기반 퀴즈 추천 시작 - tagId: {}, limit: {}", tagId, limit);
        
        // 1. 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND, "태그를 찾을 수 없습니다: " + tagId));
        
        // 2. 태그 및 자식 태그 목록 조회
        List<Tag> relatedTags = tagRepository.findTagAndAllDescendants(tagId);
        
        log.debug("태그 및 관련 태그 총 {}개 조회됨", relatedTags.size());
        
        // 3. 관련 태그를 가진 퀴즈 중 인기 있는 퀴즈 선택
        Set<Long> tagIds = relatedTags.stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());
        
        List<Quiz> quizzes = new ArrayList<>();
        for (Tag relatedTag : relatedTags) {
            // 각 태그별로 관련 퀴즈 가져오기
            List<Quiz> tagQuizzes = quizRepository.findByTags(relatedTag, PageRequest.of(0, limit))
                    .getContent()
                    .stream()
                    .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR && quiz.isPublic())
                    .collect(Collectors.toList());
            
            quizzes.addAll(tagQuizzes);
        }
        
        // 중복 제거 및 인기도순 정렬
        List<Quiz> sortedQuizzes = quizzes.stream()
                .distinct()
                .sorted((q1, q2) -> {
                    double score1 = calculatePopularityScore(q1);
                    double score2 = calculatePopularityScore(q2);
                    return Double.compare(score2, score1); // 내림차순
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        log.debug("카테고리 기반 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", sortedQuizzes.size());
        
        // 4. 응답 DTO 변환
        return entityMapperService.mapToQuizSummaryResponseList(sortedQuizzes);
    }
    
    /**
     * 난이도 기반 추천 퀴즈
     */
    public List<QuizSummaryResponse> getDifficultyBasedRecommendations(String difficultyStr, int limit) {
        log.debug("난이도 기반 퀴즈 추천 시작 - difficulty: {}, limit: {}", difficultyStr, limit);
        
        // 1. 난이도 파싱
        DifficultyLevel difficulty;
        try {
            difficulty = DifficultyLevel.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "유효하지 않은 난이도입니다: " + difficultyStr);
        }
        
        // 2. 해당 난이도의 인기 퀴즈 선택
        List<Quiz> difficultyQuizzes = quizRepository.findByQuizTypeAndIsPublicTrue(QuizType.REGULAR)
                .stream()
                .filter(quiz -> quiz.getDifficultyLevel() == difficulty)
                .sorted((q1, q2) -> {
                    double score1 = calculatePopularityScore(q1);
                    double score2 = calculatePopularityScore(q2);
                    return Double.compare(score2, score1); // 내림차순
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        log.debug("난이도 기반 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", difficultyQuizzes.size());
        
        // 3. 응답 DTO 변환
        return entityMapperService.mapToQuizSummaryResponseList(difficultyQuizzes);
    }
    
    /**
     * 오늘의 퀴즈(데일리 퀴즈)와 관련된 추천 퀴즈
     */
    public List<QuizSummaryResponse> getDailyRelatedRecommendations(int limit) {
        log.debug("데일리 퀴즈 관련 추천 시작 - limit: {}", limit);
        
        try {
            // 1. 현재 데일리 퀴즈 조회
            Quiz dailyQuiz = dailyQuizService.getCurrentDailyQuiz();
            
            // 2. 데일리 퀴즈와 동일한 태그를 가진 퀴즈 조회
            Set<Tag> dailyTags = dailyQuiz.getTags();
            DifficultyLevel dailyDifficulty = dailyQuiz.getDifficultyLevel();
            
            log.debug("데일리 퀴즈 분석 - 태그 수: {}, 난이도: {}", 
                    dailyTags.size(), dailyDifficulty);
            
            // 3. 관련 퀴즈 선택
            List<Quiz> relatedQuizzes = new ArrayList<>();
            for (Tag tag : dailyTags) {
                // 같은 태그의 퀴즈 가져오기
                List<Quiz> tagQuizzes = quizRepository.findByTags(tag, PageRequest.of(0, limit))
                        .getContent()
                        .stream()
                        .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR && quiz.isPublic()
                                && !quiz.getId().equals(dailyQuiz.getId()))
                        .collect(Collectors.toList());
                
                relatedQuizzes.addAll(tagQuizzes);
            }
            
            // 중복 제거 및 인기도순 정렬
            List<Quiz> sortedQuizzes = relatedQuizzes.stream()
                    .distinct()
                    .sorted((q1, q2) -> {
                        // 난이도 근접성 고려하여 정렬
                        int diffDiff1 = Math.abs(q1.getDifficultyLevel().ordinal() - dailyDifficulty.ordinal());
                        int diffDiff2 = Math.abs(q2.getDifficultyLevel().ordinal() - dailyDifficulty.ordinal());
                        if (diffDiff1 != diffDiff2) {
                            return Integer.compare(diffDiff1, diffDiff2); // 난이도 차이가 적은 것 우선
                        }
                        
                        // 난이도 차이가 같으면 인기도 비교
                        double score1 = calculatePopularityScore(q1);
                        double score2 = calculatePopularityScore(q2);
                        return Double.compare(score2, score1); // 내림차순
                    })
                    .limit(limit)
                    .collect(Collectors.toList());
            
            log.debug("데일리 퀴즈 관련 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", sortedQuizzes.size());
            
            // 4. 응답 DTO 변환
            return entityMapperService.mapToQuizSummaryResponseList(sortedQuizzes);
            
        } catch (BusinessException e) {
            // 데일리 퀴즈가 없는 경우 인기 퀴즈 반환
            log.warn("데일리 퀴즈를 찾을 수 없어 인기 퀴즈로 대체합니다: {}", e.getMessage());
            return getPopularQuizzes(limit);
        }
    }
    
    // ===== 내부 헬퍼 메서드 =====
    
    /**
     * 사용자의 퀴즈 시도 데이터 분석
     */
    private Map<Tag, UserPerformance> analyzeUserPerformance(List<QuizAttempt> attempts) {
        Map<Tag, List<Integer>> tagScores = new HashMap<>();
        Map<Tag, Integer> tagCounts = new HashMap<>();
        
        // 각 시도에서 퀴즈의 태그와 점수를 추출
        for (QuizAttempt attempt : attempts) {
            int score = attempt.getScore();
            int timeTaken = attempt.getTimeTaken() != null ? attempt.getTimeTaken() : 0;
            
            // 퀴즈의 각 태그에 점수 기록
            for (Tag tag : attempt.getQuiz().getTags()) {
                tagScores.computeIfAbsent(tag, k -> new ArrayList<>()).add(score);
                tagCounts.merge(tag, 1, Integer::sum);
            }
        }
        
        // 태그별 성과 계산
        Map<Tag, UserPerformance> tagPerformance = new HashMap<>();
        tagScores.forEach((tag, scores) -> {
            double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            int count = tagCounts.getOrDefault(tag, 0);
            tagPerformance.put(tag, new UserPerformance(avgScore, count));
        });
        
        return tagPerformance;
    }
    
    /**
     * 사용자 시도 데이터 기반으로 적절한 난이도 추천
     */
    private DifficultyLevel calculateRecommendedDifficulty(List<QuizAttempt> attempts) {
        // 사용자의 평균 점수로 적절한 난이도 계산
        double avgScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);
        
        // 난이도별 평균 점수
        if (avgScore >= 85) {
            return DifficultyLevel.ADVANCED;
        } else if (avgScore >= 65) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            return DifficultyLevel.BEGINNER;
        }
    }
    
    /**
     * 인기도 점수 계산 (조회수, 시도 횟수, 평균 점수 고려)
     */
    private double calculatePopularityScore(Quiz quiz) {
        // 조회수 정규화 (최대 1000회 기준)
        double viewScore = Math.min(1.0, quiz.getViewCount() / 1000.0) * VIEW_WEIGHT;
        
        // 시도 횟수 정규화 (최대 100회 기준)
        double attemptScore = Math.min(1.0, quiz.getAttemptCount() / 100.0) * ATTEMPT_WEIGHT;
        
        // 평균 점수 정규화 (0-100점 기준)
        double scoreValue = (quiz.getAvgScore() / 100.0) * SCORE_WEIGHT;
        
        return viewScore + attemptScore + scoreValue;
    }
    
    /**
     * 개인화된 퀴즈 후보 목록 생성
     */
    private List<QuizWithScore> createPersonalizedCandidates(
            Map<Tag, UserPerformance> tagPerformance, 
            DifficultyLevel recommendedDifficulty,
            Set<Long> attemptedQuizIds) {
        
        // 상위 성과를 보인 태그 5개 선택
        List<Map.Entry<Tag, UserPerformance>> topTags = tagPerformance.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAvgScore(), e1.getValue().getAvgScore()))
                .limit(5)
                .collect(Collectors.toList());
        
        // 퀴즈 후보 목록과 점수
        List<QuizWithScore> candidates = new ArrayList<>();
        
        // 각 태그에 대해 관련 퀴즈 가져오기
        for (Map.Entry<Tag, UserPerformance> tagEntry : topTags) {
            Tag tag = tagEntry.getKey();
            double tagWeight = tagEntry.getValue().getAvgScore() / 100.0; // 태그 가중치
            
            // 이 태그에 해당하는 모든 퀴즈 조회
            List<Quiz> tagQuizzes = quizRepository.findByTags(tag, PageRequest.of(0, 20))
                    .getContent()
                    .stream()
                    .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR 
                            && quiz.isPublic()
                            && !attemptedQuizIds.contains(quiz.getId()))
                    .collect(Collectors.toList());
            
            // 각 퀴즈에 점수 할당
            for (Quiz quiz : tagQuizzes) {
                // 난이도 근접성 (0-1 사이 값)
                double difficultyMatch = 1.0 - (Math.abs(quiz.getDifficultyLevel().ordinal() 
                        - recommendedDifficulty.ordinal()) / 2.0);
                
                // 최종 점수 계산 (태그 관련성 + 난이도 근접성 + 인기도)
                double score = (tagWeight * 0.5) + (difficultyMatch * 0.3) 
                        + (calculatePopularityScore(quiz) * 0.2);
                
                candidates.add(new QuizWithScore(quiz, score));
            }
        }
        
        return candidates;
    }
    
    /**
     * 최종 추천 퀴즈 선택
     */
    private List<Quiz> selectFinalRecommendations(List<QuizWithScore> candidates, int limit) {
        // 1. 중복 제거 (같은 퀴즈가 여러 태그에서 선택될 수 있음)
        Map<Long, QuizWithScore> uniqueCandidates = new HashMap<>();
        for (QuizWithScore candidate : candidates) {
            Long quizId = candidate.getQuiz().getId();
            if (!uniqueCandidates.containsKey(quizId) 
                    || uniqueCandidates.get(quizId).getScore() < candidate.getScore()) {
                uniqueCandidates.put(quizId, candidate);
            }
        }
        
        // 2. 점수 기준 정렬
        List<QuizWithScore> sortedCandidates = new ArrayList<>(uniqueCandidates.values());
        sortedCandidates.sort((c1, c2) -> Double.compare(c2.getScore(), c1.getScore()));
        
        // 3. 상위 N개 선택
        return sortedCandidates.stream()
                .limit(limit)
                .map(QuizWithScore::getQuiz)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자 태그별 성과 정보
     */
    private static class UserPerformance {
        private final double avgScore;
        private final int attemptCount;
        
        public UserPerformance(double avgScore, int attemptCount) {
            this.avgScore = avgScore;
            this.attemptCount = attemptCount;
        }
        
        public double getAvgScore() {
            return avgScore;
        }
        
        public int getAttemptCount() {
            return attemptCount;
        }
    }
    
    /**
     * 퀴즈 추천 후보 (퀴즈와 점수)
     */
    private static class QuizWithScore {
        private final Quiz quiz;
        private final double score;
        
        public QuizWithScore(Quiz quiz, double score) {
            this.quiz = quiz;
            this.score = score;
        }
        
        public Quiz getQuiz() {
            return quiz;
        }
        
        public double getScore() {
            return score;
        }
    }
}