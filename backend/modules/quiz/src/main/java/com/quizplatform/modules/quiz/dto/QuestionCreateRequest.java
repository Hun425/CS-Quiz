package com.quizplatform.modules.quiz.dto;

import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.QuestionType;
import lombok.Builder;
import lombok.Getter;

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
