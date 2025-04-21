package com.quizplatform.modules.quiz.dto;



import com.quizplatform.modules.quiz.domain.QuestionAttempt;
import lombok.Getter;
import lombok.Builder;

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
        return com.quizplatform.modules.quiz.dto.QuestionAttemptDto.builder()
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