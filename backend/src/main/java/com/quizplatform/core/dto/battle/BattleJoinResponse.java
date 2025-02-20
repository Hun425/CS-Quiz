package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 대결방 입장 응답 DTO
@Getter
@Builder
public class BattleJoinResponse {
    private UUID roomId;
    private UUID userId;
    private String username;
    private int currentParticipants;
    private int maxParticipants;
    private List<ParticipantInfo> participants;
    private LocalDateTime joinedAt;

    @Getter
    @Builder
    public static class ParticipantInfo {
        private UUID userId;
        private String username;
        private String profileImage;
        private int level;
        private boolean isReady;
    }
}