package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

// 답변 제출 응답 DTO
@Getter
@Builder
public class BattleAnswerResponse {
    private UUID questionId;
    private boolean isCorrect;
    private int earnedPoints;
    private int timeBonus;
    private int currentScore;
    private String correctAnswer;
    private String explanation;
}