package com.quizplatform.common.event.battle;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 배틀 완료 이벤트
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Getter
@ToString
@AllArgsConstructor
public class BattleCompletedEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String battleId;
    private final String winnerId;

    public BattleCompletedEvent(String battleId, String winnerId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.battleId = battleId;
        this.winnerId = winnerId;
    }
}
