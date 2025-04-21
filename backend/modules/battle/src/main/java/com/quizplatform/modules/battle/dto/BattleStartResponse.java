package com.quizplatform.modules.battle.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 대결 시작 응답 DTO
@Getter
@Builder
public class BattleStartResponse {
    private Long roomId;
    private List<ParticipantInfo> participants;
    private int totalQuestions;
    private int timeLimit;
    private LocalDateTime startTime;
    private BattleNextQuestionResponse firstQuestion;

    @Getter
    @Builder
    public static class ParticipantInfo {
        private Long userId;
        private String username;
        private String profileImage;
        private int level;
    }
}
