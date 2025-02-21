package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

// 대결방 입장 요청 DTO
@Getter
@Builder
public class BattleJoinRequest {
    private Long userId;
    private Long roomId;
    private boolean isReady;
}