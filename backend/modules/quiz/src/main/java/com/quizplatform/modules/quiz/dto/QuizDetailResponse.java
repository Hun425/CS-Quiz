package com.quizplatform.modules.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 퀴즈 상세 응답 DTO
 * <p>
 * 퀴즈 상세 조회 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer timeLimit;
    private List<String> tags;
    private Long authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer attemptCount;
    private Double averageScore;
    private List<QuestionResponse> questions;
}