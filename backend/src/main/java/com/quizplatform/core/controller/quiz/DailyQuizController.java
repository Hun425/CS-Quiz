package com.quizplatform.core.controller.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.quiz.QuizDetailResponse;
import com.quizplatform.core.service.quiz.DailyQuizService;
import com.quizplatform.core.service.quiz.QuizService;
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

@RestController
@RequestMapping("/api/daily-quiz")
@RequiredArgsConstructor
@Tag(name = "데일리 퀴즈 API", description = "데일리 퀴즈 조회 및 관리 API")
public class DailyQuizController {

    private final DailyQuizService dailyQuizService;
    private final QuizService quizService;

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