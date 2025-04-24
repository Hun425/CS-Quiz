package com.quizplatform.core.dto.battle;

import com.quizplatform.core.domain.battle.BattleRoomStatus;
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