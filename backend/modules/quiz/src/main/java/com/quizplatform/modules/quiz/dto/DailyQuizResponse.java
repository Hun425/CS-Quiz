package com.quizplatform.modules.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일일 퀴즈 응답 DTO
 * <p>
 * 일일 퀴즈 조회 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyQuizResponse {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer timeLimit;
    private LocalDate date;
    private QuizDetailResponse quiz;
}