package com.quizplatform.quiz.domain.model;

/**
 * 문제 유형 열거형
 * 
 * <p>퀴즈 문제의 다양한 유형을 정의합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public enum QuestionType {
    /**
     * 객관식 (단일 선택)
     */
    SINGLE_CHOICE,
    
    /**
     * 객관식 (다중 선택)
     */
    MULTIPLE_CHOICE,
    
    /**
     * 주관식
     */
    TEXT,
    
    /**
     * 참/거짓
     */
    TRUE_FALSE,
    
    /**
     * 코드 작성
     */
    CODE
} 