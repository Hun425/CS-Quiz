package com.quizplatform.battle.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 배틀 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleResponse {
    
    /**
     * 배틀 ID
     */
    private UUID id;
    
    /**
     * 도전자 ID
     */
    private UUID challengerId;
    
    /**
     * 상대방 ID
     */
    private UUID opponentId;
    
    /**
     * 퀴즈 ID
     */
    private UUID quizId;
    
    /**
     * 제한 시간(초)
     */
    private int timeLimit;
    
    /**
     * 배틀 상태
     */
    private String status;
    
    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 시작 시간
     */
    private LocalDateTime startTime;
    
    /**
     * 종료 시간
     */
    private LocalDateTime endTime;
    
    /**
     * 참가자 목록
     */
    private List<BattleParticipantResponse> participants;
}
