package com.quizplatform.battle.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 요약 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleSummaryResponse {
    
    /**
     * 요약 정보 ID
     */
    private UUID id;
    
    /**
     * 배틀 ID
     */
    private UUID battleId;
    
    /**
     * 승자 ID
     */
    private UUID winnerId;
    
    /**
     * 패자 ID
     */
    private UUID loserId;
    
    /**
     * 승자 점수
     */
    private int winnerScore;
    
    /**
     * 패자 점수
     */
    private int loserScore;
    
    /**
     * 도전자 점수
     */
    private int challengerScore;
    
    /**
     * 상대방 점수
     */
    private int opponentScore;
    
    /**
     * 배틀 진행 시간(초)
     */
    private long durationInSeconds;
    
    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;
}
