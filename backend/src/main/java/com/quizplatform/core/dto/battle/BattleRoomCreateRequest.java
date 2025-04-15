package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BattleRoomCreateRequest {
    private Long quizId;
    private Integer maxParticipants;
    private Long creatorId;
}