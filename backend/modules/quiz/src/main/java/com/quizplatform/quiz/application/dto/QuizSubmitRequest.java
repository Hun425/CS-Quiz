package com.quizplatform.quiz.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

/**
 * 퀴즈 제출 요청 DTO
 * 
 * <p>사용자가 퀴즈의 모든 답변을 제출할 때 사용하는 요청 데이터입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "퀴즈 제출 요청")
@Builder
public record QuizSubmitRequest(
    @Schema(description = "퀴즈 시도 ID", example = "1")
    @NotNull(message = "퀴즈 시도 ID는 필수입니다")
    Long attemptId,
    
    @Schema(description = "문제 답변 목록")
    @NotEmpty(message = "답변 목록은 비어있을 수 없습니다")
    @Valid
    List<QuestionAttemptRequest> answers
) {}