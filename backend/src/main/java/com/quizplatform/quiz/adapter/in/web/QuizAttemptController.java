package com.quizplatform.quiz.adapter.in.web;

import com.quizplatform.quiz.adapter.in.web.dto.*;
import com.quizplatform.quiz.application.port.in.*;
import com.quizplatform.quiz.application.port.in.command.*;
import com.quizplatform.quiz.domain.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 퀴즈 시도 컨트롤러 클래스
 * 퀴즈 시도, 답변 제출, 결과 조회 등의 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final StartQuizAttemptUseCase startQuizAttemptUseCase;
    private final SubmitQuizAnswerUseCase submitQuizAnswerUseCase;
    private final FinishQuizAttemptUseCase finishQuizAttemptUseCase;
    private final GetQuizAttemptUseCase getQuizAttemptUseCase;
    private final GetQuizStatisticsUseCase getQuizStatisticsUseCase;

    /**
     * 퀴즈 시도 시작 API
     */
    @PostMapping("/start")
    public ResponseEntity<QuizAttemptResponse> startQuizAttempt(@Valid @RequestBody StartQuizAttemptRequest request) {
        log.info("Starting quiz attempt for quiz ID: {} by user ID: {}", request.getQuizId(), request.getUserId());
        
        StartQuizAttemptCommand command = StartQuizAttemptCommand.builder()
                .quizId(request.getQuizId())
                .userId(request.getUserId())
                .startTime(LocalDateTime.now())
                .build();
        
        QuizAttempt quizAttempt = startQuizAttemptUseCase.startQuizAttempt(command);
        QuizAttemptResponse response = mapToQuizAttemptResponse(quizAttempt);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 답변 제출 API
     */
    @PostMapping("/submit-answer")
    public ResponseEntity<QuestionAttemptResponse> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        log.info("Submitting answer for question ID: {} in quiz attempt ID: {}", 
                request.getQuestionId(), request.getQuizAttemptId());
        
        SubmitAnswerCommand command = SubmitAnswerCommand.builder()
                .quizAttemptId(request.getQuizAttemptId())
                .questionId(request.getQuestionId())
                .userAnswer(request.getUserAnswer())
                .answerTime(LocalDateTime.now())
                .build();
        
        QuestionAttempt questionAttempt = submitQuizAnswerUseCase.submitAnswer(command);
        QuestionAttemptResponse response = mapToQuestionAttemptResponse(questionAttempt);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 시도 완료 API
     */
    @PostMapping("/finish")
    public ResponseEntity<QuizAttemptResponse> finishQuizAttempt(@Valid @RequestBody FinishQuizAttemptRequest request) {
        log.info("Finishing quiz attempt ID: {}", request.getQuizAttemptId());
        
        FinishQuizAttemptCommand command = FinishQuizAttemptCommand.builder()
                .quizAttemptId(request.getQuizAttemptId())
                .endTime(LocalDateTime.now())
                .build();
        
        QuizAttempt quizAttempt = finishQuizAttemptUseCase.finishQuizAttempt(command);
        QuizAttemptResponse response = mapToQuizAttemptResponse(quizAttempt);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 시도 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizAttemptResponse> getQuizAttempt(@PathVariable Long id) {
        log.info("Fetching quiz attempt with ID: {}", id);
        
        QuizAttempt quizAttempt = getQuizAttemptUseCase.getQuizAttemptById(id);
        QuizAttemptResponse response = mapToQuizAttemptResponse(quizAttempt);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 퀴즈 시도 목록 조회 API
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<QuizAttemptResponse>> getUserQuizAttempts(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        log.info("Fetching quiz attempts for user ID: {}", userId);
        
        Page<QuizAttempt> quizAttempts = getQuizAttemptUseCase.getQuizAttemptsByUserId(userId, pageable);
        Page<QuizAttemptResponse> response = quizAttempts.map(this::mapToQuizAttemptResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 퀴즈의 사용자 시도 목록 조회 API
     */
    @GetMapping("/users/{userId}/quizzes/{quizId}")
    public ResponseEntity<Page<QuizAttemptResponse>> getUserQuizAttemptsForQuiz(
            @PathVariable Long userId,
            @PathVariable Long quizId,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        log.info("Fetching quiz attempts for user ID: {} and quiz ID: {}", userId, quizId);
        
        Page<QuizAttempt> quizAttempts = getQuizAttemptUseCase.getQuizAttemptsByUserIdAndQuizId(userId, quizId, pageable);
        Page<QuizAttemptResponse> response = quizAttempts.map(this::mapToQuizAttemptResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 최근 퀴즈 시도 목록 조회 API
     */
    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<List<QuizAttemptResponse>> getRecentQuizAttempts(@PathVariable Long userId) {
        log.info("Fetching recent quiz attempts for user ID: {}", userId);
        
        List<QuizAttempt> quizAttempts = getQuizAttemptUseCase.getRecentQuizAttemptsByUserId(userId, 10);
        List<QuizAttemptResponse> response = quizAttempts.stream()
                .map(this::mapToQuizAttemptResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 특정 퀴즈의 최고 점수 시도 조회 API
     */
    @GetMapping("/users/{userId}/quizzes/{quizId}/best")
    public ResponseEntity<QuizAttemptResponse> getBestQuizAttempt(
            @PathVariable Long userId,
            @PathVariable Long quizId) {
        log.info("Fetching best quiz attempt for user ID: {} and quiz ID: {}", userId, quizId);
        
        QuizAttempt quizAttempt = getQuizAttemptUseCase.getBestQuizAttempt(userId, quizId);
        QuizAttemptResponse response = mapToQuizAttemptResponse(quizAttempt);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 통계 조회 API
     */
    @GetMapping("/quizzes/{quizId}/statistics")
    public ResponseEntity<QuizStatisticsResponse> getQuizStatistics(@PathVariable Long quizId) {
        log.info("Fetching statistics for quiz ID: {}", quizId);
        
        Quiz quiz = getQuizStatisticsUseCase.getQuizStatistics(quizId);
        
        // 퀴즈 통계 데이터를 담은 응답 DTO 생성
        QuizStatisticsResponse response = mapToQuizStatisticsResponse(quiz);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 도메인 객체를 퀴즈 시도 응답 DTO로 변환
     */
    private QuizAttemptResponse mapToQuizAttemptResponse(QuizAttempt quizAttempt) {
        // 도메인 모델 매핑을 위한 컨트롤러 레벨의 매퍼 메서드
        // 실제 구현에서는 별도의 매퍼 클래스 사용 권장
        
        List<QuestionAttemptResponse> questionAttemptResponses = quizAttempt.getQuestionAttempts().stream()
                .map(this::mapToQuestionAttemptResponse)
                .collect(Collectors.toList());
        
        // 추가 통계 계산
        int totalQuestions = quizAttempt.getQuiz().getQuestions().size();
        int correctAnswers = (int) quizAttempt.getQuestionAttempts().stream()
                .filter(QuestionAttempt::isCorrect)
                .count();
        int maxPossibleScore = quizAttempt.getQuiz().getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();
        double percentageScore = maxPossibleScore > 0 
                ? (double) quizAttempt.getScore() / maxPossibleScore * 100 
                : 0;
        long timeSpentSeconds = quizAttempt.getEndTime() != null 
                ? Duration.between(quizAttempt.getStartTime(), quizAttempt.getEndTime()).getSeconds()
                : 0;
        
        // QuizResponse 생성 (퀴즈 컨트롤러의 매퍼 메서드 재사용 필요)
        QuizResponse quizResponse = mapToQuizResponseWithoutQuestions(quizAttempt.getQuiz());
        
        return QuizAttemptResponse.builder()
                .id(quizAttempt.getId())
                .userId(quizAttempt.getUserId())
                .quiz(quizResponse)
                .startTime(quizAttempt.getStartTime())
                .endTime(quizAttempt.getEndTime())
                .score(quizAttempt.getScore())
                .passed(quizAttempt.getPassed())
                .status(quizAttempt.getStatus())
                .questionAttempts(questionAttemptResponses)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .maxPossibleScore(maxPossibleScore)
                .percentageScore(percentageScore)
                .timeSpentSeconds(timeSpentSeconds)
                .build();
    }

    /**
     * 도메인 객체를 퀴즈 응답 DTO로 변환 (문제 정보 없이)
     */
    private QuizResponse mapToQuizResponseWithoutQuestions(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .difficultyLevel(quiz.getDifficultyLevel())
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .passingScore(quiz.getPassingScore())
                .isPublic(quiz.isPublic())
                .createdBy(quiz.getCreatedBy())
                .createdAt(quiz.getCreatedAt())
                .quizType(quiz.getQuizType())
                .questionCount(quiz.getQuestions().size())
                .totalPoints(quiz.getQuestions().stream().mapToInt(Question::getPoints).sum())
                .build();
    }

    /**
     * 도메인 객체를 문제 시도 응답 DTO로 변환
     */
    private QuestionAttemptResponse mapToQuestionAttemptResponse(QuestionAttempt questionAttempt) {
        return QuestionAttemptResponse.builder()
                .id(questionAttempt.getId())
                .question(mapToQuestionResponse(questionAttempt.getQuestion()))
                .userAnswer(questionAttempt.getUserAnswer())
                .isCorrect(questionAttempt.isCorrect())
                .pointsEarned(questionAttempt.getPointsEarned())
                .answerTime(questionAttempt.getAnswerTime())
                .build();
    }

    /**
     * 도메인 객체를 문제 응답 DTO로 변환
     */
    private QuestionResponse mapToQuestionResponse(Question question) {
        // 세부 구현은 QuizController와 동일하게 처리
        List<QuestionResponse.AnswerOptionResponse> optionResponses = question.getAnswerOptions().stream()
                .map(option -> QuestionResponse.AnswerOptionResponse.builder()
                        .id(option.getId())
                        .optionText(option.getOptionText())
                        .displayOrder(option.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
        
        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .points(question.getPoints())
                .explanation(question.getExplanation())
                .displayOrder(question.getDisplayOrder())
                .answerOptions(optionResponses)
                // 시도 조회 시에는 정답 정보 포함
                .correctAnswerIds(question.getCorrectAnswerIds())
                .build();
    }

    /**
     * 도메인 객체를 퀴즈 통계 응답 DTO로 변환
     */
    private QuizStatisticsResponse mapToQuizStatisticsResponse(Quiz quiz) {
        List<QuizStatisticsResponse.QuestionStatistics> questionStatsList = quiz.getQuestions().stream()
                .map(question -> QuizStatisticsResponse.QuestionStatistics.builder()
                        .questionId(question.getId())
                        .questionText(question.getQuestionText())
                        .correctRate(0.0) // 실제 정답률은 별도 계산 필요
                        .points(question.getPoints())
                        .build())
                .collect(Collectors.toList());
        
        return QuizStatisticsResponse.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalAttempts(quiz.getTotalAttempts())
                .averageScore(quiz.getAverageScore())
                .passRate(quiz.getPassRate())
                .passingScore(quiz.getPassingScore())
                .maxPossibleScore(calculateTotalPoints(quiz.getQuestions()))
                .questionStatistics(questionStatsList)
                .build();
    }

    /**
     * 문제 목록으로부터 총 배점 계산
     */
    private Integer calculateTotalPoints(List<Question> questions) {
        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }
}
