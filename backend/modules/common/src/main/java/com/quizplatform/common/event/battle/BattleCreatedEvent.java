package com.quizplatform.common.event.battle;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 배틀 생성 이벤트
 */
@Getter
@ToString
@AllArgsConstructor
public class BattleCreatedEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String battleId;
    private final String hostUserId;
    private final String title;

    public BattleCreatedEvent(String battleId, String hostUserId, String title) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.battleId = battleId;
        this.hostUserId = hostUserId;
        this.title = title;
    }
}
