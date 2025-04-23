package com.quizplatform.modules.battle.domain.vo;

/**
 * 배틀 방의 상태를 나타내는 열거형 (Value Object)
 *
 * <p>WAITING: 참가자 대기 중</p>
 * <p>IN_PROGRESS: 게임 진행 중</p>
 * <p>ENDED: 게임 종료</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public enum BattleRoomStatus {
    /**
     * 참가자 대기 중인 상태
     */
    WAITING,

    /**
     * 게임이 진행 중인 상태
     */
    IN_PROGRESS,

    /**
     * 게임이 종료된 상태
     */
    ENDED
} 