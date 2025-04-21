package com.quizplatform.modules.quiz.domain;

import lombok.Getter;

/**
 * 업적 시스템 열거형 클래스
 * 
 * <p>사용자가 달성할 수 있는 다양한 업적을 정의합니다.
 * 각 업적은 이름, 설명, 아이콘 URL 및 달성 조건 설명을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public enum Achievement {
    /**
     * 첫 퀴즈 완료 업적
     */
    FIRST_QUIZ_COMPLETED("첫 퀴즈 완료", "첫 번째 퀴즈를 완료하세요", "/images/achievements/first-quiz.png", "첫 퀴즈를 완료하세요"),
    
    /**
     * 완벽한 점수 업적
     */
    PERFECT_SCORE("완벽한 점수", "퀴즈에서 만점을 받으세요", "/images/achievements/perfect-score.png", "퀴즈에서 100점을 받으세요"),
    
    /**
     * 3연승 업적
     */
    WINNING_STREAK_3("3연승", "배틀에서 3연승을 달성하세요", "/images/achievements/streak-3.png", "배틀에서 3연속 승리하세요"),
    
    /**
     * 5연승 업적
     */
    WINNING_STREAK_5("5연승", "배틀에서 5연승을 달성하세요", "/images/achievements/streak-5.png", "배틀에서 5연속 승리하세요"),
    
    /**
     * 10연승 업적
     */
    WINNING_STREAK_10("10연승", "배틀에서 10연승을 달성하세요", "/images/achievements/streak-10.png", "배틀에서 10연속 승리하세요"),
    
    /**
     * 데일리 퀴즈 마스터 업적
     */
    DAILY_QUIZ_MASTER("데일리 퀴즈 마스터", "7일 연속으로 데일리 퀴즈를 완료하세요", "/images/achievements/daily-master.png", "7일 연속으로 데일리 퀴즈를 완료하세요"),
    
    /**
     * 빠른 해결사 업적
     */
    QUICK_SOLVER("빠른 해결사", "30초 이내에 퀴즈를 완료하세요", "/images/achievements/quick-solver.png", "30초 이내에 퀴즈를 완료하세요"),
    
    /**
     * 지식 탐구자 업적
     */
    KNOWLEDGE_SEEKER("지식 탐구자", "3가지 다른 주제의 퀴즈를 완료하세요", "/images/achievements/seeker.png", "3가지 다른 주제의 퀴즈를 완료하세요");

    /**
     * 업적 이름
     */
    private final String name;
    
    /**
     * 업적 설명
     */
    private final String description;
    
    /**
     * 업적 아이콘 URL
     */
    private final String iconUrl;
    
    /**
     * 업적 달성 조건 설명
     */
    private final String requirementDescription;

    /**
     * 업적 생성자
     * 
     * @param name 업적 이름
     * @param description 업적 설명
     * @param iconUrl 업적 아이콘 URL
     * @param requirementDescription 업적 달성 조건 설명
     */
    Achievement(String name, String description, String iconUrl, String requirementDescription) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.requirementDescription = requirementDescription;
    }
}