package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배틀 강제 진행 응답 DTO
 * 
 * <p>강제 진행 처리 결과를 클라이언트에 전달하는 응답 객체입니다.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleForceNextResponse {
    
    /**
     * 배틀룸 ID
     */
    private Long roomId;
    
    /**
     * 강제 진행 성공 여부
     */
    private boolean success;
}