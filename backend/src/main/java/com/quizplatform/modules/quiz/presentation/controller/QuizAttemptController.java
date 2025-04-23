package com.quizplatform.modules.quiz.presentation.controller;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.modules.quiz.presentation.dto.QuizResultResponse;
import com.quizplatform.modules.quiz.presentation.dto.QuizSubmitRequest;
import com.quizplatform.modules.quiz.application.service.QuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 퀴즈 시도 컨트롤러 클래스
 * 
 * <p>퀴즈 풀이, 답변 제출, 결과 조회 등 퀴즈 시도 관련 API를 제공합니다.
 * 모든 API는 사용자 인증이 필요합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Attempt Controller", description = "퀴즈 풀이 및 결과 관련 API를 제공합니다.")
public class QuizAttemptController {

    /**
     * 퀴즈 시도 서비스
     */
    private final QuizAttemptService quizAttemptService;

    /**
     * 퀴즈 답변 제출 API
     * 
     * <p>사용자가 풀이한 퀴즈의 답변을 제출하고 결과를 반환합니다.
     * 제출된 답변은 채점되어 결과에 반영됩니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param quizId 퀴즈 ID
     * @param request 퀴즈 답변 제출 요청 데이터
     * @return 퀴즈 풀이 결과
     */
    @Operation(summary = "퀴즈 답변 제출", description = "퀴즈 답변을 제출하고 결과를 받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 결과가 성공적으로 반환되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "퀴즈에 접근할 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PostMapping("/{quizId}/results")
    public ResponseEntity<CommonApiResponse<QuizResultResponse>> submitQuiz(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "퀴즈 ID") @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            return ResponseEntity.status(403).body(
                    CommonApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED")
            );
        }

        // 퀴즈 ID와 요청의 퀴즈 시도 ID가 일치하는지 확인하는 로직은 서비스에서 처리

        // 결과 계산 및 저장
        QuizResultResponse result = quizAttemptService.submitQuiz(
                quizId, userPrincipal.getId(), request
        );

        return ResponseEntity.ok(CommonApiResponse.success(result));
    }

    /**
     * 퀴즈 결과 조회 API
     * 
     * <p>이전에 제출한 퀴즈 시도의 결과를 조회합니다.
     * 자신이 제출한 답변, 정답, 해설, 점수 등의 정보를 확인할 수 있습니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param quizId 퀴즈 ID
     * @param attemptId 퀴즈 시도 ID
     * @return 퀴즈 시도 결과
     */
    @Operation(summary = "퀴즈 결과 조회", description = "이전에 완료한 퀴즈의 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 결과가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "403", description = "접근할 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "결과를 찾을 수 없습니다.")
    })
    @GetMapping("/{quizId}/results/{attemptId}")
    public ResponseEntity<CommonApiResponse<QuizResultResponse>> getQuizResult(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "퀴즈 ID") @PathVariable Long quizId,
            @Parameter(description = "퀴즈 시도 ID") @PathVariable Long attemptId) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            return ResponseEntity.status(403).body(
                    CommonApiResponse.error("로그인이 필요합니다.", "UNAUTHORIZED")
            );
        }

        // 퀴즈 결과 조회
        QuizResultResponse result = quizAttemptService.getQuizResult(
                quizId, attemptId, userPrincipal.getId()
        );

        return ResponseEntity.ok(CommonApiResponse.success(result));
    }
}