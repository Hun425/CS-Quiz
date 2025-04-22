package com.quizplatform.quiz.domain.model;

import lombok.Getter;

/**
 * 문제 유형을 정의하는 열거형 클래스
 */
@Getter
public enum QuestionType {
    /**
     * 객관식 문제 - 여러 선택지 중 하나를 선택
     */
    MULTIPLE_CHOICE("객관식", "여러 선택지 중 하나를 선택하는 문제"),
    
    /**
     * 참/거짓 문제 - 참과 거짓 중 하나를 선택
     */
    TRUE_FALSE("참/거짓", "참과 거짓 중 하나를 선택하는 문제"),
    
    /**
     * 주관식 문제 - 간단한 답을 직접 입력
     */
    SHORT_ANSWER("주관식", "간단한 답을 직접 입력하는 문제"),
    
    /**
     * 코드 분석 문제 - 주어진 코드를 분석하여 답변
     */
    CODE_ANALYSIS("코드 분석", "주어진 코드를 분석하여 답하는 문제"),
    
    /**
     * 다이어그램 문제 - 다이어그램을 보고 답변
     */
    DIAGRAM_BASED("다이어그램", "다이어그램을 보고 답하는 문제");

    /**
     * 화면에 표시되는 문제 유형 이름
     */
    private final String displayName;
    
    /**
     * 문제 유형에 대한 설명
     */
    private final String description;

    /**
     * 생성자
     * 
     * @param displayName 화면에 표시할 이름
     * @param description 설명
     */
    QuestionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}