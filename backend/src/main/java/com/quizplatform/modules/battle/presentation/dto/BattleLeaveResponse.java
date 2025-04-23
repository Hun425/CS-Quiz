package com.quizplatform.modules.battle.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleLeaveResponse {
    private Long userId;
    private Long roomId;
    private String status;
}