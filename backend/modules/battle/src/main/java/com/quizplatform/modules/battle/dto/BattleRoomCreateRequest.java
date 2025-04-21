package com.quizplatform.modules.battle.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BattleRoomCreateRequest {
    private Long quizId;
    private Integer maxParticipants;
    private Long creatorId;
}