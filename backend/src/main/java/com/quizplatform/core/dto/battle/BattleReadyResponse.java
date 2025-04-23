package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BattleReadyResponse {
    private Long roomId;
    @Builder.Default
    private String type = "PARTICIPANTS";
    private List<ParticipantInfo> participants;

    @Getter
    @Builder
    public static class ParticipantInfo {
        private Long userId;
        private String username;
        private String profileImage;
        private int level;
        private boolean isReady;
    }
}