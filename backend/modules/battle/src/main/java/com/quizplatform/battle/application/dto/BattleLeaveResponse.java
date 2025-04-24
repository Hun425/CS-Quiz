package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배틀방 나가기 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleLeaveResponse {
    
    /**
     * 배틀방 ID
     */
    private Long roomId;
    
    /**
     * 나간 사용자 ID
     */
    private Long userId;
    
    /**
     * 배틀방이 닫혔는지 여부
     */
    private boolean roomClosed;
    
    /**
     * 남은 참가자 수
     */
    private int remainingParticipants;
    
    /**
     * 3-parameter 생성자 (하위 호환성 유지)
     */
    public BattleLeaveResponse(Long roomId, Long userId, boolean roomClosed) {
        this.roomId = roomId;
        this.userId = userId;
        this.roomClosed = roomClosed;
        this.remainingParticipants = 0; // 기본값
    }
} 