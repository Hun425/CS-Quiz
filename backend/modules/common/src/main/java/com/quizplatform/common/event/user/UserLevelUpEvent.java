package com.quizplatform.common.event.user;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 사용자 레벨업 이벤트
 */
@Getter
@ToString
@AllArgsConstructor
public class UserLevelUpEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String userId;
    private final int oldLevel;
    private final int newLevel;

    public UserLevelUpEvent(String userId, int oldLevel, int newLevel) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }
}
