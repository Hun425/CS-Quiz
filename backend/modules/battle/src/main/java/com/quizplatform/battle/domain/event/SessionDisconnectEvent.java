package com.quizplatform.battle.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * WebSocket 세션 연결 해제 이벤트
 * 세션 ID와 연관된 사용자 ID, 방 ID 정보를 담고 있다
 */
@Getter
public class SessionDisconnectEvent extends ApplicationEvent {
    
    private final Long userId;
    private final Long roomId;
    private final String sessionId;
    
    public SessionDisconnectEvent(Object source, Long userId, Long roomId, String sessionId) {
        super(source);
        this.userId = userId;
        this.roomId = roomId;
        this.sessionId = sessionId;
    }
} 