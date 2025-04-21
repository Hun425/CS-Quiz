package com.quizplatform.modules.battle.dto;

import lombok.Builder;
import lombok.Getter;

// 답변 제출 응답 DTO
@Getter
@Builder
public class BattleAnswerResponse {
    private Long questionId;
    private boolean isCorrect;
    private int earnedPoints;
    private int timeBonus;
    private int currentScore;
    private String correctAnswer;
    private String explanation;
}