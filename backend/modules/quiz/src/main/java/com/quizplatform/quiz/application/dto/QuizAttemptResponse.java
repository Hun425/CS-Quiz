package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.model.QuizAttempt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 퀴즈 시도 응답 DTO
 * 
 * <p>퀴즈 시도 정보를 반환하는 응답 데이터입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "퀴즈 시도 응답")
@Builder
public record QuizAttemptResponse(
    @Schema(description = "시도 ID", example = "1")
    Long id,
    
    @Schema(description = "퀴즈 ID", example = "1")
    Long quizId,
    
    @Schema(description = "퀴즈 제목", example = "Java 기초 퀴즈")
    String quizTitle,
    
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "시작 시간")
    LocalDateTime startTime,
    
    @Schema(description = "종료 시간")
    LocalDateTime endTime,
    
    @Schema(description = "완료 여부", example = "true")
    boolean completed,
    
    @Schema(description = "통과 여부", example = "true")
    boolean passed,
    
    @Schema(description = "획득 점수", example = "85")
    int score,
    
    @Schema(description = "총 문제 수", example = "10")
    int totalQuestions,
    
    @Schema(description = "소요 시간(초)", example = "1200")
    long durationSeconds,
    
    @Schema(description = "문제 시도 목록")
    List<QuestionAttemptResponse> questionAttempts
) {
    
    /**
     * QuizAttempt 엔티티로부터 응답 DTO 생성
     * 
     * @param attempt 퀴즈 시도 엔티티
     * @return 퀴즈 시도 응답 DTO
     */
    public static QuizAttemptResponse from(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .userId(attempt.getUserId())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .completed(attempt.isCompleted())
                .passed(attempt.isPassed())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .durationSeconds(attempt.getDurationInSeconds())
                .questionAttempts(attempt.getQuestionAttempts().stream()
                        .map(QuestionAttemptResponse::from)
                        .toList())
                .build();
    }
    
    /**
     * 문제 시도 목록 없이 간단한 응답 생성
     * 
     * @param attempt 퀴즈 시도 엔티티
     * @return 간단한 퀴즈 시도 응답 DTO
     */
    public static QuizAttemptResponse fromWithoutQuestions(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .userId(attempt.getUserId())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .completed(attempt.isCompleted())
                .passed(attempt.isPassed())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .durationSeconds(attempt.getDurationInSeconds())
                .questionAttempts(List.of())
                .build();
    }
}