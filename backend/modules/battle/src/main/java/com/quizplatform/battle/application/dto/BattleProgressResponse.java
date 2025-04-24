package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleProgressResponse {
    private Long roomId;
    private int currentQuestionIndex;
    private int totalQuestions;
    private Map<Long, ParticipantProgress> participantProgress; // userId를 키로 사용

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantProgress {
        private Long userId;
        private String username;
        private int currentScore;
        private int correctAnswers;
        private boolean hasAnsweredCurrent;
    }
} 