package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleAnswerResponse {
    private Long roomId;
    private Long userId;
    private Long questionId;
    private boolean isCorrect;
    private int correctAnswerIndex;
    private int points;
    private String explanation;
} 