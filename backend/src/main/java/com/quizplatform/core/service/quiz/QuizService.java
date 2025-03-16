package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.*;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.question.QuestionCreateRequest;
import com.quizplatform.core.dto.quiz.*;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.common.EntityMapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptService quizAttemptService;
    private final EntityMapperService entityMapperService;

    /**
     * 새로운 퀴즈를 생성합니다.
     */
    @Transactional
    public QuizResponse createQuiz(Long creatorId, QuizCreateRequest request) {
        // 퀴즈 생성자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + creatorId));

        // 태그 조회 및 검증
        Set<Tag> tags = validateAndGetTags(request.getTagIds());

        // 퀴즈 객체 생성
        Quiz quiz = Quiz.builder()
                .creator(creator)
                .title(request.getTitle())
                .description(request.getDescription())
                .quizType(request.getQuizType())
                .difficultyLevel(request.getDifficultyLevel())
                .timeLimit(request.getTimeLimit())
                .build();

        // 태그 추가
        tags.forEach(quiz::addTag);

        // 문제 추가
        request.getQuestions().forEach(questionRequest ->
                createAndAddQuestion(quiz, questionRequest));

        // 퀴즈 저장
        Quiz savedQuiz = quizRepository.save(quiz);

        // DTO로 변환하여 반환
        return entityMapperService.mapToQuizResponse(savedQuiz);
    }

    /**
     * 퀴즈를 수정합니다.
     */
    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizCreateRequest request) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "Quiz not found with id: " + quizId));

        // 기본 정보 업데이트
        quiz.update(
                request.getTitle(),
                request.getDescription(),
                request.getDifficultyLevel(),
                request.getTimeLimit()
        );

        // 태그 업데이트
        Set<Tag> newTags = validateAndGetTags(request.getTagIds());
        quiz.updateTags(newTags);

        // 문제 업데이트
        quiz.getQuestions().clear();
        request.getQuestions().forEach(questionRequest ->
                createAndAddQuestion(quiz, questionRequest));

        Quiz updatedQuiz = quizRepository.save(quiz);

        // DTO로 변환하여 반환
        return entityMapperService.mapToQuizResponse(updatedQuiz);
    }

    /**
     * 데일리 퀴즈를 생성합니다.
     */
    @Transactional
    public QuizResponse createDailyQuiz() {
        // 이전 데일리 퀴즈들의 태그와 난이도를 분석
        List<Quiz> recentDailyQuizzes = quizRepository
                .findRecentDailyQuizzes(LocalDateTime.now().minusDays(7));

        Set<Long> recentTagIds = new HashSet<>();
        Set<DifficultyLevel> recentDifficulties = new HashSet<>();

        recentDailyQuizzes.forEach(quiz -> {
            quiz.getTags().forEach(tag -> recentTagIds.add(tag.getId()));
            recentDifficulties.add(quiz.getDifficultyLevel());
        });

        // 적절한 퀴즈 선택
        Quiz selectedQuiz = quizRepository.findQuizForDaily(recentTagIds, recentDifficulties)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_READY_TO_START, "No quiz available for daily creation based on recent data"));

        // 데일리 퀴즈로 설정
        Quiz dailyQuiz = selectedQuiz.createDailyCopy();
        dailyQuiz.setValidUntil(LocalDateTime.now().plusDays(1));

        Quiz savedDailyQuiz = quizRepository.save(dailyQuiz);

        // DTO로 변환하여 반환
        return entityMapperService.mapToQuizResponse(savedDailyQuiz);
    }

    /**
     * 주어진 조건에 맞는 퀴즈 목록을 검색합니다.
     */
    public Page<QuizSummaryResponse> searchQuizzesDto(QuizSearchCondition condition, Pageable pageable) {
        // 조건 유효성 검사 추가
        condition.validate();

        Page<Quiz> quizzes = quizRepository.search(condition, pageable);
        // 트랜잭션 내에서 DTO로 변환
        return quizzes.map(entityMapperService::mapToQuizSummaryResponse);
    }

    /**
     * 문제 내용을 제외한 퀴즈 정보를 조회합니다.
     */
    public QuizDetailResponse getQuizWithoutQuestions(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        return entityMapperService.mapToQuizDetailResponse(quiz);
    }

    /**
     * 문제를 포함한 퀴즈 정보를 조회합니다.
     */
    public QuizResponse getQuizWithQuestions(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        return entityMapperService.mapToQuizResponse(quiz);
    }

    /**
     * 현재 유효한 데일리 퀴즈를 조회합니다.
     */
    public QuizResponse getCurrentDailyQuiz() {
        Quiz quiz = quizRepository.findCurrentDailyQuizWithTags(LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Current daily quiz not found at " + LocalDateTime.now()));

        return entityMapperService.mapToQuizResponse(quiz);
    }

    /**
     * 특정 태그에 속한 퀴즈 목록을 조회합니다.
     */
    public Page<QuizSummaryResponse> getQuizzesByTag(Long tagId, Pageable pageable) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Tag not found with id: " + tagId));

        Page<Quiz> quizzes = quizRepository.findByTags(tag, pageable);
        return quizzes.map(entityMapperService::mapToQuizSummaryResponse);
    }

    /**
     * 퀴즈의 상세 통계 정보를 조회합니다.
     */
    public QuizStatisticsResponse getQuizStatistics(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "Quiz not found with id: " + quizId));

        // 퀴즈 시도 기록 조회
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz(quiz);

        // 각 질문 시도 데이터 로드
        attempts.forEach(attempt -> {
            attempt.getQuestionAttempts().size();
        });

        // 문제별 통계 계산
        Map<Long, QuestionStatistics> questionStats = calculateQuestionStatistics(quiz, attempts);

        QuizStatistics statistics = QuizStatistics.builder()
                .totalAttempts(attempts.size())
                .averageScore(calculateAverageScore(attempts))
                .completionRate(calculateCompletionRate(attempts))
                .averageTimeSeconds(calculateAverageTime(attempts))
                .difficultyDistribution(calculateDifficultyDistribution(quiz))
                .questionStatistics(createQuestionStatisticsList(questionStats))
                .build();

        return QuizStatisticsResponse.from(statistics);
    }

    /**
     * 특정 난이도와 태그에 맞는 퀴즈를 추천합니다.
     */
    public List<QuizSummaryResponse> getRecommendedQuizzes(User user, int limit) {
        // 사용자의 최근 퀴즈 시도 분석
        List<QuizAttempt> recentAttempts = quizAttemptRepository
                .findRecentAttempts(user, LocalDateTime.now().minusDays(30));

        // 성과가 좋은 태그와 적절한 난이도 찾기
        Map<Tag, Double> tagPerformance = analyzeTagPerformance(recentAttempts);
        DifficultyLevel recommendedDifficulty = calculateRecommendedDifficulty(recentAttempts);

        // 추천 퀴즈 찾기
        List<Quiz> recommendedQuizzes = quizRepository.findRecommendedQuizzes(
                tagPerformance.keySet(),
                recommendedDifficulty,
                limit
        );

        return entityMapperService.mapToQuizSummaryResponseList(recommendedQuizzes);
    }

    /**
     * 플레이 가능한 퀴즈 정보를 조회합니다.
     */
    @Transactional
    public QuizResponse getPlayableQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND,
                        "퀴즈를 찾을 수 없습니다. ID: " + quizId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "사용자를 찾을 수 없습니다. ID: " + userId));

        // 퀴즈가 공개된 것인지 확인
        if (!quiz.isPublic() && !quiz.getCreator().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "접근할 수 없는 퀴즈입니다.");
        }

        // 데일리 퀴즈인 경우 이미 완료했는지 체크
        if (quiz.getQuizType() == QuizType.DAILY &&
                quizAttemptRepository.hasCompletedQuiz(user, quiz)) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED, "이미 완료한 데일리 퀴즈입니다.");
        }

        // 퀴즈 시도 객체 생성 (시작 시간 기록)
        QuizAttempt quizAttempt = quizAttemptService.startQuiz(quizId, user);

        // 퀴즈 조회수 증가
        quiz.incrementViewCount();
        quizRepository.save(quiz);

        // 기본 퀴즈 응답 생성
        QuizResponse quizResponse = entityMapperService.mapToQuizResponse(quiz);

        // 퀴즈 시도 ID 추가
        return quizResponse.withQuizAttemptId(quizAttempt.getId());
    }

    // 내부 헬퍼 메서드들
    private Set<Tag> validateAndGetTags(List<Long> tagIds) {
        return tagIds.stream()
                .map(tagId -> tagRepository.findById(tagId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "Tag not found with id: " + tagId)))
                .collect(Collectors.toSet());
    }

    private void createAndAddQuestion(Quiz quiz, QuestionCreateRequest request) {
        Question question = Question.builder()
                .questionType(request.getQuestionType())
                .questionText(request.getQuestionText())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .points(request.getPoints())
                .build();

        if (request.getCodeSnippet() != null) {
            question.setCodeSnippet(request.getCodeSnippet());
        }

        if (request.getOptions() != null) {
            question.setOptions(request.getOptions());
        }

        quiz.addQuestion(question);
    }

    private Map<Long, QuestionStatistics> calculateQuestionStatistics(Quiz quiz, List<QuizAttempt> attempts) {
        Map<Long, QuestionStatistics> stats = new HashMap<>();

        // 각 문제별 통계 초기화
        quiz.getQuestions().forEach(question -> {
            stats.put(question.getId(), new QuestionStatistics(question.getId()));
        });

        // 시도 데이터로 통계 업데이트
        attempts.forEach(attempt -> {
            attempt.getQuestionAttempts().forEach(questionAttempt -> {
                QuestionStatistics questionStat = stats.get(questionAttempt.getQuestion().getId());
                if (questionStat != null) {
                    questionStat.updateStatistics(questionAttempt);
                }
            });
        });

        return stats;
    }

    private static class QuestionStatistics {
        private final Long questionId;
        private int correctAnswers = 0;
        private int totalAttempts = 0;
        private long totalTimeSeconds = 0;

        public QuestionStatistics(Long questionId) {
            this.questionId = questionId;
        }

        public void updateStatistics(com.quizplatform.core.domain.question.QuestionAttempt attempt) {
            totalAttempts++;
            if (attempt.isCorrect()) {
                correctAnswers++;
            }
            if (attempt.getTimeTaken() != null) {
                totalTimeSeconds += attempt.getTimeTaken();
            }
        }

        public Long getQuestionId() {
            return questionId;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAttempts() {
            return totalAttempts;
        }

        public double getCorrectRate() {
            return totalAttempts == 0 ? 0 : (double) correctAnswers / totalAttempts * 100;
        }

        public int getAverageTimeSeconds() {
            return totalAttempts == 0 ? 0 : (int) (totalTimeSeconds / totalAttempts);
        }
    }

    private double calculateAverageScore(List<QuizAttempt> attempts) {
        return attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);
    }

    private double calculateCompletionRate(List<QuizAttempt> attempts) {
        long completedAttempts = attempts.stream()
                .filter(QuizAttempt::isCompleted)
                .count();
        return attempts.isEmpty() ? 0 : (double) completedAttempts / attempts.size() * 100;
    }

    private int calculateAverageTime(List<QuizAttempt> attempts) {
        return (int) attempts.stream()
                .filter(attempt -> attempt.getTimeTaken() != null)
                .mapToInt(QuizAttempt::getTimeTaken)
                .average()
                .orElse(0.0);
    }

    private Map<DifficultyLevel, Integer> calculateDifficultyDistribution(Quiz quiz) {
        return quiz.getQuestions().stream()
                .collect(Collectors.groupingBy(
                        Question::getDifficultyLevel,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private List<QuizStatistics.QuestionStatistic> createQuestionStatisticsList(
            Map<Long, QuestionStatistics> questionStats) {
        return questionStats.values().stream()
                .map(stats -> QuizStatistics.QuestionStatistic.builder()
                        .questionId(stats.getQuestionId())
                        .correctAnswers(stats.getCorrectAnswers())
                        .totalAttempts(stats.getTotalAttempts())
                        .correctRate(stats.getCorrectRate())
                        .averageTimeSeconds(stats.getAverageTimeSeconds())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Tag, Double> analyzeTagPerformance(List<QuizAttempt> attempts) {
        // 태그별 성과 분석 로직
        Map<Tag, Double> tagPerformance = new HashMap<>();
        // 각 시도에서 퀴즈의 태그와 점수를 추출하여 태그별 평균 성과 계산
        Map<Tag, List<Integer>> tagScores = new HashMap<>();

        for (QuizAttempt attempt : attempts) {
            int score = attempt.getScore();
            attempt.getQuiz().getTags().forEach(tag -> {
                tagScores.computeIfAbsent(tag, k -> new ArrayList<>()).add(score);
            });
        }

        // 태그별 평균 점수 계산
        tagScores.forEach((tag, scores) -> {
            double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            tagPerformance.put(tag, avgScore);
        });

        return tagPerformance;
    }

    private DifficultyLevel calculateRecommendedDifficulty(List<QuizAttempt> attempts) {
        // 사용자의 평균 점수를 기반으로 적절한 난이도 계산
        double avgScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);

        if (avgScore >= 85) {
            return DifficultyLevel.ADVANCED;
        } else if (avgScore >= 65) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            return DifficultyLevel.BEGINNER;
        }
    }
}