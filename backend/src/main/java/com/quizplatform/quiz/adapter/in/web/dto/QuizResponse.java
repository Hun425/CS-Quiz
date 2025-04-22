package com.quizplatform.quiz.adapter.in.web.dto;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 퀴즈 응답 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimitSeconds;
    private Integer passingScore;
    private Boolean isPublic;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalAttempts;
    private Double averageScore;
    private Double passRate;
    private QuizType quizType;
    private Integer questionCount;
    private Integer totalPoints;
    private List<QuestionResponse> questions = new ArrayList<>();
    private Set<TagResponse> tags = new HashSet<>();
}
