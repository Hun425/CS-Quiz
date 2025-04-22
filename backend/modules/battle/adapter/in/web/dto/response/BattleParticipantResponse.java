package com.quizplatform.battle.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 배틀 참가자 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleParticipantResponse {
    
    /**
     * 참가자 ID
     */
    private UUID id;
    
    /**
     * 사용자 ID
     */
    private UUID userId;
    
    /**
     * 배틀 ID
     */
    private UUID battleId;
    
    /**
     * 점수
     */
    private int score;
    
    /**
     * 정답 수
     */
    private int correctAnswers;
    
    /**
     * 총 문제 수
     */
    private int totalQuestions;
    
    /**
     * 참여 시간
     */
    private LocalDateTime joinTime;
    
    /**
     * 완료 시간
     */
    private LocalDateTime completionTime;
    
    /**
     * 참가자 상태
     */
    private String status;
}
