package adapter.in.web;

import adapter.in.web.dto.*;
import application.port.in.CreateQuizUseCase;
import application.port.in.GetQuizUseCase;
import application.port.in.SearchQuizUseCase;
import application.port.in.UpdateQuizUseCase;
import application.port.in.command.CreateQuestionCommand;
import application.port.in.command.CreateQuizCommand;
import application.port.in.command.SearchQuizCommand;
import application.port.in.command.UpdateQuizCommand;
import domain.model.Question;
import domain.model.Quiz;
import domain.model.Tag;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 퀴즈 컨트롤러 클래스
 * 퀴즈 생성, 조회, 수정, 삭제 등의 API 제공
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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
            @Valid @RequestBody UpdateQuizRequest request) {
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
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) Boolean isPublic,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("Searching quizzes with keyword: {}, difficulty: {}, tagIds: {}, isPublic: {}",
                keyword, difficulty, tagIds, isPublic);
        
        SearchQuizCommand command = SearchQuizCommand.builder()
                .keyword(keyword)
                .tagIds(tagIds != null ? Set.copyOf(tagIds) : null)
                .difficulty(difficulty)
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
                .createdBy(request.getUserId())
                .quizType(request.getQuizType())
                .tags(request.getTagIds() != null ? request.getTagIds().stream().map(Tag::new).collect(Collectors.toSet()) : Set.of())
                .questions(questionCommands)
                .build();
    }

    /**
     * 요청 DTO를 퀴즈 수정 명령으로 변환
     */
    private UpdateQuizCommand mapToUpdateQuizCommand(Long quizId, UpdateQuizRequest request) {
        List<CreateQuestionCommand> questionCommands = request.getQuestions().stream()
                .map(this::mapToCreateQuestionCommand)
                .collect(Collectors.toList());
        
        return UpdateQuizCommand.builder()
                .quizId(quizId)
                .title(request.getTitle())
                .description(request.getDescription())
                .difficultyLevel(request.getDifficultyLevel())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .passingScore(request.getPassingScore())
                .isPublic(request.getIsPublic())
                .quizType(request.getQuizType())
                .tags(request.getTagIds() != null ? request.getTagIds().stream().map(Tag::new).collect(Collectors.toSet()) : Set.of())
                .questions(questionCommands)
                .build();
    }

    /**
     * 요청 DTO를 문제 생성 명령으로 변환
     */
    private CreateQuestionCommand mapToCreateQuestionCommand(CreateQuestionRequest request) {
        List<CreateQuestionCommand.AnswerOptionCommand> optionCommands = request.getAnswerOptions().stream()
                .map(opt -> CreateQuestionCommand.AnswerOptionCommand.builder()
                        .text(opt.getText())
                        .displayOrder(opt.getDisplayOrder())
                        .isCorrect(opt.getIsCorrect())
                        .build())
                .collect(Collectors.toList());

        return CreateQuestionCommand.builder()
                .text(request.getText())
                .questionType(request.getQuestionType())
                .points(request.getPoints())
                .explanation(request.getExplanation())
                .displayOrder(request.getDisplayOrder())
                .codeSnippet(request.getCodeSnippet())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .options(optionCommands)
                .build();
    }

    /**
     * 도메인 객체를 응답 DTO로 변환
     */
    private QuizResponse mapToQuizResponse(Quiz quiz) {
        List<QuestionResponse> questionResponses = new ArrayList<>(quiz.getQuestions()).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
        
        List<TagResponse> tagResponses = new ArrayList<>(quiz.getTags()).stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
        
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .difficulty(quiz.getDifficultyLevel() != null ? quiz.getDifficultyLevel().name() : null)
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .passingScore(quiz.getPassingScore())
                .isPublic(quiz.isPublic())
                .createdBy(quiz.getCreatedBy())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .viewCount(quiz.getViewCount())
                .totalAttempts(quiz.getTotalAttempts())
                .averageScore(quiz.getAverageScore())
                .passRate(quiz.getPassRate())
                .quizType(quiz.getQuizType() != null ? quiz.getQuizType().name() : null)
                .questionCount(quiz.getQuestions().size())
                .totalPoints(calculateTotalPoints(new ArrayList<>(quiz.getQuestions())))
                .questions(questionResponses)
                .tags(tagResponses)
                .build();
    }

    /**
     * 도메인 객체를 문제 응답 DTO로 변환
     */
    private QuestionResponse mapToQuestionResponse(Question question) {
        List<AnswerOptionResponse> optionResponses = new ArrayList<>(question.getOptions()).stream()
                .map(option -> AnswerOptionResponse.builder()
                        .id(option.getId())
                        .text(option.getText())
                        .displayOrder(option.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
        
        return QuestionResponse.builder()
                .id(question.getId())
                .text(question.getText())
                .questionType(question.getQuestionType() != null ? question.getQuestionType().name() : null)
                .points(question.getPoints())
                .explanation(question.getExplanation())
                .displayOrder(question.getDisplayOrder())
                .codeSnippet(question.getCodeSnippet())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .difficulty(question.getDifficultyLevel() != null ? question.getDifficultyLevel().name() : null)
                .correctAnswer(question.getCorrectAnswer())
                .options(optionResponses)
                .build();
    }

    /**
     * 도메인 객체를 태그 응답 DTO로 변환
     */
    private TagResponse mapToTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    /**
     * 문제 목록으로부터 총 배점 계산
     */
    private Integer calculateTotalPoints(List<Question> questions) {
        if (questions == null) {
            return 0;
        }
        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }
}
