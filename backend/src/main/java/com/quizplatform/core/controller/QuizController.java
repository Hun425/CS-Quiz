package com.quizplatform.core.controller;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizStatistics;
import com.quizplatform.core.dto.common.ApiResponse;
import com.quizplatform.core.dto.common.PageResponse;
import com.quizplatform.core.dto.quiz.*;
import com.quizplatform.core.service.quiz.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Validated
public class QuizController {
    private final QuizService quizService;

    /**
     * 새로운 퀴즈를 생성합니다.
     * 퀴즈 생성자의 권한 검증이 필요합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody QuizCreateRequest request) {
        Quiz quiz = quizService.createQuiz(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(QuizResponse.from(quiz)));
    }

    /**
     * 기존 퀴즈를 수정합니다.
     * 퀴즈 생성자나 관리자만 수정할 수 있습니다.
     */
    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizCreateRequest request) {
        Quiz quiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(ApiResponse.success(QuizResponse.from(quiz)));
    }

    /**
     * 퀴즈 상세 정보를 조회합니다.
     * 문제 내용은 퀴즈 시작 시에만 제공됩니다.
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuiz(
            @PathVariable UUID quizId) {
        Quiz quiz = quizService.getQuizWithoutQuestions(quizId);
        return ResponseEntity.ok(ApiResponse.success(QuizDetailResponse.from(quiz)));
    }

    /**
     * 퀴즈 목록을 검색합니다.
     * 태그, 난이도, 제목 등으로 필터링할 수 있습니다.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<QuizSummaryResponse>>> searchQuizzes(
            @ModelAttribute QuizSearchRequest request,
            Pageable pageable) {
        Page<Quiz> quizzes = quizService.searchQuizzes(request.toCondition(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
                quizzes.map(QuizSummaryResponse::from))));
    }

    /**
     * 오늘의 데일리 퀴즈를 조회합니다.
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<QuizResponse>> getDailyQuiz() {
        Quiz dailyQuiz = quizService.getCurrentDailyQuiz();
        return ResponseEntity.ok(ApiResponse.success(QuizResponse.from(dailyQuiz)));
    }

    /**
     * 사용자에게 추천되는 퀴즈 목록을 조회합니다.
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<QuizSummaryResponse>>> getRecommendedQuizzes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "5") int limit) {
        List<Quiz> recommendedQuizzes = quizService.getRecommendedQuizzes(
                userPrincipal.getUser(), limit);
        return ResponseEntity.ok(ApiResponse.success(
                recommendedQuizzes.stream()
                        .map(QuizSummaryResponse::from)
                        .collect(Collectors.toList())));
    }

    /**
     * 특정 태그의 퀴즈 목록을 조회합니다.
     */
    @GetMapping("/tags/{tagId}")
    public ResponseEntity<ApiResponse<PageResponse<QuizSummaryResponse>>> getQuizzesByTag(
            @PathVariable UUID tagId,
            Pageable pageable) {
        Page<Quiz> quizzes = quizService.getQuizzesByTag(tagId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
                quizzes.map(QuizSummaryResponse::from))));
    }

    /**
     * 퀴즈의 통계 정보를 조회합니다.
     */
    @GetMapping("/{quizId}/statistics")
    public ResponseEntity<ApiResponse<QuizStatisticsResponse>> getQuizStatistics(
            @PathVariable UUID quizId) {
        QuizStatistics statistics = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(ApiResponse.success(QuizStatisticsResponse.from(statistics)));
    }
}

