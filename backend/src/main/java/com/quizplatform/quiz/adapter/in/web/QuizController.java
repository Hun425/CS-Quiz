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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 퀴즈 컨트롤러 클래스
 * 퀴즈 생성, 조회, 수정, 삭제 등의 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final CreateQuizUseCase createQuizUseCase;
    private final UpdateQuizUseCase updateQuizUseCase;
    private final GetQuizUseCase getQuizUseCase;
    private final SearchQuizUseCase searchQuizUseCase;

    /**
     * 퀴즈 생성 API
     */
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        log.info("Creating quiz: {}", request.getTitle());
        
        // 요청 DTO를 명령 객체로 변환
        CreateQuizCommand command = mapToCreateQuizCommand(request);
        
        // 유스케이스 실행
        Quiz quiz = createQuizUseCase.createQuiz(command);
        
        // 응답 DTO로 변환
        QuizResponse response = mapToQuizResponse(quiz);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 퀴즈 수정 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuizRequest request) {
        log.info("Updating quiz with ID: {}", id);
        
        // 요청 DTO를 명령 객체로 변환
        UpdateQuizCommand command = mapToUpdateQuizCommand(id, request);
        
        // 유스케이스 실행
        Quiz quiz = updateQuizUseCase.updateQuiz(command);
        
        // 응답 DTO로 변환
        QuizResponse response = mapToQuizResponse(quiz);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable Long id) {
        log.info("Fetching quiz with ID: {}", id);
        
        Quiz quiz = getQuizUseCase.getQuiz(id);
        QuizResponse response = mapToQuizResponse(quiz);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<Page<QuizResponse>> getQuizzes(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Fetching quizzes with pageable: {}", pageable);
        
        Page<Quiz> quizzes = getQuizUseCase.getQuizzes(pageable);
        Page<QuizResponse> response = quizzes.map(this::mapToQuizResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 생성한 퀴즈 목록 조회 API
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<QuizResponse>> getUserQuizzes(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Fetching quizzes created by user ID: {}", userId);
        
        Page<Quiz> quizzes = getQuizUseCase.getQuizzesByUser(userId, pageable);
        Page<QuizResponse> response = quizzes.map(this::mapToQuizResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<Page<QuizResponse>> searchQuizzes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Boolean isPublic,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Searching quizzes with keyword: {}, difficultyLevel: {}, tagId: {}, isPublic: {}",
                keyword, difficultyLevel, tagId, isPublic);
        
        SearchQuizCommand command = SearchQuizCommand.builder()
                .keyword(keyword)
                .difficultyLevel(difficultyLevel)
                .tagId(tagId)
                .isPublic(isPublic)
                .build();
        
        Page<Quiz> quizzes = searchQuizUseCase.searchQuizzes(command, pageable);
        Page<QuizResponse> response = quizzes.map(this::mapToQuizResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 데일리 퀴즈 조회 API
     */
    @GetMapping("/daily")
    public ResponseEntity<QuizResponse> getDailyQuiz() {
        log.info("Fetching daily quiz");
        
        Quiz quiz = getQuizUseCase.getDailyQuiz();
        QuizResponse response = mapToQuizResponse(quiz);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 퀴즈 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        log.info("Deleting quiz with ID: {}", id);
        
        // 해당 ID의 퀴즈가 있는지 먼저 확인
        getQuizUseCase.getQuiz(id);
        
        // 퀴즈 삭제 (실제 삭제 메소드는 구현 필요)
        // 서비스 계층에서 구현해야 함
        
        return ResponseEntity.noContent().build();
    }

    /**
     * 요청 DTO를 퀴즈 생성 명령으로 변환
     */
    private CreateQuizCommand mapToCreateQuizCommand(CreateQuizRequest request) {
        List<CreateQuestionCommand> questionCommands = request.getQuestions().stream()
                .map(this::mapToCreateQuestionCommand)
                .collect(Collectors.toList());
        
        return CreateQuizCommand.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficultyLevel(request.getDifficultyLevel())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .passingScore(request.getPassingScore())
                .isPublic(request.getIsPublic())
                .quizType(request.getQuizType())
                .questions(questionCommands)
                .tagIds(request.getTagIds())
                .createdBy(1L) // 실제로는 인증된 사용자 ID를 사용해야 함
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 요청 DTO를 퀴즈 수정 명령으로 변환
     */
    private UpdateQuizCommand mapToUpdateQuizCommand(Long id, CreateQuizRequest request) {
        List<CreateQuestionCommand> questionCommands = request.getQuestions().stream()
                .map(this::mapToCreateQuestionCommand)
                .collect(Collectors.toList());
        
        return UpdateQuizCommand.builder()
                .id(id)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficultyLevel(request.getDifficultyLevel())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .passingScore(request.getPassingScore())
                .isPublic(request.getIsPublic())
                .quizType(request.getQuizType())
                .questions(questionCommands)
                .tagIds(request.getTagIds())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 요청 DTO를 문제 생성 명령으로 변환
     */
    private CreateQuestionCommand mapToCreateQuestionCommand(CreateQuestionRequest request) {
        List<CreateQuestionCommand.AnswerOptionCommand> optionCommands = request.getAnswerOptions().stream()
                .map(option -> CreateQuestionCommand.AnswerOptionCommand.builder()
                        .optionText(option.getOptionText())
                        .displayOrder(option.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
        
        return CreateQuestionCommand.builder()
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .points(request.getPoints())
                .explanation(request.getExplanation())
                .displayOrder(request.getDisplayOrder())
                .answerOptions(optionCommands)
                .correctAnswerIds(request.getCorrectAnswerIds())
                .build();
    }

    /**
     * 도메인 객체를 응답 DTO로 변환
     */
    private QuizResponse mapToQuizResponse(Quiz quiz) {
        List<QuestionResponse> questionResponses = quiz.getQuestions().stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
        
        Set<TagResponse> tagResponses = quiz.getTags().stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toSet());
        
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
                .updatedAt(quiz.getUpdatedAt())
                .totalAttempts(quiz.getTotalAttempts())
                .averageScore(quiz.getAverageScore())
                .passRate(quiz.getPassRate())
                .quizType(quiz.getQuizType())
                .questionCount(quiz.getQuestions().size())
                .totalPoints(calculateTotalPoints(quiz.getQuestions()))
                .questions(questionResponses)
                .tags(tagResponses)
                .build();
    }

    /**
     * 도메인 객체를 문제 응답 DTO로 변환
     */
    private QuestionResponse mapToQuestionResponse(Question question) {
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
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .displayOrder(question.getDisplayOrder())
                .answerOptions(optionResponses)
                .correctAnswerIds(question.getCorrectAnswerIds())
                .build();
    }

    /**
     * 도메인 객체를 태그 응답 DTO로 변환
     */
    private TagResponse mapToTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
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
