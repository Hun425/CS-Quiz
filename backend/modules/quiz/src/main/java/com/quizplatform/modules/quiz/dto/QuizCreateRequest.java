package com.quizplatform.modules.quiz.dto;


import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.QuizType;
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
