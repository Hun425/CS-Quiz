package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.QuestionType;
import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import lombok.Getter;
import lombok.Builder;

import java.util.List;

@Getter
@Builder
public class QuestionCreateRequest {
    private QuestionType questionType;
    private String questionText;
    private String codeSnippet;
    private String diagramData;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private int points;
    private DifficultyLevel difficultyLevel;
}
