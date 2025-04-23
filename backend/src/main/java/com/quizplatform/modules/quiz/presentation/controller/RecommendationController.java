package com.quizplatform.modules.quiz.presentation.controller;

import com.quizplatform.modules.user.domain.entity.User;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.modules.quiz.presentation.dto.QuizSummaryResponse;
import com.quizplatform.modules.quiz.application.service.RecommendationService;
import com.quizplatform.modules.user.application.service.UserService;
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

/**
 * 퀴즈 추천 컨트롤러 클래스
 * 
 * <p>사용자 맞춤형 퀴즈 추천, 인기 퀴즈, 카테고리별/난이도별 추천 등
 * 다양한 기준의 퀴즈 추천 API를 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "퀴즈 추천 API", description = "사용자 맞춤형 퀴즈 추천 및 인기 퀴즈 API")
public class RecommendationController {

    /**
     * 퀴즈 추천 서비스
     */
    private final RecommendationService recommendationService;
    
    /**
     * 사용자 서비스
     */
    private final UserService userService;

    /**
     * 맞춤형 퀴즈 추천 API
     * 
     * <p>사용자의 학습 성향, 이전 퀴즈 결과, 관심사 등을 분석하여
     * 개인화된 맞춤형 퀴즈를 추천합니다. 사용자 인증이 필요합니다.</p>
     * 
     * @param userId 인증된 사용자 ID
     * @param limit 반환할 추천 퀴즈 수 (기본값 5)
     * @return 맞춤형 추천 퀴즈 목록
     */
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

    /**
     * 인기 퀴즈 추천 API
     * 
     * <p>조회수, 시도 횟수, 평점 등을 기반으로 인기 있는 퀴즈를 추천합니다.
     * 인증 없이 접근 가능합니다.</p>
     * 
     * @param limit 반환할 인기 퀴즈 수 (기본값 5)
     * @return 인기 퀴즈 목록
     */
    @Operation(summary = "인기 퀴즈 추천", description = "조회수, 완료율, 평가 등을 기반으로 인기 있는 퀴즈를 추천합니다.")
    @GetMapping("/popular")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getPopularQuizzes(
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> popularQuizzes = 
                recommendationService.getPopularQuizzes(limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(popularQuizzes));
    }

    /**
     * 카테고리별 추천 퀴즈 API
     * 
     * <p>특정 태그(카테고리)에 속한 퀴즈 중 추천 퀴즈를 제공합니다.
     * 태그 내에서 가장 인기 있거나 품질이 높은 퀴즈를 추천합니다.</p>
     * 
     * @param tagId 카테고리(태그) ID
     * @param limit 반환할 추천 퀴즈 수 (기본값 5)
     * @return 카테고리별 추천 퀴즈 목록
     */
    @Operation(summary = "특정 카테고리 추천 퀴즈", description = "특정 태그나 카테고리의 추천 퀴즈를 제공합니다.")
    @GetMapping("/category/{tagId}")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getCategoryRecommendations(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> categoryRecommendations = 
                recommendationService.getCategoryRecommendations(tagId, limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(categoryRecommendations));
    }

    /**
     * 난이도별 추천 퀴즈 API
     * 
     * <p>특정 난이도(초급, 중급, 고급)의 퀴즈 중 추천 퀴즈를 제공합니다.
     * 난이도별로 가장 인기 있거나 품질이 높은 퀴즈를 추천합니다.</p>
     * 
     * @param difficulty 난이도 (BEGINNER, INTERMEDIATE, ADVANCED)
     * @param limit 반환할 추천 퀴즈 수 (기본값 5)
     * @return 난이도별 추천 퀴즈 목록
     */
    @Operation(summary = "난이도별 추천 퀴즈", description = "특정 난이도의 추천 퀴즈를 제공합니다.")
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getDifficultyBasedRecommendations(
            @PathVariable String difficulty,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<QuizSummaryResponse> difficultyRecommendations = 
                recommendationService.getDifficultyBasedRecommendations(difficulty, limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(difficultyRecommendations));
    }

    /**
     * 오늘의 추천 퀴즈 API
     * 
     * <p>오늘의 데일리 퀴즈와 연관성이 높은 추천 퀴즈 목록을 제공합니다.
     * 동일 주제, 유사 난이도 등을 고려하여 추천합니다.</p>
     * 
     * @param limit 반환할 추천 퀴즈 수 (기본값 3)
     * @return 데일리 퀴즈 관련 추천 퀴즈 목록
     */
    @Operation(summary = "오늘의 추천 퀴즈", description = "오늘의 퀴즈(데일리 퀴즈)와 관련성 높은 추천 퀴즈 목록을 제공합니다.")
    @GetMapping("/daily-related")
    public ResponseEntity<CommonApiResponse<List<QuizSummaryResponse>>> getDailyRelatedRecommendations(
            @RequestParam(defaultValue = "3") int limit) {
        
        List<QuizSummaryResponse> dailyRelatedQuizzes = 
                recommendationService.getDailyRelatedRecommendations(limit);
        
        return ResponseEntity.ok(CommonApiResponse.success(dailyRelatedQuizzes));
    }
}