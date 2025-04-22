package com.quizplatform.battle.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 배틀이 시작되었을 때 발행되는 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class BattleStartedEvent implements DomainEvent {
    private final UUID battleId;
    private final UUID challengerId;
    private final UUID opponentId;
    private final UUID quizId;
    private final long timestamp;

    /**
     * 새로운 BattleStartedEvent 인스턴스 생성
     * 
     * @param battleId 배틀 ID
     * @param challengerId 도전자 ID
     * @param opponentId 상대방 ID
     * @param quizId 퀴즈 ID
     * @return 생성된 이벤트 인스턴스
     */
    public static BattleStartedEvent of(UUID battleId, UUID challengerId, UUID opponentId, UUID quizId) {
        return new BattleStartedEvent(battleId, challengerId, opponentId, quizId, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
