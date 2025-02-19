package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.dto.question.QuestionCreateRequest;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

// 퀴즈 생성을 위한 DTO
@Getter
@Builder
public class QuizCreateRequest {
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimit;
    private List<UUID> tagIds;
    private List<QuestionCreateRequest> questions;
}
