package com.quizplatform.quiz.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트의 기본 클래스
 */
@Getter
public abstract class DomainEvent {
    private final String eventId;
    private final String eventType;
    private final LocalDateTime occurredAt;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
    }
}