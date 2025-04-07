package com.quizplatform.core.controller.quiz;

import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;
import com.quizplatform.core.service.quiz.RecommendationService;
import com.quizplatform.core.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "퀴즈 추천 API", description = "사용자 맞춤형 퀴즈 추천 및 인기 퀴즈 API")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    @Operation(summary = "맞춤 퀴즈 추천", description = "사용자의 학습 성향과 퀴즈 성과를 분석하여 맞춤형 퀴즈를 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/personalized")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getPersonalizedRecommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        
        User user = userService.getUserById(userId);
        List<QuizSummaryResponse> recommendations = 
                recommendationService.getPersonalizedRecommendations(user, limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(recommendations));
    }

    @Operation(summary = "인기 퀴즈 추천", description = "조회수, 완료율, 평가 등을 기반으로 인기 있는 퀴즈를 추천합니다.")
    @GetMapping("/popular")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getPopularQuizzes(
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> popularQuizzes = 
                recommendationService.getPopularQuizzes(limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(popularQuizzes));
    }

    @Operation(summary = "특정 카테고리 추천 퀴즈", description = "특정 태그나 카테고리의 추천 퀴즈를 제공합니다.")
    @GetMapping("/category/{tagId}")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getCategoryRecommendations(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> categoryRecommendations = 
                recommendationService.getCategoryRecommendations(tagId, limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(categoryRecommendations));
    }

    @Operation(summary = "난이도별 추천 퀴즈", description = "특정 난이도의 추천 퀴즈를 제공합니다.")
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getDifficultyBasedRecommendations(
            @PathVariable String difficulty,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> difficultyRecommendations = 
                recommendationService.getDifficultyBasedRecommendations(difficulty, limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(difficultyRecommendations));
    }

    @Operation(summary = "오늘의 추천 퀴즈", description = "오늘의 퀴즈(데일리 퀴즈)와 관련성 높은 추천 퀴즈 목록을 제공합니다.")
    @GetMapping("/daily-related")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getDailyRelatedRecommendations(
            @RequestParam(defaultValue = "3") int limit) {
        
        List<QuizSummaryResponse> dailyRelatedQuizzes = 
                recommendationService.getDailyRelatedRecommendations(limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(dailyRelatedQuizzes));
    }
}