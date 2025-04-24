package com.quizplatform.battle.application.dto;

import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배틀방 정보를 담는 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRoomResponse {
    
    /**
     * 배틀방 ID
     */
    private Long roomId;
    
    /**
     * 연결된 퀴즈 ID
     */
    private Long quizId;
    
    /**
     * 방 제목 (퀴즈 제목으로 자동 설정)
     */
    private String title;
    
    /**
     * 배틀방 상태
     */
    private BattleRoomStatus status;
    
    /**
     * 최대 참가자 수
     */
    private int maxParticipants;
    
    /**
     * 현재 참가자 수
     */
    private int currentParticipants;
    
    /**
     * 방 생성자 ID
     */
    private Long creatorId;
    
    /**
     * 방 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 총 문제 수
     */
    private int totalQuestions;
    
    /**
     * 문제당 제한 시간 (초)
     */
    private Integer questionTimeLimitSeconds;
    
    /**
     * 현재 참가자 목록
     */
    private List<BattleParticipant> participants;
} 