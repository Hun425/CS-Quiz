package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleAnswerRequest {
    private Long roomId;
    private Long userId;
    private Long questionId;
    private int answerIndex; // 선택한 답변 인덱스 (0, 1, 2, 3 중 하나)
    private long responseTime; // 응답 시간 (밀리초)
} 