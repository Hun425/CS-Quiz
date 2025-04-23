package com.quizplatform.modules.battle.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 대결방 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRoomResponse {
    private Long id;
    private String roomCode;
    private Long quizId;
    private String status;
    private int maxParticipants;
    private int currentParticipants;
    private List<ParticipantInfo> participants;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer timeLimitPerQuestion;
    private Long creatorId;

    /**
     * 참가자 정보 DTO (Nested Class)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long userId;
        private boolean isReady;
        private int score;
        private boolean isHost;
        private boolean isDefeated;
    }
}
