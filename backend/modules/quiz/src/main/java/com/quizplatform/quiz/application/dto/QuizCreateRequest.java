package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.model.QuizType;
import jakarta.validation.Valid;
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
 * 퀴즈 생성 요청 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateRequest {
    
    /**
     * 퀴즈 제목
     */
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다")
    private String title;
    
    /**
     * 퀴즈 설명
     */
    @NotBlank(message = "설명은 필수입니다")
    @Size(min = 10, max = 1000, message = "설명은 10자 이상 1000자 이하여야 합니다")
    private String description;
    
    /**
     * 퀴즈 타입
     */
    @NotNull(message = "퀴즈 타입은 필수입니다")
    private QuizType quizType;
    
    /**
     * 난이도 (1-5)
     */
    @NotNull(message = "난이도는 필수입니다")
    private Integer difficultyLevel;
    
    /**
     * 시간 제한 (초)
     */
    private Integer timeLimit;
    
    /**
     * 태그 ID 목록
     */
    @NotEmpty(message = "최소한 하나의 태그가 필요합니다")
    private List<Long> tagIds;
    
    /**
     * 문제 목록
     */
    @NotEmpty(message = "최소한 하나의 문제가 필요합니다")
    @Valid
    private List<QuestionCreateRequest> questions;
} 