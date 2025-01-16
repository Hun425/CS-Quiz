package com.quizplatform.core.domain.quiz;

import lombok.Getter;

@Getter
public enum QuizType {
    DAILY("매일 퀴즈"),
    TAG_BASED("태그 기반 퀴즈"),
    TOPIC_BASED("주제 기반 퀴즈"),
    CUSTOM("커스텀 퀴즈");

    private final String description;

    QuizType(String description) {
        this.description = description;
    }
}