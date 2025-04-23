package com.quizplatform.user.domain.event;

import com.quizplatform.user.domain.model.User;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 사용자 생성 이벤트
 * 
 * <p>새로운 사용자가 생성되었을 때 발생하는 이벤트입니다.
 * 다른 모듈에서 새 사용자 정보를 필요로 할 때 사용됩니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Getter
public class UserCreatedEvent implements UserEvent {
    private final String eventId;
    private final long timestamp;
    private final Long userId;
    private final String username;
    private final String email;
    
    /**
     * 사용자 생성 이벤트 생성자
     * 
     * @param user 생성된 사용자 엔티티
     */
    public UserCreatedEvent(User user) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "USER_CREATED";
    }
} 