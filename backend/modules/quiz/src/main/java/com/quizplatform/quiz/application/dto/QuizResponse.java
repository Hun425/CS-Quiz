package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.model.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 퀴즈 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    
    /**
     * 퀴즈 ID
     */
    private Long id;
    
    /**
     * 퀴즈 제목
     */
    private String title;
    
    /**
     * 퀴즈 설명
     */
    private String description;
    
    /**
     * 퀴즈 타입
     */
    private QuizType quizType;
    
    /**
     * 난이도 (1-5)
     */
    private Integer difficultyLevel;
    
    /**
     * 시간 제한 (초)
     */
    private Integer timeLimit;
    
    /**
     * 생성자 ID
     */
    private Long creatorId;
    
    /**
     * 생성자 이름
     */
    private String creatorName;
    
    /**
     * 문제 수
     */
    private Integer questionCount;
    
    /**
     * 공개 여부
     */
    private boolean published;
    
    /**
     * 활성화 여부
     */
    private boolean active;
    
    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;
    
    /**
     * 태그 목록
     */
    private List<TagResponse> tags;
    
    /**
     * 문제 목록
     */
    private List<QuestionResponse> questions;
    
    /**
     * 태그 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagResponse {
        private Long id;
        private String name;
        private String description;
    }
    
    /**
     * 문제 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponse {
        private Long id;
        private String content;
        private String type;
        private String explanation;
        private Integer points;
        private List<String> options;
        private String answer;
    }
} 