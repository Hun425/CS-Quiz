package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

// 대결 진행 상황 DTO
@Getter
@Builder
public class BattleProgressResponse {
    private UUID roomId;
    private int currentQuestionIndex;
    private int totalQuestions;
    private int remainingTimeSeconds;
    private Map<UUID, ParticipantProgress> participantProgress;
    private BattleStatus status;

    @Getter
    @Builder
    public static class ParticipantProgress {
        private UUID userId;
        private String username;
        private int currentScore;
        private int correctAnswers;
        private boolean hasAnsweredCurrent;
        private int currentStreak;
    }
}
