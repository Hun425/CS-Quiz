package com.quizplatform.modules.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 퀴즈 생성/수정 요청 DTO
 * <p>
 * 퀴즈 생성 및 수정 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
    private String title;
    private String description;
    private String difficulty;
    private String category;
    private Integer timeLimit;
    private List<String> tags;
    private List<QuestionRequest> questions;
}