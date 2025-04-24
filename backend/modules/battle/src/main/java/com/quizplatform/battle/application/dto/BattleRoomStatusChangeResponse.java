package com.quizplatform.battle.application.dto;

import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleRoomStatusChangeResponse {
    private Long roomId;
    private BattleRoomStatus status;
} 