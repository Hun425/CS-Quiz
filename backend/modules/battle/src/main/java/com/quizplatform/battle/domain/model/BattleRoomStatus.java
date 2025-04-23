package com.quizplatform.battle.domain.model;

/**
 * 배틀 방 상태 열거형
 * 
 * <p>배틀 방의 생명주기 상태를 정의합니다. 
 * 방 생성부터 완료까지의 모든 상태를 표현합니다.</p>
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public enum BattleRoomStatus {
    /**
     * 대기 상태
     * 참가자를 기다리는 중
     */
    WAITING,
    
    /**
     * 준비 상태
     * 모든 참가자가 준비 완료, 곧 시작 예정
     */
    READY,
    
    /**
     * 진행 중 상태
     * 배틀이 현재 진행 중
     */
    IN_PROGRESS,
    
    /**
     * 일시 정지 상태
     * 배틀이 일시 정지됨
     */
    PAUSED,
    
    /**
     * 종료 상태
     * 배틀이 정상적으로, 혹은 참가자들의 동의로 종료됨
     */
    FINISHED,
    
    /**
     * 취소 상태
     * 참가자 부족 등의 이유로 배틀이 취소됨
     */
    CANCELLED,
    
    /**
     * 만료 상태
     * 시간 초과로 배틀이 만료됨
     */
    EXPIRED,
    
    /**
     * 오류 상태
     * 기술적 문제로 배틀에 오류 발생
     */
    ERROR
} 