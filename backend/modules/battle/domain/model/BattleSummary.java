package com.quizplatform.battle.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Battle의 결과 요약을 표현하는 도메인 엔티티
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BattleSummary {
    private final UUID id;
    private final UUID battleId;
    private final UUID winnerId;
    private final UUID loserId;
    private final int winnerScore;
    private final int loserScore;
    private final int challengerScore;
    private final int opponentScore;
    private final long durationInSeconds;
    private final LocalDateTime createdAt;

    /**
     * 새로운 BattleSummary 인스턴스 생성
     * 
     * @param battle 배틀 객체
     * @param winnerId 승자 ID
     * @return 생성된 BattleSummary 인스턴스
     */
    public static BattleSummary create(Battle battle, UUID winnerId) {
        UUID loserId;
        int challengerScore = 0;
        int opponentScore = 0;
        
        // 승자가 도전자인 경우
        if (winnerId.equals(battle.getChallengerId())) {
            loserId = battle.getOpponentId();
        } else {
            loserId = battle.getChallengerId();
        }
        
        // 참가자별 점수 계산
        for (BattleParticipant participant : battle.getParticipants()) {
            if (participant.getUserId().equals(battle.getChallengerId())) {
                challengerScore = participant.getScore();
            } else if (participant.getUserId().equals(battle.getOpponentId())) {
                opponentScore = participant.getScore();
            }
        }
        
        int winnerScore = winnerId.equals(battle.getChallengerId()) ? challengerScore : opponentScore;
        int loserScore = winnerId.equals(battle.getChallengerId()) ? opponentScore : challengerScore;
        
        // 배틀 기간 계산
        long durationInSeconds = 0;
        if (battle.getStartTime() != null && battle.getEndTime() != null) {
            durationInSeconds = java.time.Duration.between(battle.getStartTime(), battle.getEndTime()).getSeconds();
        }
        
        return BattleSummary.builder()
                .id(UUID.randomUUID())
                .battleId(battle.getId())
                .winnerId(winnerId)
                .loserId(loserId)
                .winnerScore(winnerScore)
                .loserScore(loserScore)
                .challengerScore(challengerScore)
                .opponentScore(opponentScore)
                .durationInSeconds(durationInSeconds)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
