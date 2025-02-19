package com.quizplatform.core.dto.question;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionType;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class QuestionResponse {
    private UUID id;
    private QuestionType questionType;
    private String questionText;
    private String codeSnippet;
    private String diagramData;
    private List<String> options;
    private String explanation;
    private int points;
    private DifficultyLevel difficultyLevel;
    private int timeLimitSeconds;

    public static QuestionResponse from(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .codeSnippet(question.getCodeSnippet())
                .diagramData(question.getDiagramData())
                .options(question.getOptionList())
                .explanation(question.getExplanation())
                .points(question.getPoints())
                .difficultyLevel(question.getDifficultyLevel())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .build();
    }
}
