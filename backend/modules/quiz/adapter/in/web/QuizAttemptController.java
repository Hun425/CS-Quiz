package com.example.quiz.adapter.in.web;

import com.example.quiz.adapter.in.web.dto.AnswerOptionResponse;
import com.example.quiz.adapter.in.web.dto.QuestionAttemptResponse;
import com.example.quiz.adapter.in.web.dto.QuizAttemptResponse;
import com.example.quiz.application.port.in.QuizAttemptUseCase;
import com.example.quiz.application.port.in.SubmitAnswerCommand;
import com.example.quiz.domain.model.AttemptStatus;
import com.example.quiz.domain.model.Question;
import com.example.quiz.domain.model.QuestionAttempt;
import com.example.quiz.domain.model.QuizAttempt;
import com.example.quiz.domain.model.AnswerOption;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attempts")
public class QuizAttemptController {

    private final QuizAttemptUseCase quizAttemptUseCase;

    public QuizAttemptController(QuizAttemptUseCase quizAttemptUseCase) {
        this.quizAttemptUseCase = quizAttemptUseCase;
    }

    @PostMapping("/start")
    public ResponseEntity<QuizAttemptResponse> startQuizAttempt(@RequestParam Long userId, @RequestParam Long quizId) {
        QuizAttempt attempt = quizAttemptUseCase.startQuizAttempt(userId, quizId);
        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizAttemptResponse> submitAnswer(
            @PathVariable Long attemptId,
            @RequestBody SubmitAnswerCommand command
    ) {
        QuizAttempt attempt = quizAttemptUseCase.submitAnswer(attemptId, command);
        return ResponseEntity.ok(mapToResponse(attempt));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<QuizAttemptResponse> getQuizAttempt(@PathVariable Long attemptId) {
        QuizAttempt attempt = quizAttemptUseCase.getQuizAttemptById(attemptId);
        return ResponseEntity.ok(mapToResponse(attempt));
    }

    private QuizAttemptResponse mapToResponse(QuizAttempt attempt) {
        List<QuestionAttemptResponse> questionAttemptResponses = attempt.getQuestionAttempts().stream()
                .map(qa -> {
                    Question question = qa.getQuestion();
                    if (question == null) {
                        return new QuestionAttemptResponse(
                                qa.getId(),
                                null,
                                "Question not found",
                                List.of(),
                                qa.getSelectedOptionId(),
                                qa.getIsCorrect(),
                                qa.getPointsEarned(),
                                qa.getTimeTaken(),
                                qa.getSubmittedAt()
                         );
                    }

                    List<AnswerOptionResponse> options = question.getOptions().stream()
                            .map(opt -> new AnswerOptionResponse(
                                    opt.getId(),
                                    opt.getText(),
                                    opt.getDisplayOrder()
                            ))
                            .collect(Collectors.toList());

                    return new QuestionAttemptResponse(
                            qa.getId(),
                            question.getId(),
                            question.getText(),
                            options,
                            qa.getSelectedOptionId(),
                            qa.getIsCorrect(),
                            qa.getPointsEarned(),
                            qa.getTimeTaken(),
                            qa.getSubmittedAt()
                    );
                })
                .collect(Collectors.toList());

        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getUserId(),
                attempt.getQuizId(),
                attempt.getStartTime(),
                attempt.getEndTime(),
                attempt.getScore(),
                attempt.isCompleted(),
                attempt.getStatus() != null ? attempt.getStatus().name() : AttemptStatus.UNKNOWN.name(),
                attempt.getTimeTaken(),
                attempt.getPassed(),
                questionAttemptResponses
        );
    }
}
