package com.quizplatform.core.domain.quiz;

/**
 * 퀴즈 유형을 정의하는 열거형
 */
public enum QuizType {
    REGULAR("일반 퀴즈"),
    DAILY("데일리 퀴즈"),
    WEEKLY("위클리 퀴즈"),
    SPECIAL("스페셜 퀴즈"),
    BATTLE("배틀 퀴즈");

    private final String description;

    QuizType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}