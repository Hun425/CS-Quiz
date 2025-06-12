package com.quizplatform.quiz.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 퀴즈 시도 시작 요청 DTO
 * 
 * <p>사용자가 퀴즈를 시작할 때 사용하는 요청 데이터입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "퀴즈 시도 시작 요청")
@Builder
public record QuizAttemptRequest(
    @Schema(description = "퀴즈 ID", example = "1")
    @NotNull(message = "퀴즈 ID는 필수입니다")
    Long quizId
) {}