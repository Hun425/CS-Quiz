package com.quizplatform.battle.domain.model;

/**
 * Battle의 상태를 나타내는 열거형
 */
public enum BattleStatus {
    /**
     * 생성됨 - 배틀이 생성되었지만 아직 시작되지 않음
     */
    CREATED,
    
    /**
     * 진행 중 - 배틀이a 현재 진행 중
     */
    IN_PROGRESS,
    
    /**
     * 완료됨 - 배틀이 정상적으로 완료됨
     */
    COMPLETED,
    
    /**
     * 취소됨 - 배틀이 취소됨
     */
    CANCELLED
}
