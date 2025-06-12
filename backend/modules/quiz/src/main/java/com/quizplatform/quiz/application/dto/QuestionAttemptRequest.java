package com.quizplatform.quiz.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 문제 답변 제출 요청 DTO
 * 
 * <p>사용자가 문제에 대한 답변을 제출할 때 사용하는 요청 데이터입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "문제 답변 제출 요청")
@Builder
public record QuestionAttemptRequest(
    @Schema(description = "문제 ID", example = "1")
    @NotNull(message = "문제 ID는 필수입니다")
    Long questionId,
    
    @Schema(description = "사용자 답변", example = "1")
    @NotBlank(message = "답변은 필수입니다")
    String userAnswer,
    
    @Schema(description = "소요 시간(초)", example = "30")
    int timeSpentSeconds
) {}