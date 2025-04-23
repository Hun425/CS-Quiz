package com.quizplatform.modules.battle.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배틀 문제 시간 초과 응답 DTO
 * 
 * <p>문제 시간 초과 처리 결과를 클라이언트에 전달하는 응답 객체입니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleTimeoutResponse {
    
    /**
     * 배틀룸 ID
     */
    private Long roomId;
    
    /**
     * 시간 초과가 발생한 문제 인덱스
     */
    private Integer questionIndex;
    
    /**
     * 타임아웃 처리된 참가자 수
     */
    private Integer processedCount;
}