package com.quizplatform.core.domain.quiz;

import lombok.Getter;

@Getter
public enum DifficultyLevel {
    BEGINNER("초급", 50),
    INTERMEDIATE("중급", 100),
    ADVANCED("고급", 150);

    private final int baseExp;
    private final String description;

    // 두 값을 모두 받는 생성자
    DifficultyLevel(String description, int baseExp) {
        this.description = description;
        this.baseExp = baseExp;
    }
}