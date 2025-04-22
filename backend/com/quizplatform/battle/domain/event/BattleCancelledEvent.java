package com.quizplatform.battle.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 배틀이 취소되었을 때 발행되는 도메인 이벤트
 */
@Getter
@RequiredArgsConstructor
public class BattleCancelledEvent implements DomainEvent {
    private final UUID battleId;
    private final UUID challengerId;
    private final UUID opponentId;
    private final long timestamp;

    /**
     * 새로운 BattleCancelledEvent 인스턴스 생성
     * 
     * @param battleId 배틀 ID
     * @param challengerId 도전자 ID
     * @param opponentId 상대방 ID
     * @return 생성된 이벤트 인스턴스
     */
    public static BattleCancelledEvent of(UUID battleId, UUID challengerId, UUID opponentId) {
        return new BattleCancelledEvent(battleId, challengerId, opponentId, System.currentTimeMillis());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
