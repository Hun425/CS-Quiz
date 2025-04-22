package com.quizplatform.quiz.domain.model;

import lombok.Getter;

/**
 * 퀴즈와 문제의 난이도를 정의하는 열거형
 */
@Getter
public enum DifficultyLevel {
    /**
     * 초급 난이도 - 기본 경험치 50
     */
    BEGINNER("초급", 50),
    
    /**
     * 중급 난이도 - 기본 경험치 100
     */
    INTERMEDIATE("중급", 100),
    
    /**
     * 고급 난이도 - 기본 경험치 150
     */
    ADVANCED("고급", 150);

    /**
     * 난이도별 기본 획득 경험치
     */
    private final int baseExp;
    
    /**
     * 난이도 설명 (사용자에게 표시됨)
     */
    private final String description;

    /**
     * 난이도 생성자
     * 
     * @param description 난이도 설명
     * @param baseExp 기본 획득 경험치
     */
    DifficultyLevel(String description, int baseExp) {
        this.description = description;
        this.baseExp = baseExp;
    }
}