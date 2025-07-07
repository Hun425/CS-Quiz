package com.quizplatform.core.dto.battle;

import com.quizplatform.core.domain.battle.BattleRoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// 대결 진행 상황 DTO
@Getter
@Builder
public class BattleProgressResponse {
    private Long roomId;
    private int currentQuestionIndex;
    private int totalQuestions;
    private int remainingTimeSeconds;
    private Map<Long, ParticipantProgress> participantProgress;
    private BattleRoomStatus status;

    @Getter
    @Builder
    public static class ParticipantProgress {
        private Long userId;
        private String username;
        private int currentScore;
        private int correctAnswers;
        private boolean hasAnsweredCurrent;
        private int currentStreak;
    }
}
