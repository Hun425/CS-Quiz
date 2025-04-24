package com.quizplatform.battle.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {
    private Long roomId;
    private List<ParticipantResult> participants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResult {
        private Long userId;
        private String username;
        private int finalScore;
        private int correctAnswers;
        private int rank; // 순위
    }
} 