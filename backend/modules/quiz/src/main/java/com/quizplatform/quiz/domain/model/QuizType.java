package com.quizplatform.quiz.domain.model;

/**
 * 퀴즈 타입 열거형
 * 
 * <p>퀴즈 유형을 정의합니다.
 * 플랫폼에서 지원하는 다양한 퀴즈 형태를 나타냅니다.</p>
 */
public enum QuizType {
    /**
     * 일반 퀴즈
     * 사용자가 생성한 기본 퀴즈
     */
    NORMAL,
    
    /**
     * 데일리 퀴즈
     * 매일 새롭게 제공되는 퀴즈
     */
    DAILY,
    
    /**
     * 챌린지 퀴즈
     * 특별한 도전 과제가 포함된 퀴즈
     */
    CHALLENGE,
    
    /**
     * 대회 퀴즈
     * 사용자 간 대결을 위한 퀴즈
     */
    COMPETITION,
    
    /**
     * 학습 퀴즈
     * 개념 학습을 위한 퀴즈
     */
    LEARNING
} 