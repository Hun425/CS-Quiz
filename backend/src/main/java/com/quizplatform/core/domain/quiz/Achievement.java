package com.quizplatform.core.domain.quiz;

import lombok.Getter;

// 업적 시스템
@Getter
public enum Achievement {


    FIRST_QUIZ_COMPLETED("첫 퀴즈 완료", "첫 번째 퀴즈를 완료하세요", "/images/achievements/first-quiz.png", "첫 퀴즈를 완료하세요"),
    PERFECT_SCORE("완벽한 점수", "퀴즈에서 만점을 받으세요", "/images/achievements/perfect-score.png", "퀴즈에서 100점을 받으세요"),
    WINNING_STREAK_3("3연승", "배틀에서 3연승을 달성하세요", "/images/achievements/streak-3.png", "배틀에서 3연속 승리하세요"),
    WINNING_STREAK_5("5연승", "배틀에서 5연승을 달성하세요", "/images/achievements/streak-5.png", "배틀에서 5연속 승리하세요"),
    WINNING_STREAK_10("10연승", "배틀에서 10연승을 달성하세요", "/images/achievements/streak-10.png", "배틀에서 10연속 승리하세요"),
    DAILY_QUIZ_MASTER("데일리 퀴즈 마스터", "7일 연속으로 데일리 퀴즈를 완료하세요", "/images/achievements/daily-master.png", "7일 연속으로 데일리 퀴즈를 완료하세요"),
    QUICK_SOLVER("빠른 해결사", "30초 이내에 퀴즈를 완료하세요", "/images/achievements/quick-solver.png", "30초 이내에 퀴즈를 완료하세요"),
    KNOWLEDGE_SEEKER("지식 탐구자", "3가지 다른 주제의 퀴즈를 완료하세요", "/images/achievements/seeker.png", "3가지 다른 주제의 퀴즈를 완료하세요");

    private final String name;
    private final String description;
    private final String iconUrl;
    private final String requirementDescription;

    Achievement(String name, String description, String iconUrl, String requirementDescription) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.requirementDescription = requirementDescription;
    }
}
