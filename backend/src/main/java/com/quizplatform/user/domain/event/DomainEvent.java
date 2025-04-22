package com.quizplatform.user.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 도메인 이벤트의 기본 클래스
 */
@Getter
public abstract class DomainEvent {
    private final String eventId;
    private final String eventType;
    private final ZonedDateTime occurredAt;

    protected DomainEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = ZonedDateTime.now();
    }
}
