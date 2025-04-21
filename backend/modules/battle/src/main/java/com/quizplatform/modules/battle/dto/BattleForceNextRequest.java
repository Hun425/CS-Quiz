package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배틀 강제 진행 요청 DTO
 * 
 * <p>다음 문제로 강제 진행을 요청할 때 클라이언트에서 서버로 전송하는 요청 객체입니다.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleForceNextRequest {
    
    /**
     * 배틀룸 ID
     */
    private Long roomId;
    
    /**
     * 요청자 ID (선택사항)
     */
    private Long requesterId;
}