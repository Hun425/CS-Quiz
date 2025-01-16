package com.quizplatform.core.domain.quiz;

import lombok.Getter;

@Getter
public enum QuestionType {
    MULTIPLE_CHOICE("객관식", "여러 선택지 중 하나를 선택하는 문제"),
    TRUE_FALSE("참/거짓", "참과 거짓 중 하나를 선택하는 문제"),
    SHORT_ANSWER("주관식", "간단한 답을 직접 입력하는 문제"),
    CODE_ANALYSIS("코드 분석", "주어진 코드를 분석하여 답하는 문제"),
    DIAGRAM_BASED("다이어그램", "다이어그램을 보고 답하는 문제");

    private final String displayName;
    private final String description;

    QuestionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}