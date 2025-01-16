package com.quizplatform.core.domain.quiz;

import lombok.Getter;

@Getter
public enum DifficultyLevel {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급");

    private final String description;

    DifficultyLevel(String description) {
        this.description = description;
    }
}