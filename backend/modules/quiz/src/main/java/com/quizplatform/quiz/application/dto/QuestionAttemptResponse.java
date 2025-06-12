package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.model.QuestionAttempt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 문제 시도 응답 DTO
 * 
 * <p>문제 시도 정보를 반환하는 응답 데이터입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "문제 시도 응답")
@Builder
public record QuestionAttemptResponse(
    @Schema(description = "시도 ID", example = "1")
    Long id,
    
    @Schema(description = "문제 ID", example = "1")
    Long questionId,
    
    @Schema(description = "사용자 답변", example = "1")
    String userAnswer,
    
    @Schema(description = "정답 여부", example = "true")
    boolean correct,
    
    @Schema(description = "시도 시간")
    LocalDateTime attemptTime,
    
    @Schema(description = "소요 시간(초)", example = "30")
    int timeSpentSeconds
) {
    
    /**
     * QuestionAttempt 엔티티로부터 응답 DTO 생성
     * 
     * @param attempt 문제 시도 엔티티
     * @return 문제 시도 응답 DTO
     */
    public static QuestionAttemptResponse from(QuestionAttempt attempt) {
        return QuestionAttemptResponse.builder()
                .id(attempt.getId())
                .questionId(attempt.getQuestionId())
                .userAnswer(attempt.getUserAnswer())
                .correct(attempt.isCorrect())
                .attemptTime(attempt.getAttemptTime())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .build();
    }
}