package com.quizplatform.core.service.quiz.impl;

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
import com.quizplatform.core.service.quiz.DailyQuizService;
import com.quizplatform.core.service.quiz.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RecommendationService 인터페이스의 구현체
 * 사용자에게 다양한 유형의 퀴즈를 추천하는 로직을 담당합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용
public class RecommendationServiceImpl implements RecommendationService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final TagRepository tagRepository;
    private final DailyQuizService dailyQuizService;
    private final EntityMapperService entityMapperService;

    // 사용자별 최근 퀴즈 시도 분석 기간 (일)
    private static final int USER_HISTORY_DAYS = 30;
    // 개인화 분석을 위한 최소 퀴즈 시도 횟수
    private static final int MIN_ATTEMPTS_FOR_ANALYSIS = 3;
    // 인기도 점수 계산 시 조회수 가중치
    private static final double VIEW_WEIGHT = 0.4;
    // 인기도 점수 계산 시 시도 횟수 가중치
    private static final double ATTEMPT_WEIGHT = 0.3;
    // 인기도 점수 계산 시 평균 점수 가중치
    private static final double SCORE_WEIGHT = 0.3;

    /**
     * 사용자의 최근 퀴즈 시도 기록을 분석하여 맞춤형 퀴즈를 추천합니다.
     * 사용자가 선호하는 태그와 적정 난이도를 파악하여, 시도하지 않은 퀴즈 중 관련성이 높은 퀴즈를 추천합니다.
     * 사용자 데이터가 부족할 경우 인기 퀴즈를 대신 추천합니다.
     *
     * @param user  추천을 받을 사용자
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 추천된 퀴즈 요약 정보 DTO 리스트
     */
    @Override
    public List<QuizSummaryResponse> getPersonalizedRecommendations(User user, int limit) {
        log.debug("사용자 맞춤형 퀴즈 추천 시작 - userId: {}, limit: {}", user.getId(), limit);

        // 1. 사용자의 최근 퀴즈 시도 기록 조회 (최근 USER_HISTORY_DAYS일 간)
        List<QuizAttempt> recentAttempts = quizAttemptRepository
                .findRecentAttempts(user, LocalDateTime.now().minusDays(USER_HISTORY_DAYS));

        // 2. 시도 횟수가 분석 기준(MIN_ATTEMPTS_FOR_ANALYSIS) 미만이면 인기 퀴즈 추천으로 전환
        if (recentAttempts.size() < MIN_ATTEMPTS_FOR_ANALYSIS) {
            log.debug("사용자 퀴즈 시도 데이터 부족 ({}회 < {}회), 인기 퀴즈 추천으로 대체",
                    recentAttempts.size(), MIN_ATTEMPTS_FOR_ANALYSIS);
            return getPopularQuizzes(limit);
        }

        // 3. 사용자 성과 분석 (태그별 평균 점수, 시도 횟수)
        Map<Tag, UserPerformance> tagPerformance = analyzeUserPerformance(recentAttempts);
        // 4. 사용자 평균 점수 기반 추천 난이도 계산
        DifficultyLevel recommendedDifficulty = calculateRecommendedDifficulty(recentAttempts);

        log.debug("사용자 분석 결과 - 추천 난이도: {}, 분석된 태그 수: {}",
                recommendedDifficulty, tagPerformance.size());

        // 5. 사용자가 이미 시도한 퀴즈 ID 목록 생성
        Set<Long> attemptedQuizIds = recentAttempts.stream()
                .map(attempt -> attempt.getQuiz().getId())
                .collect(Collectors.toSet());

        // 6. 분석 결과(태그 성과, 추천 난이도)를 바탕으로 추천 퀴즈 후보 생성 (점수 포함)
        List<QuizWithScore> candidates = createPersonalizedCandidates(
                tagPerformance, recommendedDifficulty, attemptedQuizIds);

        // 7. 후보 퀴즈 중 최종 추천 퀴즈 선택 (중복 제거 및 점수 순 정렬)
        List<Quiz> recommendedQuizzes = selectFinalRecommendations(candidates, limit);

        log.debug("최종 맞춤 추천 퀴즈 수: {}", recommendedQuizzes.size());

        // 8. 결과를 DTO로 변환하여 반환
        return entityMapperService.mapToQuizSummaryResponseList(recommendedQuizzes);
    }

    /**
     * 조회수, 시도 횟수, 평균 점수 등을 종합적으로 고려하여 인기 있는 퀴즈를 추천합니다.
     *
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 인기 퀴즈 요약 정보 DTO 리스트
     */
    @Override
    public List<QuizSummaryResponse> getPopularQuizzes(int limit) {
        log.debug("인기 퀴즈 추천 시작 - limit: {}", limit);

        // 1. 일반(REGULAR) 타입의 공개된(isPublic=true) 퀴즈 목록 조회
        List<Quiz> popularQuizzes = quizRepository.findByQuizTypeAndIsPublicTrue(QuizType.REGULAR)
                .stream()
                .filter(quiz -> quiz.getQuestionCount() >= 3) // 최소 3문제 이상인 퀴즈만 필터링
                .sorted((q1, q2) -> {
                    // 각 퀴즈의 인기도 점수 계산
                    double score1 = calculatePopularityScore(q1);
                    double score2 = calculatePopularityScore(q2);
                    // 점수가 높은 순서(내림차순)로 정렬
                    return Double.compare(score2, score1);
                })
                .limit(limit) // 요청된 개수만큼 제한
                .collect(Collectors.toList());

        log.debug("인기 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", popularQuizzes.size());

        // 2. 결과를 DTO로 변환하여 반환
        return entityMapperService.mapToQuizSummaryResponseList(popularQuizzes);
    }

    /**
     * 특정 카테고리(태그) 및 그 하위 카테고리에 속하는 퀴즈 중에서 인기 있는 퀴즈를 추천합니다.
     *
     * @param tagId 추천 기준이 되는 태그(카테고리)의 ID
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 카테고리 기반 추천 퀴즈 요약 정보 DTO 리스트
     * @throws BusinessException 해당 tagId의 태그를 찾을 수 없을 경우 (TAG_NOT_FOUND)
     */
    @Override
    public List<QuizSummaryResponse> getCategoryRecommendations(Long tagId, int limit) {
        log.debug("카테고리 기반 퀴즈 추천 시작 - tagId: {}, limit: {}", tagId, limit);

        // 1. 요청된 tagId로 태그 조회 (없으면 예외 발생)
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND, "태그를 찾을 수 없습니다: " + tagId));

        // 2. 해당 태그와 모든 하위 태그 목록 조회
        List<Tag> relatedTags = tagRepository.findTagAndAllDescendants(tagId);
        log.debug("태그 ID {} 및 관련 하위 태그 총 {}개 조회됨", tagId, relatedTags.size());

        // 3. 관련 태그들에 속하는 퀴즈들을 수집
        List<Quiz> quizzes = new ArrayList<>();
        for (Tag relatedTag : relatedTags) {
            // 각 태그별로 연관된 퀴즈 목록 조회 (PageRequest로 과도한 조회 방지)
            List<Quiz> tagQuizzes = quizRepository.findByTags(relatedTag, PageRequest.of(0, limit))
                    .getContent() // Page 객체에서 Quiz 목록 추출
                    .stream()
                    // 일반 타입이고 공개된 퀴즈만 필터링
                    .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR && quiz.isPublic())
                    .collect(Collectors.toList());
            quizzes.addAll(tagQuizzes);
        }

        // 4. 수집된 퀴즈 목록에서 중복 제거 후 인기도 순으로 정렬하여 최종 추천 목록 생성
        List<Quiz> sortedQuizzes = quizzes.stream()
                .distinct() // 중복된 퀴즈 제거
                .sorted((q1, q2) -> {
                    // 인기도 점수 계산 및 내림차순 정렬
                    double score1 = calculatePopularityScore(q1);
                    double score2 = calculatePopularityScore(q2);
                    return Double.compare(score2, score1);
                })
                .limit(limit) // 요청된 개수만큼 제한
                .collect(Collectors.toList());

        log.debug("카테고리 기반 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", sortedQuizzes.size());

        // 5. 결과를 DTO로 변환하여 반환
        return entityMapperService.mapToQuizSummaryResponseList(sortedQuizzes);
    }

    /**
     * 특정 난이도에 해당하는 퀴즈 중에서 인기 있는 퀴즈를 추천합니다.
     *
     * @param difficultyStr 추천 기준이 되는 난이도 문자열 (예: "BEGINNER", "INTERMEDIATE", "ADVANCED")
     * @param limit         추천할 퀴즈의 최대 개수
     * @return 난이도 기반 추천 퀴즈 요약 정보 DTO 리스트
     * @throws BusinessException 유효하지 않은 난이도 문자열이 입력된 경우 (INVALID_INPUT_VALUE)
     */
    @Override
    public List<QuizSummaryResponse> getDifficultyBasedRecommendations(String difficultyStr, int limit) {
        log.debug("난이도 기반 퀴즈 추천 시작 - difficulty: {}, limit: {}", difficultyStr, limit);

        // 1. 입력된 난이도 문자열을 DifficultyLevel Enum으로 변환 (대소문자 구분 없이)
        DifficultyLevel difficulty;
        try {
            difficulty = DifficultyLevel.valueOf(difficultyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 문자열일 경우 예외 발생
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 난이도입니다: " + difficultyStr);
        }

        // 2. 해당 난이도의 일반, 공개 퀴즈 조회
        List<Quiz> quizzes = quizRepository.findAll().stream()
                .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR 
                        && quiz.isPublic() 
                        && quiz.getDifficultyLevel() == difficulty)
                .collect(Collectors.toList());
        
        // 랜덤하게 섞기 (데이터베이스에서 ORDER BY random() 대신 Java에서 섞기)
        Collections.shuffle(quizzes);
        
        // 결과 제한
        List<Quiz> difficultyQuizzes = quizzes.stream()
                .limit(limit)
                .collect(Collectors.toList());

        log.debug("난이도 기반 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", difficultyQuizzes.size());

        // 3. 결과를 DTO로 변환하여 반환
        return entityMapperService.mapToQuizSummaryResponseList(difficultyQuizzes);
    }

    /**
     * 오늘의 퀴즈(데일리 퀴즈)와 관련된 태그 또는 유사한 난이도의 퀴즈를 추천합니다.
     * 데일리 퀴즈가 없을 경우 인기 퀴즈를 대신 추천합니다.
     *
     * @param limit 추천할 퀴즈의 최대 개수
     * @return 데일리 퀴즈 관련 추천 퀴즈 요약 정보 DTO 리스트
     */
    @Override
    public List<QuizSummaryResponse> getDailyRelatedRecommendations(int limit) {
        log.debug("데일리 퀴즈 관련 추천 시작 - limit: {}", limit);

        try {
            // 1. DailyQuizService를 통해 현재의 데일리 퀴즈 조회
            Quiz dailyQuiz = dailyQuizService.getCurrentDailyQuiz();

            // 2. 데일리 퀴즈의 태그 및 난이도 정보 추출
            Set<Tag> dailyTags = dailyQuiz.getTags();
            DifficultyLevel dailyDifficulty = dailyQuiz.getDifficultyLevel();

            log.debug("데일리 퀴즈 분석 완료 - 태그 수: {}, 난이도: {}",
                    dailyTags.size(), dailyDifficulty);

            // 3. 데일리 퀴즈와 동일한 태그를 가진 퀴즈들 수집
            List<Quiz> relatedQuizzes = new ArrayList<>();
            for (Tag tag : dailyTags) {
                // 각 태그별 관련 퀴즈 조회
                List<Quiz> tagQuizzes = quizRepository.findByTags(tag, PageRequest.of(0, limit))
                        .getContent()
                        .stream()
                        // 일반 타입, 공개 상태, 그리고 데일리 퀴즈 자신이 아닌 것만 필터링
                        .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR
                                && quiz.isPublic()
                                && !quiz.getId().equals(dailyQuiz.getId()))
                        .collect(Collectors.toList());
                relatedQuizzes.addAll(tagQuizzes);
            }

            // 4. 수집된 퀴즈 목록 중복 제거 후 정렬 (난이도 근접성 > 인기도 순)
            List<Quiz> sortedQuizzes = relatedQuizzes.stream()
                    .distinct() // 중복 제거
                    .sorted((q1, q2) -> {
                        // 데일리 퀴즈와의 난이도 차이 계산 (절대값)
                        int diffDiff1 = Math.abs(q1.getDifficultyLevel().ordinal() - dailyDifficulty.ordinal());
                        int diffDiff2 = Math.abs(q2.getDifficultyLevel().ordinal() - dailyDifficulty.ordinal());

                        // 난이도 차이가 다르면, 차이가 적은 퀴즈 우선 정렬
                        if (diffDiff1 != diffDiff2) {
                            return Integer.compare(diffDiff1, diffDiff2);
                        }

                        // 난이도 차이가 같다면 인기도 점수로 비교 (내림차순)
                        double score1 = calculatePopularityScore(q1);
                        double score2 = calculatePopularityScore(q2);
                        return Double.compare(score2, score1);
                    })
                    .limit(limit) // 요청된 개수만큼 제한
                    .collect(Collectors.toList());

            log.debug("데일리 퀴즈 관련 퀴즈 선택 완료 - 선택된 퀴즈 수: {}", sortedQuizzes.size());

            // 5. 결과를 DTO로 변환하여 반환
            return entityMapperService.mapToQuizSummaryResponseList(sortedQuizzes);

        } catch (BusinessException e) {
            // DailyQuizService에서 데일리 퀴즈를 찾지 못했을 경우 (예: 아직 생성 전)
            log.warn("데일리 퀴즈를 찾을 수 없어 인기 퀴즈로 대체합니다: {}", e.getMessage());
            // 인기 퀴즈 추천으로 대체
            return getPopularQuizzes(limit);
        }
    }

    // ===== 내부 헬퍼 메서드 =====

    /**
     * 사용자의 최근 퀴즈 시도 목록을 분석하여 태그별 평균 점수와 시도 횟수를 계산합니다. (내부 헬퍼 메서드)
     *
     * @param attempts 분석할 사용자의 최근 QuizAttempt 리스트
     * @return 태그(Tag)를 키로, 해당 태그에 대한 사용자 성과(UserPerformance)를 값으로 갖는 Map
     */
    private Map<Tag, UserPerformance> analyzeUserPerformance(List<QuizAttempt> attempts) {
        Map<Tag, List<Integer>> tagScores = new HashMap<>(); // 태그별 점수 목록
        Map<Tag, Integer> tagCounts = new HashMap<>(); // 태그별 시도 횟수

        // 각 시도에서 태그와 점수 정보 추출
        for (QuizAttempt attempt : attempts) {
            // 완료되지 않았거나 점수가 없는 시도는 분석에서 제외할 수 있음 (현재는 포함)
            int score = attempt.getScore();
            // 퀴즈에 포함된 각 태그에 대해 점수 기록 및 횟수 증가
            for (Tag tag : attempt.getQuiz().getTags()) {
                tagScores.computeIfAbsent(tag, k -> new ArrayList<>()).add(score);
                tagCounts.merge(tag, 1, Integer::sum); // 태그 등장 시 횟수 1 증가
            }
        }

        // 태그별 평균 점수와 시도 횟수를 UserPerformance 객체로 변환
        Map<Tag, UserPerformance> tagPerformance = new HashMap<>();
        tagScores.forEach((tag, scores) -> {
            double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            int count = tagCounts.getOrDefault(tag, 0);
            tagPerformance.put(tag, new UserPerformance(avgScore, count));
        });

        return tagPerformance;
    }

    /**
     * 사용자의 최근 퀴즈 시도들의 평균 점수를 기반으로 추천 난이도를 계산합니다. (내부 헬퍼 메서드)
     *
     * @param attempts 분석할 사용자의 최근 QuizAttempt 리스트
     * @return 추천되는 DifficultyLevel (BEGINNER, INTERMEDIATE, ADVANCED)
     */
    private DifficultyLevel calculateRecommendedDifficulty(List<QuizAttempt> attempts) {
        // 시도 목록에서 평균 점수 계산 (시도가 없으면 0점)
        double avgScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);

        // 평균 점수 구간에 따라 난이도 결정
        if (avgScore >= 85) { // 85점 이상: ADVANCED
            return DifficultyLevel.ADVANCED;
        } else if (avgScore >= 65) { // 65점 이상 85점 미만: INTERMEDIATE
            return DifficultyLevel.INTERMEDIATE;
        } else { // 65점 미만: BEGINNER
            return DifficultyLevel.BEGINNER;
        }
    }

    /**
     * 퀴즈의 인기도 점수를 계산합니다. (내부 헬퍼 메서드)
     * 조회수, 시도 횟수, 평균 점수에 각각 가중치를 적용하여 합산합니다.
     * 각 지표는 최대값을 기준으로 정규화됩니다.
     *
     * @param quiz 인기도 점수를 계산할 Quiz 객체
     * @return 계산된 인기도 점수 (0.0 ~ 1.0 사이 값)
     */
    private double calculatePopularityScore(Quiz quiz) {
        // 조회수 기반 점수 (최대 1000회 기준, 가중치 적용)
        double viewScore = Math.min(1.0, (double) quiz.getViewCount() / 1000.0) * VIEW_WEIGHT;

        // 시도 횟수 기반 점수 (최대 100회 기준, 가중치 적용)
        double attemptScore = Math.min(1.0, (double) quiz.getAttemptCount() / 100.0) * ATTEMPT_WEIGHT;

        // 평균 점수 기반 점수 (0-100점 범위, 가중치 적용)
        double scoreValue = (quiz.getAvgScore() / 100.0) * SCORE_WEIGHT;

        // 세 가지 점수를 합산하여 최종 인기도 점수 반환
        return viewScore + attemptScore + scoreValue;
    }

    /**
     * 개인화 추천을 위한 퀴즈 후보 목록을 생성합니다. (내부 헬퍼 메서드)
     * 사용자의 상위 태그, 추천 난이도, 인기도 등을 고려하여 각 퀴즈 후보에 점수를 부여합니다.
     * 사용자가 이미 시도한 퀴즈는 제외됩니다.
     *
     * @param tagPerformance 사용자의 태그별 성과 정보 (Tag -> UserPerformance Map)
     * @param recommendedDifficulty 사용자에게 추천되는 난이도
     * @param attemptedQuizIds 사용자가 이미 시도한 퀴즈 ID Set
     * @return 점수가 부여된 퀴즈 후보(QuizWithScore) 리스트
     */
    private List<QuizWithScore> createPersonalizedCandidates(
            Map<Tag, UserPerformance> tagPerformance,
            DifficultyLevel recommendedDifficulty,
            Set<Long> attemptedQuizIds) {

        // 사용자가 좋은 성과를 보인 상위 5개 태그 선택 (평균 점수 기준)
        List<Map.Entry<Tag, UserPerformance>> topTags = tagPerformance.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getAvgScore(), e1.getValue().getAvgScore()))
                .limit(5)
                .collect(Collectors.toList());

        List<QuizWithScore> candidates = new ArrayList<>();

        // 선택된 각 상위 태그에 대해
        for (Map.Entry<Tag, UserPerformance> tagEntry : topTags) {
            Tag tag = tagEntry.getKey();
            // 태그 가중치 (사용자의 해당 태그 평균 점수 / 100)
            double tagWeight = tagEntry.getValue().getAvgScore() / 100.0;

            // 해당 태그를 포함하는 퀴즈 목록 조회 (최대 20개)
            List<Quiz> tagQuizzes = quizRepository.findByTags(tag, PageRequest.of(0, 20))
                    .getContent()
                    .stream()
                    // 일반 타입, 공개 상태, 아직 시도하지 않은 퀴즈만 필터링
                    .filter(quiz -> quiz.getQuizType() == QuizType.REGULAR
                            && quiz.isPublic()
                            && !attemptedQuizIds.contains(quiz.getId()))
                    .collect(Collectors.toList());

            // 각 퀴즈에 대해 개인화 점수 계산
            for (Quiz quiz : tagQuizzes) {
                // 난이도 일치도 계산 (추천 난이도와 얼마나 가까운지, 0.0 ~ 1.0)
                // 난이도 차이: 0 (일치), 1 (한 단계 차이), 2 (두 단계 차이)
                double difficultyDiff = Math.abs(quiz.getDifficultyLevel().ordinal() - recommendedDifficulty.ordinal());
                // 일치도: 1.0 (일치), 0.5 (한 단계 차이), 0.0 (두 단계 차이)
                double difficultyMatch = 1.0 - (difficultyDiff / 2.0);

                // 최종 개인화 점수 계산 (태그 관련성 50%, 난이도 일치도 30%, 인기도 20%)
                double score = (tagWeight * 0.5) + (difficultyMatch * 0.3)
                        + (calculatePopularityScore(quiz) * 0.2);

                candidates.add(new QuizWithScore(quiz, score));
            }
        }

        return candidates;
    }

    /**
     * 점수가 부여된 퀴즈 후보 목록에서 최종 추천 퀴즈를 선택합니다. (내부 헬퍼 메서드)
     * 중복된 퀴즈는 점수가 높은 것만 남기고, 최종적으로 점수 순으로 정렬하여 상위 N개를 선택합니다.
     *
     * @param candidates 점수가 부여된 퀴즈 후보(QuizWithScore) 리스트
     * @param limit      최종 추천할 퀴즈의 최대 개수
     * @return 최종 선택된 추천 Quiz 엔티티 리스트
     */
    private List<Quiz> selectFinalRecommendations(List<QuizWithScore> candidates, int limit) {
        // 1. 중복 퀴즈 제거 (Map을 이용하여 퀴즈 ID 기준, 점수가 높은 후보 유지)
        Map<Long, QuizWithScore> uniqueCandidates = new HashMap<>();
        for (QuizWithScore candidate : candidates) {
            Long quizId = candidate.getQuiz().getId();
            // Map에 없거나, 기존 후보보다 점수가 높으면 Map에 추가/갱신
            if (!uniqueCandidates.containsKey(quizId)
                    || uniqueCandidates.get(quizId).getScore() < candidate.getScore()) {
                uniqueCandidates.put(quizId, candidate);
            }
        }

        // 2. Map의 값들(유니크하고 점수 높은 후보)을 리스트로 변환 후 점수 내림차순 정렬
        List<QuizWithScore> sortedCandidates = new ArrayList<>(uniqueCandidates.values());
        sortedCandidates.sort((c1, c2) -> Double.compare(c2.getScore(), c1.getScore()));

        // 3. 정렬된 후보 중 상위 limit 개수만큼 선택하여 Quiz 객체만 추출
        return sortedCandidates.stream()
                .limit(limit)
                .map(QuizWithScore::getQuiz)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 태그에 대한 성과(평균 점수, 시도 횟수)를 저장하는 내부 클래스입니다.
     */
    private static class UserPerformance {
        private final double avgScore;
        private final int attemptCount;

        /**
         * UserPerformance 생성자
         * @param avgScore 해당 태그 퀴즈들의 평균 점수
         * @param attemptCount 해당 태그 퀴즈들의 시도 횟수
         */
        public UserPerformance(double avgScore, int attemptCount) {
            this.avgScore = avgScore;
            this.attemptCount = attemptCount;
        }

        /**
         * 평균 점수를 반환합니다.
         * @return 평균 점수
         */
        public double getAvgScore() {
            return avgScore;
        }

        /**
         * 시도 횟수를 반환합니다.
         * @return 시도 횟수
         */
        public int getAttemptCount() {
            return attemptCount;
        }
    }

    /**
     * 추천 후보 퀴즈와 해당 퀴즈의 추천 점수를 함께 저장하는 내부 클래스입니다.
     */
    private static class QuizWithScore {
        private final Quiz quiz;
        private final double score;

        /**
         * QuizWithScore 생성자
         * @param quiz 추천 후보 퀴즈
         * @param score 계산된 추천 점수
         */
        public QuizWithScore(Quiz quiz, double score) {
            this.quiz = quiz;
            this.score = score;
        }

        /**
         * 추천 후보 퀴즈를 반환합니다.
         * @return Quiz 객체
         */
        public Quiz getQuiz() {
            return quiz;
        }

        /**
         * 계산된 추천 점수를 반환합니다.
         * @return 추천 점수
         */
        public double getScore() {
            return score;
        }
    }
} 