package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 대결 종료 응답 DTO
@Getter
@Builder
public class BattleEndResponse {
    private Long roomId;
    private List<ParticipantResult> results;
    private int totalQuestions;
    private int timeTakenSeconds;
    private LocalDateTime endTime;

    @Getter
    @Builder
    public static class ParticipantResult {
        private Long userId;
        private String username;
        private int finalScore;
        private int correctAnswers;
        private int averageTimeSeconds;
        private int experienceGained;
        private boolean isWinner;
        private Map<Long, Boolean> questionResults;
    }
}