package com.quizplatform.battle.domain.model;

/**
 * BattleParticipant의 상태를 나타내는 열거형
 */
public enum ParticipantStatus {
    /**
     * 참여함 - 참가자가 배틀에 참여했지만 아직 퀴즈를 시작하지 않음
     */
    JOINED,
    
    /**
     * 진행 중 - 참가자가 배틀을 진행 중
     */
    IN_PROGRESS,
    
    /**
     * 완료됨 - 참가자가 배틀을 완료함
     */
    COMPLETED,
    
    /**
     * 포기함 - 참가자가 배틀을 중도에 포기함
     */
    FORFEITED
}
