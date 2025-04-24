package com.quizplatform.core.dto.battle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BattleLeaveRequest {
    private Long roomId;
    private Long userId;
}