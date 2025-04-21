package com.quizplatform.modules.battle.domain;

import lombok.Getter;

/**
 * 배틀룸 상태 열거형 클래스
 * 
 * <p>배틀 방의 현재 상태를 나타내는 열거형입니다.
 * 각 상태는 화면에 표시될 상태명과 설명을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public enum BattleRoomStatus {
    /**
     * 대기 중 상태 - 참가자 참여 대기
     */
    WAITING("대기 중", "참가자를 기다리는 중입니다."),
    
    /**
     * 준비 완료 상태 - 참가자 모두 준비됨
     */
    READY("준비 완료", "잠시 후 시작합니다."),
    
    /**
     * 진행 중 상태 - 배틀 진행 중
     */
    IN_PROGRESS("진행 중", "퀴즈가 진행 중입니다."),
    
    /**
     * 종료 상태 - 배틀 완료
     */
    FINISHED("종료됨", "퀴즈가 종료되었습니다.");

    /**
     * 상태 표시명
     */
    private final String status;
    
    /**
     * 상태 설명
     */
    private final String description;

    /**
     * 배틀룸 상태 생성자
     * 
     * @param status 상태 표시명
     * @param description 상태 설명
     */
    BattleRoomStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }
}