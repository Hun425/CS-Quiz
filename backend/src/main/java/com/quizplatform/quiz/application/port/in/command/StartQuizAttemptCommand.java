package com.quizplatform.quiz.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 시도 시작 명령
 */
@Getter
public class StartQuizAttemptCommand {
    private final Long quizId;
    private final Long userId;

    @Builder
    public StartQuizAttemptCommand(Long quizId, Long userId) {
        // 유효성 검사
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        this.quizId = quizId;
        this.userId = userId;
    }
}