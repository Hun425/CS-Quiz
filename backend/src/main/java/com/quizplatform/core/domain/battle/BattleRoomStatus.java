package com.quizplatform.core.domain.battle;

// 실시간 대결방 상태를 관리하는 enum
public enum BattleRoomStatus {
    WAITING,    // 대기 중
    IN_PROGRESS, // 진행 중
    FINISHED    // 종료됨
}
