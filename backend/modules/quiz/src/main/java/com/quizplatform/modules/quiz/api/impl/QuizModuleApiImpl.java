package com.quizplatform.modules.quiz.api.impl;

import com.quizplatform.modules.quiz.api.QuizModuleApi;
import com.quizplatform.modules.quiz.dto.DailyQuizResponse;
import com.quizplatform.modules.quiz.dto.QuizDetailResponse;
import com.quizplatform.modules.quiz.dto.QuizRequest;
import com.quizplatform.modules.quiz.dto.QuizResponse;
import com.quizplatform.modules.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 퀴즈 모듈 API 구현체
 * <p>
 * QuizModuleApi 인터페이스를 구현하여 퀴즈 관련 API를 제공합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class QuizModuleApiImpl implements QuizModuleApi {

    private final QuizService quizService;

    @Override
    public ResponseEntity<?> getQuizById(Long quizId) {
        log.debug("Fetching quiz by ID: {}", quizId);
        QuizDetailResponse quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quiz);
    }

    @Override
    public ResponseEntity<?> getQuizzes(Map<String, Object> filters, Pageable pageable) {
        log.debug("Fetching quizzes with filters: {}", filters);
        Page<QuizResponse> quizzes = quizService.getQuizzes(filters, pageable);
        return ResponseEntity.ok(quizzes);
    }

    @Override
    public ResponseEntity<?> createQuiz(Object quizDto) {
        log.debug("Creating new quiz");
        QuizRequest quizRequest = (QuizRequest) quizDto;
        QuizDetailResponse createdQuiz = quizService.createQuiz(quizRequest);
        return ResponseEntity.ok(createdQuiz);
    }

    @Override
    public ResponseEntity<?> updateQuiz(Long quizId, Object quizDto) {
        log.debug("Updating quiz with ID: {}", quizId);
        QuizRequest quizRequest = (QuizRequest) quizDto;
        QuizDetailResponse updatedQuiz = quizService.updateQuiz(quizId, quizRequest);
        return ResponseEntity.ok(updatedQuiz);
    }

    @Override
    public ResponseEntity<?> deleteQuiz(Long quizId) {
        log.debug("Deleting quiz with ID: {}", quizId);
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getDailyQuiz() {
        log.debug("Fetching daily quiz");
        DailyQuizResponse dailyQuiz = quizService.getDailyQuiz();
        return ResponseEntity.ok(dailyQuiz);
    }

    @Override
    public ResponseEntity<?> getRecommendedQuizzes(Long userId, int limit) {
        log.debug("Fetching recommended quizzes for user: {}", userId);
        List<QuizResponse> recommendedQuizzes = quizService.getRecommendedQuizzes(userId, limit);
        return ResponseEntity.ok(recommendedQuizzes);
    }

    @Override
    public ResponseEntity<?> saveQuizAttempt(Long quizId, Object attemptDto) {
        log.debug("Saving quiz attempt for quiz: {}", quizId);
        // QuizAttemptRequest attemptRequest = (QuizAttemptRequest) attemptDto;
        // QuizAttemptResponse attemptResponse = quizService.saveQuizAttempt(quizId, attemptRequest);
        // return ResponseEntity.ok(attemptResponse);
        
        // 실제 구현에서는 위의 주석 코드를 사용
        return ResponseEntity.ok().build();
    }
}