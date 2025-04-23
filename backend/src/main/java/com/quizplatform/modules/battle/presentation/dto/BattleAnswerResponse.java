package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 답변 제출 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleAnswerResponse {
    private Long questionId;
    private boolean isCorrect;
    private int earnedPoints;
    private int timeBonus;
    private int currentScore;
}