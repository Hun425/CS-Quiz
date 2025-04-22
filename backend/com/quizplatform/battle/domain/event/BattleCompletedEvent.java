package com.quizplatform.battle.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 배틀이 완료되었을 때 발행되는 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class BattleCompletedEvent implements DomainEvent {
    private final UUID battleId;
    private final UUID winnerId;
    private final UUID loserId;
    private final int winnerScore;
    private final int loserScore;
    private final long durationInSeconds;
    private final long timestamp;

    /**
     * 새로운 BattleCompletedEvent 인스턴스 생성
     * 
     * @param battleId 배틀 ID
     * @param winnerId 승자 ID
     * @param loserId 패자 ID
     * @param winnerScore 승자 점수
     * @param loserScore 패자 점수
     * @param durationInSeconds 배틀 진행 시간(초)
     * @return 생성된 이벤트 인스턴스
     */
    public static BattleCompletedEvent of(
            UUID battleId, 
            UUID winnerId, 
            UUID loserId, 
            int winnerScore, 
            int loserScore, 
            long durationInSeconds) {
        return new BattleCompletedEvent(
                battleId, 
                winnerId, 
                loserId, 
                winnerScore, 
                loserScore, 
                durationInSeconds, 
                System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
