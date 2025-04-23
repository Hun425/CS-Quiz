package com.quizplatform.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 문제 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {
    
    /**
     * 문제 내용
     */
    @NotBlank(message = "문제 내용은 필수입니다")
    @Size(min = 5, max = 500, message = "문제 내용은 5자 이상 500자 이하여야 합니다")
    private String content;
    
    /**
     * 문제 유형
     */
    @NotBlank(message = "문제 유형은 필수입니다")
    private String type;
    
    /**
     * 정답 설명
     */
    private String explanation;
    
    /**
     * 배점
     */
    @NotNull(message = "배점은 필수입니다")
    private Integer points;
    
    /**
     * 보기 목록
     */
    @NotEmpty(message = "최소한 하나의 보기가 필요합니다")
    private List<String> options;
    
    /**
     * 정답
     */
    @NotBlank(message = "정답은 필수입니다")
    private String answer;
} 