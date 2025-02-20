package com.quizplatform.core.dto.battle;

// 대결 상태 열거형
public enum BattleStatus {
    WAITING("대기 중"),
    IN_PROGRESS("진행 중"),
    FINISHED("종료됨");

    private final String description;

    BattleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}