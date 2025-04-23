package com.quizplatform.quiz.domain.model;

/**
 * 문제 유형 열거형
 * 
 * <p>문제 유형을 정의합니다. 
 * 다양한 문제 형식을 지원합니다.</p>
 */
public enum QuestionType {
    /**
     * 객관식 문제
     * 여러 선택지 중 하나를 선택
     */
    MULTIPLE_CHOICE,
    
    /**
     * 체크박스 문제
     * 여러 선택지 중 여러 개를 선택
     */
    CHECKBOX,
    
    /**
     * 단답형 문제
     * 짧은 텍스트 답변
     */
    SHORT_ANSWER,
    
    /**
     * 서술형 문제
     * 긴 텍스트 답변
     */
    ESSAY,
    
    /**
     * 참/거짓 문제
     * 참과 거짓 중 하나를 선택
     */
    TRUE_FALSE,
    
    /**
     * 코드 작성 문제
     * 프로그래밍 코드를 작성하는 문제
     */
    CODE,
    
    /**
     * 순서 배열 문제
     * 항목을 정확한 순서로 배열
     */
    ORDERING,
    
    /**
     * 매칭 문제
     * 두 집합의 항목을 매칭
     */
    MATCHING,
    
    /**
     * 빈칸 채우기 문제
     * 문장 속 빈칸을 채우는 문제
     */
    FILL_IN_BLANK
} 