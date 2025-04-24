package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleNextQuestionResponse {
    private Long roomId;
    private Long questionId;
    private String questionText;
    private List<String> options;
    private int questionIndex; // 0부터 시작
    private int totalQuestions;
    private boolean isGameOver;
    private int timeLimit; // 초 단위 (일반적으로 20초)
} 