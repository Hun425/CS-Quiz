package com.quizplatform.core.domain.quiz;

// 업적 시스템
public enum Achievement {
    FIRST_QUIZ_COMPLETED("첫 퀴즈 완료"),
    PERFECT_SCORE("만점 달성"),
    WINNING_STREAK_3("3연승 달성"),
    WINNING_STREAK_5("5연승 달성"),
    WINNING_STREAK_10("10연승 달성"),
    DAILY_QUIZ_MASTER("데일리 퀴즈 마스터"),
    QUICK_SOLVER("퀴즈 스피드스타"),
    KNOWLEDGE_SEEKER("지식 탐구가");

    private final String description;

    Achievement(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
