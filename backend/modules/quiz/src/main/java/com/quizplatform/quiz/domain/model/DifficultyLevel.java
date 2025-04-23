package com.quizplatform.quiz.domain.model;

/**
 * 난이도 레벨 열거형
 * 
 * <p>퀴즈와 문제의 난이도를 정의합니다.
 * 초보자부터 전문가까지 다양한 수준을 지원합니다.</p>
 */
public enum DifficultyLevel {
    /**
     * 입문 난이도
     * 기초 개념과 간단한 문제
     */
    BEGINNER(1),
    
    /**
     * 초급 난이도
     * 기본 지식을 적용하는 문제
     */
    EASY(2),
    
    /**
     * 중급 난이도
     * 약간 복잡한 개념과 응용 문제
     */
    INTERMEDIATE(3),
    
    /**
     * 고급 난이도
     * 심화 개념과 복잡한 응용 문제
     */
    ADVANCED(4),
    
    /**
     * 전문가 난이도
     * 매우 높은 수준의 지식과 추론이 필요한 문제
     */
    EXPERT(5);
    
    private final int level;
    
    DifficultyLevel(int level) {
        this.level = level;
    }
    
    /**
     * 난이도 수치 반환
     * 
     * @return 난이도 수치 (1-5)
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * 보상 포인트 계산
     * 난이도에 비례하여 보상 포인트를 계산
     * 
     * @param basePoints 기본 포인트
     * @return 난이도 보정된 포인트
     */
    public int calculateRewardPoints(int basePoints) {
        return (int)(basePoints * (1 + (level - 1) * 0.25));
    }
} 