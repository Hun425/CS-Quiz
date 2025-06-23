package com.quizplatform.core.domain.quiz;

/**
 * 퀴즈 유형을 정의하는 열거형
 * 
 * <p>다양한 퀴즈 유형을 구분하기 위한 상수 집합입니다.
 * 각 유형은 시스템 내에서 다르게 처리되며 사용자에게 표시될 때 사용할 설명을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public enum QuizType {
    /**
     * 일반 퀴즈 - 기본 퀴즈 유형
     */
    REGULAR("일반 퀴즈"),
    
    /**
     * 데일리 퀴즈 - 매일 갱신되는 오늘의 퀴즈
     */
    DAILY("데일리 퀴즈"),
    
    /**
     * 위클리 퀴즈 - 주간 단위로 갱신되는 퀴즈
     */
    WEEKLY("위클리 퀴즈"),
    
    /**
     * 스페셜 퀴즈 - 특별 이벤트 등에 사용되는 퀴즈
     */
    SPECIAL("스페셜 퀴즈"),
    
    /**
     * 배틀 퀴즈 - 사용자 간 대결에 사용되는 퀴즈
     */
    BATTLE("배틀 퀴즈"),
    
    /**
     * 태그 기반 퀴즈 - 특정 태그로 분류된 퀴즈
     */
    TAG_BASED("태그 기반 퀴즈"),
    
    /**
     * 주제별 퀴즈 - 특정 주제나 기술 스택으로 분류된 퀴즈
     */
    TOPIC_BASED("주제별 퀴즈"),
    
    /**
     * 사용자 맞춤 퀴즈 - 사용자가 직접 생성하거나 맞춤 설정한 퀴즈
     */
    CUSTOM("맞춤 퀴즈");

    /**
     * 퀴즈 유형 설명 (사용자에게 표시됨)
     */
    private final String description;

    /**
     * 퀴즈 유형 생성자
     * 
     * @param description 유형 설명
     */
    QuizType(String description) {
        this.description = description;
    }

    /**
     * 퀴즈 유형 설명 조회
     * 
     * @return 설명 문자열
     */
    public String getDescription() {
        return description;
    }
}