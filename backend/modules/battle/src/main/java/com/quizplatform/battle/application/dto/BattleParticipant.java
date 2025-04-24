package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleParticipant {
    private Long userId;
    private String username;
    private boolean ready;
    private boolean isCreator;
} 