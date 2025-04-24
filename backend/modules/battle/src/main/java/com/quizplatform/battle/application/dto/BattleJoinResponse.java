package com.quizplatform.battle.application.dto;

import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleJoinResponse {
    private Long roomId;
    private List<BattleParticipant> participants;
    private BattleRoomStatus status;
    private String categoryName;
    private int maxParticipants;
    private Long creatorId;
} 