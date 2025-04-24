package com.quizplatform.battle.domain.model;

/**
 * 배틀 상태 열거형
 */
public enum BattleStatus {
    /**
     * 대기 중: 배틀이 생성되었지만 아직 시작되지 않은 상태
     */
    WAITING,
    
    /**
     * 진행 중: 배틀이 시작되어 진행 중인 상태
     */
    IN_PROGRESS,
    
    /**
     * 완료: 배틀이 종료된 상태
     */
    COMPLETED
} 