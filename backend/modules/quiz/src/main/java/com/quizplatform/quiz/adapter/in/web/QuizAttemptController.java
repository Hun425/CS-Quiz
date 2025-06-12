package com.quizplatform.quiz.adapter.in.web;

import com.quizplatform.common.auth.CurrentUser;
import com.quizplatform.common.auth.CurrentUserInfo;
import com.quizplatform.quiz.application.dto.*;
import com.quizplatform.quiz.domain.service.QuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 퀴즈 시도 관리 REST API 컨트롤러
 * 
 * <p>퀴즈 시도 시작, 답변 제출, 결과 조회 등의 API를 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Attempt API", description = "퀴즈 시도 관련 API")
public class QuizAttemptController {
    
    private final QuizAttemptService quizAttemptService;
    
    @PostMapping("/start")
    @Operation(summary = "퀴즈 시도 시작", description = "새로운 퀴즈 시도를 시작합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "퀴즈 시도 시작 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 진행 중인 퀴즈 시도가 있음")
    })
    public ResponseEntity<QuizAttemptResponse> startQuizAttempt(
            @RequestBody @Valid QuizAttemptRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Starting quiz attempt for quiz: {} by user: {}", 
                request.quizId(), currentUser.id());
        
        QuizAttemptResponse response = quizAttemptService.startQuizAttempt(request, currentUser.id());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/submit")
    @Operation(summary = "퀴즈 답변 제출", description = "퀴즈의 모든 답변을 제출하고 채점합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "퀴즈 제출 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "퀴즈 시도를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 완료된 퀴즈")
    })
    public ResponseEntity<QuizAttemptResponse> submitQuizAttempt(
            @RequestBody @Valid QuizSubmitRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Submitting quiz attempt: {} by user: {}", 
                request.attemptId(), currentUser.id());
        
        QuizAttemptResponse response = quizAttemptService.submitQuizAttempt(request, currentUser.id());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    @Operation(summary = "내 퀴즈 시도 목록 조회", description = "현재 사용자의 퀴즈 시도 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<List<QuizAttemptResponse>> getMyQuizAttempts(
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.debug("Getting quiz attempts for user: {}", currentUser.id());
        
        List<QuizAttemptResponse> attempts = quizAttemptService.getUserQuizAttempts(currentUser.id());
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/{attemptId}")
    @Operation(summary = "퀴즈 시도 상세 조회", description = "특정 퀴즈 시도의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "퀴즈 시도를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    public ResponseEntity<QuizAttemptResponse> getQuizAttempt(
            @PathVariable Long attemptId,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.debug("Getting quiz attempt: {} for user: {}", attemptId, currentUser.id());
        
        QuizAttemptResponse response = quizAttemptService.getQuizAttempt(attemptId, currentUser.id());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/quiz/{quizId}/latest")
    @Operation(summary = "최근 퀴즈 시도 조회", description = "특정 퀴즈의 가장 최근 시도를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "퀴즈 시도를 찾을 수 없음")
    })
    public ResponseEntity<QuizAttemptResponse> getLatestQuizAttempt(
            @PathVariable Long quizId,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.debug("Getting latest quiz attempt for quiz: {} by user: {}", quizId, currentUser.id());
        
        QuizAttemptResponse response = quizAttemptService.getLatestQuizAttempt(quizId, currentUser.id());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "퀴즈별 시도 목록 조회", description = "특정 퀴즈의 모든 시도 목록을 조회합니다. (관리자용)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<List<QuizAttemptResponse>> getQuizAttempts(
            @PathVariable Long quizId) {
        
        log.debug("Getting attempts for quiz: {}", quizId);
        
        List<QuizAttemptResponse> attempts = quizAttemptService.getQuizAttempts(quizId);
        return ResponseEntity.ok(attempts);
    }
}