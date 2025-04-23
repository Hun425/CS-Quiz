package com.quizplatform.modules.battle.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배틀 문제 시간 초과 요청 DTO
 * 
 * <p>문제 시간이 종료되었을 때 클라이언트에서 서버로 전송하는 요청 객체입니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleTimeoutRequest {
    
    /**
     * 배틀룸 ID
     */
    private Long roomId;
    
    /**
     * 현재 문제 인덱스
     */
    private Integer questionIndex;
}