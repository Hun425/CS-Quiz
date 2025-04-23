package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 대결방 입장 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleJoinResponse {
    private Long roomId;
    private Long userId;
    private int currentParticipants;
    private int maxParticipants;
    private List<ParticipantInfo> participants;
    private LocalDateTime joinedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long userId;
        private boolean isReady;
    }
}