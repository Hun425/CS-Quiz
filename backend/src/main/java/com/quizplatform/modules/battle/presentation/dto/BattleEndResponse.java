package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 대결 종료 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleEndResponse {
    private Long roomId;
    private List<ParticipantResult> results;
    private int totalQuestions;
    private int timeTakenSeconds;
    private LocalDateTime endTime;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResult {
        private Long userId;
        private int finalScore;
        private int correctAnswers;
        private int averageTimeSeconds;
        private boolean isWinner;
        private Map<Long, Boolean> questionResults;
    }
}