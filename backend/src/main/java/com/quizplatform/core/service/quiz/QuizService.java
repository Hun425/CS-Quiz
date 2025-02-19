package com.quizplatform.core.service.quiz;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.quiz.QuizStatistics;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.question.QuestionCreateRequest;
import com.quizplatform.core.dto.quiz.QuizCreateRequest;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// 퀴즈 서비스 구현
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    /**
     * 새로운 퀴즈를 생성합니다.
     * 이 메서드는 퀴즈의 기본 정보와 함께 문제들도 함께 생성합니다.
     */
    @Transactional
    public Quiz createQuiz(UUID creatorId, QuizCreateRequest request) {
        // 퀴즈 생성자 조회
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
        return quizRepository.save(quiz);
    }

    /**
     * 퀴즈를 수정합니다.
     * 문제와 태그의 수정도 함께 처리합니다.
     */
    @Transactional
    public Quiz updateQuiz(UUID quizId, QuizCreateRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

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

        return quiz;
    }

    /**
     * 데일리 퀴즈를 생성합니다.
     * 매일 자동으로 생성되며, 난이도와 주제를 고려하여 선택됩니다.
     */
    @Transactional
    public Quiz createDailyQuiz() {
        // 이전 데일리 퀴즈들의 태그와 난이도를 분석
        List<Quiz> recentDailyQuizzes = quizRepository
                .findRecentDailyQuizzes(LocalDateTime.now().minusDays(7));

        Set<UUID> recentTagIds = new HashSet<>();
        Set<DifficultyLevel> recentDifficulties = new HashSet<>();

        recentDailyQuizzes.forEach(quiz -> {
            quiz.getTags().forEach(tag -> recentTagIds.add(tag.getId()));
            recentDifficulties.add(quiz.getDifficultyLevel());
        });

        // 적절한 퀴즈 선택
        Quiz selectedQuiz = quizRepository.findQuizForDaily(recentTagIds, recentDifficulties)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_READY_TO_START));

        // 데일리 퀴즈로 설정
        Quiz dailyQuiz = selectedQuiz.createDailyCopy();
        dailyQuiz.setValidUntil(LocalDateTime.now().plusDays(1));

        return quizRepository.save(dailyQuiz);
    }

    /**
     * 주어진 조건에 맞는 퀴즈 목록을 검색합니다.
     */
    public Page<Quiz> searchQuizzes(QuizSearchCondition condition, Pageable pageable) {
        return quizRepository.search(condition, pageable);
    }

    /**
     * 특정 난이도와 태그에 맞는 퀴즈를 추천합니다.
     */
    public List<Quiz> getRecommendedQuizzes(User user, int limit) {
        // 사용자의 최근 퀴즈 시도 분석
        List<QuizAttempt> recentAttempts = quizAttemptRepository
                .findRecentAttempts(user, LocalDateTime.now().minusDays(30));

        // 성과가 좋은 태그와 적절한 난이도 찾기
        Map<Tag, Double> tagPerformance = analyzeTagPerformance(recentAttempts);
        DifficultyLevel recommendedDifficulty = calculateRecommendedDifficulty(recentAttempts);

        // 추천 퀴즈 찾기
        return quizRepository.findRecommendedQuizzes(
                tagPerformance.keySet(),
                recommendedDifficulty,
                limit
        );
    }

    // 내부 헬퍼 메서드들
    private Set<Tag> validateAndGetTags(List<UUID> tagIds) {
        return tagIds.stream()
                .map(tagId -> tagRepository.findById(tagId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND)))
                .collect(Collectors.toSet());
    }

    private void createAndAddQuestion(Quiz quiz, QuestionCreateRequest request) {
        Question question = Question.builder()
                .questionType(request.getQuestionType())
                .questionText(request.getQuestionText())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .build();

        if (request.getCodeSnippet() != null) {
            question.setCodeSnippet(request.getCodeSnippet());
        }

        if (request.getDiagramData() != null) {
            question.setDiagramData(request.getDiagramData());
        }

        if (request.getOptions() != null) {
            question.setOptions(request.getOptions());
        }

        quiz.addQuestion(question);
    }

    /**
     * 문제 내용을 제외한 퀴즈 정보를 조회합니다.
     */
    public Quiz getQuizWithoutQuestions(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 문제 내용은 제외하고 기본 정보만 반환
        quiz.getQuestions().size(); // 지연 로딩 초기화
        return quiz;
    }

    /**
     * 현재 유효한 데일리 퀴즈를 조회합니다.
     */
    public Quiz getCurrentDailyQuiz() {
        return quizRepository.findCurrentDailyQuiz(LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 특정 태그에 속한 퀴즈 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<Quiz> getQuizzesByTag(UUID tagId, Pageable pageable) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        return quizRepository.findByTags(tag, pageable);
    }

    /**
     * 퀴즈의 상세 통계 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public QuizStatistics getQuizStatistics(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 퀴즈 시도 기록 조회
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz(quiz);

        // 문제별 통계 계산
        Map<UUID, QuestionStatistics> questionStats = calculateQuestionStatistics(quiz, attempts);

        return QuizStatistics.builder()
                .totalAttempts(attempts.size())
                .averageScore(calculateAverageScore(attempts))
                .completionRate(calculateCompletionRate(attempts))
                .averageTimeSeconds(calculateAverageTime(attempts))
                .difficultyDistribution(calculateDifficultyDistribution(quiz))
                .questionStatistics(createQuestionStatisticsList(questionStats))
                .build();
    }

    private Map<UUID, QuestionStatistics> calculateQuestionStatistics(Quiz quiz, List<QuizAttempt> attempts) {
        Map<UUID, QuestionStatistics> stats = new HashMap<>();

        // 각 문제별 통계 초기화
        quiz.getQuestions().forEach(question -> {
            stats.put(question.getId(), new QuestionStatistics(question.getId()));
        });

        // 시도 데이터로 통계 업데이트
        attempts.forEach(attempt -> {
            attempt.getQuestionAttempts().forEach(questionAttempt -> {
                QuestionStatistics questionStat = stats.get(questionAttempt.getQuestion().getId());
                questionStat.updateStatistics(questionAttempt);
            });
        });

        return stats;
    }

    @Getter
    private static class QuestionStatistics {
        private final UUID questionId;
        private int correctAnswers = 0;
        private int totalAttempts = 0;
        private long totalTimeSeconds = 0;

        public QuestionStatistics(UUID questionId) {
            this.questionId = questionId;
        }

        public void updateStatistics(QuestionAttempt attempt) {
            totalAttempts++;
            if (attempt.isCorrect()) {
                correctAnswers++;
            }
            totalTimeSeconds += attempt.getTimeTaken();
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
            Map<UUID, QuestionStatistics> questionStats) {
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
        // 태그별 성과 분석 로직 구현
        Map<Tag, Double> tagPerformance = new HashMap<>();
        // ... 구현 ...
        return tagPerformance;
    }

    private DifficultyLevel calculateRecommendedDifficulty(List<QuizAttempt> attempts) {
        // 적절한 난이도 계산 로직 구현
        // ... 구현 ...
        return DifficultyLevel.INTERMEDIATE; // 임시 반환
    }
}