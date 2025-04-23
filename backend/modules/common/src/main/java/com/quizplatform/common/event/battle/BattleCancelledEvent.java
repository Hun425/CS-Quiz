package com.quizplatform.common.event.battle;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 배틀 취소 이벤트
 */
@Getter
@ToString
@AllArgsConstructor
public class BattleCancelledEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String battleId;

    public BattleCancelledEvent(String battleId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.battleId = battleId;
    }
}
