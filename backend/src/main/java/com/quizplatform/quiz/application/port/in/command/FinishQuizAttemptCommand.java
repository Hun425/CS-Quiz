package com.quizplatform.quiz.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 시도 완료 명령
 */
@Getter
public class FinishQuizAttemptCommand {
    private final Long quizAttemptId;
    private final Long userId;

    @Builder
    public FinishQuizAttemptCommand(Long quizAttemptId, Long userId) {
        // 유효성 검사
        if (quizAttemptId == null) {
            throw new IllegalArgumentException("Quiz attempt ID must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        this.quizAttemptId = quizAttemptId;
        this.userId = userId;
    }
}