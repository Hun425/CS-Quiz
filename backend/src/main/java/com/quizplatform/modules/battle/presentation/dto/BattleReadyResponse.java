package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleReadyResponse {
    private Long roomId;
    @Builder.Default
    private String type = "PARTICIPANTS";
    private List<ParticipantInfo> participants;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long userId;
        private boolean isReady;
    }
}