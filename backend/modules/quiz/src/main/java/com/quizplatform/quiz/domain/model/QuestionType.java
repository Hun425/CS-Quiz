package com.quizplatform.quiz.domain.model;

/**
 * 문제 유형 열거형
 * 
 * <p>문제 유형을 정의합니다. 
 * 다양한 문제 형식을 지원합니다.</p>
 */
public enum QuestionType {
    /**
     * 객관식 단일 선택
     * 한 개의 정답만 선택할 수 있는 문제
     */
    SINGLE_CHOICE,
    
    /**
     * 객관식 다중 선택
     * 여러 개의 정답을 선택할 수 있는 문제
     */
    MULTIPLE_CHOICE,
    
    /**
     * 체크박스 문제
     * 여러 선택지 중 여러 개를 선택
     */
    CHECKBOX,
    
    /**
     * 단답형
     * 짧은 텍스트 답변을 입력
     */
    SHORT_ANSWER,
    
    /**
     * 서술형
     * 긴 텍스트 답변을 작성
     */
    ESSAY,
    
    /**
     * 참/거짓 문제
     * True 또는 False 중 하나를 선택
     */
    TRUE_FALSE,
    
    /**
     * 코드 작성 문제
     * 프로그래밍 코드를 작성
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