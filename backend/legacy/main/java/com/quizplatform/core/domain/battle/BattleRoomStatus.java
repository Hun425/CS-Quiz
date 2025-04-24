package com.quizplatform.core.domain.battle;

import lombok.Getter;

// 배틀룸 상태를 나타내는 Enum
@Getter
public enum BattleRoomStatus {
    WAITING("대기 중", "참가자를 기다리는 중입니다."),
    READY("준비 완료", "잠시 후 시작합니다."),
    IN_PROGRESS("진행 중", "퀴즈가 진행 중입니다."),
    FINISHED("종료됨", "퀴즈가 종료되었습니다.");

    private final String status;
    private final String description;

    BattleRoomStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }
}
