package com.quizplatform.quiz.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 답변 제출 명령
 */
@Getter
public class SubmitAnswerCommand {
    private final Long quizAttemptId;
    private final Long questionId;
    private final String userAnswer;
    private final Integer timeTaken;

    @Builder
    public SubmitAnswerCommand(
            Long quizAttemptId,
            Long questionId,
            String userAnswer,
            Integer timeTaken
    ) {
        // 유효성 검사
        if (quizAttemptId == null) {
            throw new IllegalArgumentException("Quiz attempt ID must not be null");
        }
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID must not be null");
        }
        if (userAnswer == null) {
            throw new IllegalArgumentException("User answer must not be null");
        }

        this.quizAttemptId = quizAttemptId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.timeTaken = timeTaken;
    }
}