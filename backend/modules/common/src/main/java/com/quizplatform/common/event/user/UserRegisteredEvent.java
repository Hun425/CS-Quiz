package com.quizplatform.common.event.user;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 사용자 등록 이벤트
 */
@Getter
@ToString
@AllArgsConstructor
public class UserRegisteredEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(String userId, String username, String email) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
