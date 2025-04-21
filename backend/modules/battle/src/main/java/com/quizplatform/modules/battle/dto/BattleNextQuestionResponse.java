package com.quizplatform.modules.battle.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 다음 문제 응답 DTO
@Getter
@Builder
public class BattleNextQuestionResponse {
    private Long questionId;
    private String questionText;
    private String questionType;
    private List<String> options;
    private int timeLimit;
    private int points;
    private boolean isLastQuestion;
    private boolean isGameOver;
}