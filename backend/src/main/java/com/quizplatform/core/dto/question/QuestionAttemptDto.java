package com.quizplatform.core.dto.question;

import com.quizplatform.core.domain.question.QuestionAttempt;
import lombok.Getter;
import lombok.Builder;

import java.util.UUID;

@Getter
@Builder
public class QuestionAttemptDto {
    private Long id;
    private Long questionId;
    private String questionText;
    private String userAnswer;
    private boolean isCorrect;
    private Integer timeTaken;
    private String explanation;

    public static QuestionAttemptDto from(QuestionAttempt attempt) {
        return QuestionAttemptDto.builder()
                .id(attempt.getId())
                .questionId(attempt.getQuestion().getId())
                .questionText(attempt.getQuestion().getQuestionText())
                .userAnswer(attempt.getUserAnswer())
                .isCorrect(attempt.isCorrect())
                .timeTaken(attempt.getTimeTaken())
                .explanation(attempt.getQuestion().getExplanation())
                .build();
    }
}
