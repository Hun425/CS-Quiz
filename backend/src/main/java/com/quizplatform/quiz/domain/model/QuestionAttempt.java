package com.quizplatform.quiz.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문제 시도 도메인 모델
 */
@Getter
public class QuestionAttempt {
    private final Long id;
    private final Long quizAttemptId;
    private final Long questionId;
    private final String userAnswer;
    private final boolean isCorrect;
    private final Integer timeTaken;
    private final LocalDateTime createdAt;

    @Builder
    public QuestionAttempt(
            Long id,
            Long quizAttemptId,
            Long questionId,
            String userAnswer,
            boolean isCorrect,
            Integer timeTaken,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.quizAttemptId = quizAttemptId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.timeTaken = timeTaken;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * 특정 문제에 대한 시도 생성
     */
    public static QuestionAttempt createAttempt(
            Long quizAttemptId,
            Question question,
            String userAnswer,
            Integer timeTaken
    ) {
        boolean isCorrect = question.isCorrectAnswer(userAnswer);
        
        return QuestionAttempt.builder()
                .quizAttemptId(quizAttemptId)
                .questionId(question.getId())
                .userAnswer(userAnswer)
                .isCorrect(isCorrect)
                .timeTaken(timeTaken)
                .build();
    }
}