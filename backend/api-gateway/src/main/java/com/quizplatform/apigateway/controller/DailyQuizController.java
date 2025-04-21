package com.quizplatform.apigateway.controller;


import com.quizplatform.common.dto.CommonApiResponse;
import com.quizplatform.modules.quiz.domain.Quiz;
import com.quizplatform.modules.quiz.dto.QuizDetailResponse;

import com.quizplatform.modules.quiz.service.impl.DailyQuizService;
import com.quizplatform.modules.quiz.service.impl.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데일리 퀴즈 컨트롤러 클래스
 * 
 * <p>오늘의 데일리 퀴즈 조회 및 관리를 위한 API를 제공합니다.
 * 데일리 퀴즈는 자동으로 매일 갱신되며, 관리자는 수동 갱신할 수 있습니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RestController
@RequestMapping("/api/daily-quiz")
@RequiredArgsConstructor
@Tag(name = "데일리 퀴즈 API", description = "데일리 퀴즈 조회 및 관리 API")
public class DailyQuizController {

    /**
     * 데일리 퀴즈 서비스
     */
    private final DailyQuizService dailyQuizService;
    
    /**
     * 퀴즈 서비스
     */
    private final QuizService quizService;

    /**
     * 오늘의 데일리 퀴즈 조회 API
     * 
     * <p>현재 활성화된 오늘의 데일리 퀴즈를 조회합니다.
     * 데일리 퀴즈는 매일 자동으로 선정되며, 한 번 풀이한 사용자는 다시 풀 수 없습니다.</p>
     * 
     * @return 데일리 퀴즈 상세 정보
     */
    @Operation(summary = "오늘의 데일리 퀴즈 조회", description = "현재 활성화된 데일리 퀴즈를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데일리 퀴즈 조회 성공"),
            @ApiResponse(responseCode = "404", description = "데일리 퀴즈가 없음")
    })
    @GetMapping("/today")
    public ResponseEntity<CommonApiResponse<QuizDetailResponse>> getTodayDailyQuiz() {
        Quiz dailyQuiz = dailyQuizService.getCurrentDailyQuiz();
        QuizDetailResponse response = quizService.convertToDetailResponse(dailyQuiz);
        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    /**
     * 데일리 퀴즈 수동 갱신 API
     * 
     * <p>관리자 전용 API로, 데일리 퀴즈를 수동으로 갱신합니다.
     * 시스템 문제로 자동 갱신이 안되거나 특별한 이벤트 때 사용할 수 있습니다.</p>
     * 
     * @return 갱신 결과 메시지
     */
    @Operation(summary = "데일리 퀴즈 수동 갱신", description = "관리자용: 데일리 퀴즈를 수동으로 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데일리 퀴즈 갱신 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<String>> refreshDailyQuiz() {
        dailyQuizService.selectDailyQuiz();
        return ResponseEntity.ok(CommonApiResponse.success("데일리 퀴즈가 갱신되었습니다."));
    }
}