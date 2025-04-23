package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

// 다음 문제 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleNextQuestionResponse {
    private Long questionId;
    private String questionText;
    private List<String> options;
    private int timeLimitSeconds;
    private int questionIndex;
    private boolean isLastQuestion;
}