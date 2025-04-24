package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleJoinRequest {
    private Long roomId;
    private Long userId;
    private Long creatorUserId; // 방 생성자 ID
} 