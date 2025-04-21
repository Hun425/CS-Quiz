package com.quizplatform.modules.battle.dto;

import com.quizplatform.modules.battle.domain.BattleRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BattleLeaveResponse {
    private Long userId;
    private Long roomId;
    private BattleRoomStatus status;
}