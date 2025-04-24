package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleReadyResponse {
    private Long roomId;
    private List<BattleParticipant> participants;
    private boolean allReady;
} 