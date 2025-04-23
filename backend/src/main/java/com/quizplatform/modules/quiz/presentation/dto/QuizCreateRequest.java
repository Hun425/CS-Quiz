package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 퀴즈 생성을 위한 DTO
@Getter
@Builder
public class QuizCreateRequest {
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimit;
    private List<Long> tagIds;
    private List<QuestionCreateRequest> questions;
}
