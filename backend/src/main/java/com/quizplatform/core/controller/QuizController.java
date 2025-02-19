package com.quizplatform.core.controller;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizStatistics;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.common.PageResponse;
import com.quizplatform.core.dto.quiz.*;
import com.quizplatform.core.service.quiz.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Quiz Controller", description = "퀴즈 관련 API를 제공합니다.")
public class QuizController {
    private final QuizService quizService;

    @Operation(summary = "퀴즈 생성", description = "새로운 퀴즈를 생성합니다. 퀴즈 생성자의 권한 검증이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈가 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "접근이 거부되었습니다.")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<QuizResponse>> createQuiz(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody QuizCreateRequest request) {
        Quiz quiz = quizService.createQuiz(userPrincipal.getId(), request);
        return ResponseEntity.ok(CommonApiResponse.success(QuizResponse.from(quiz)));
    }

    @Operation(summary = "퀴즈 수정", description = "기존 퀴즈를 수정합니다. 퀴즈 생성자나 관리자만 수정할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "접근이 거부되었습니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PutMapping("/{quizId}")
    public ResponseEntity<CommonApiResponse<QuizResponse>> updateQuiz(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizCreateRequest request) {
        Quiz quiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(CommonApiResponse.success(QuizResponse.from(quiz)));
    }

    @Operation(summary = "퀴즈 상세 조회", description = "문제 내용 없이 퀴즈 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 상세 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/{quizId}")
    public ResponseEntity<CommonApiResponse<QuizDetailResponse>> getQuiz(
            @PathVariable UUID quizId) {
        Quiz quiz = quizService.getQuizWithoutQuestions(quizId);
        return ResponseEntity.ok(CommonApiResponse.success(QuizDetailResponse.from(quiz)));
    }

    @Operation(summary = "퀴즈 검색", description = "태그, 난이도, 제목 등으로 퀴즈를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @GetMapping("/search")
    public ResponseEntity<CommonApiResponse<PageResponse<QuizSummaryResponse>>> searchQuizzes(
            @ModelAttribute QuizSearchRequest request,
            Pageable pageable) {
        Page<Quiz> quizzes = quizService.searchQuizzes(request.toCondition(), pageable);
        return ResponseEntity.ok(CommonApiResponse.success(PageResponse.of(
                quizzes.map(QuizSummaryResponse::from))));
    }

    @Operation(summary = "데일리 퀴즈 조회", description = "오늘의 데일리 퀴즈를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데일리 퀴즈가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "데일리 퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/daily")
    public ResponseEntity<CommonApiResponse<QuizResponse>> getDailyQuiz() {
        Quiz dailyQuiz = quizService.getCurrentDailyQuiz();
        return ResponseEntity.ok(CommonApiResponse.success(QuizResponse.from(dailyQuiz)));
    }

    @Operation(summary = "추천 퀴즈 조회", description = "사용자에게 추천되는 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "추천 퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/recommended")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getRecommendedQuizzes(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "5") int limit) {
        List<Quiz> recommendedQuizzes = quizService.getRecommendedQuizzes(
                userPrincipal.getUser(), limit);
        return ResponseEntity.ok(CommonApiResponse.success(
                recommendedQuizzes.stream()
                        .map(QuizSummaryResponse::from)
                        .collect(Collectors.toList())));
    }

    @Operation(summary = "태그별 퀴즈 조회", description = "특정 태그의 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그별 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 태그의 퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/tags/{tagId}")
    public ResponseEntity<CommonApiResponse<PageResponse<QuizSummaryResponse>>> getQuizzesByTag(
            @PathVariable UUID tagId,
            Pageable pageable) {
        Page<Quiz> quizzes = quizService.getQuizzesByTag(tagId, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(PageResponse.of(
                quizzes.map(QuizSummaryResponse::from))));
    }

    @Operation(summary = "퀴즈 통계 조회", description = "특정 퀴즈의 통계 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 통계가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/{quizId}/statistics")
    public ResponseEntity<CommonApiResponse<QuizStatisticsResponse>> getQuizStatistics(
            @PathVariable UUID quizId) {
        QuizStatistics statistics = quizService.getQuizStatistics(quizId);
        return ResponseEntity.ok(CommonApiResponse.success(QuizStatisticsResponse.from(statistics)));
    }
}
