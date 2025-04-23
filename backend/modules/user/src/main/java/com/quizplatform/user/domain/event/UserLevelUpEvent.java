package com.quizplatform.user.domain.event;

import com.quizplatform.user.domain.model.User;
import lombok.Getter;

import java.util.UUID;

/**
 * 사용자 레벨업 이벤트
 * 
 * <p>사용자가 레벨업 했을 때 발생하는 이벤트입니다.
 * 다른 모듈에서 사용자 레벨업 정보를 필요로 할 때 사용됩니다.</p>
 */
@Getter
public class UserLevelUpEvent implements UserEvent {
    private final String eventId;
    private final long timestamp;
    private final Long userId;
    private final String username;
    private final int oldLevel;
    private final int newLevel;
    
    /**
     * 사용자 레벨업 이벤트 생성자
     * 
     * @param user 레벨업한 사용자 엔티티
     * @param oldLevel 이전 레벨
     */
    public UserLevelUpEvent(User user, int oldLevel) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = user.getId();
        this.username = user.getUsername();
        this.oldLevel = oldLevel;
        this.newLevel = user.getLevel();
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
        return "USER_LEVEL_UP";
    }
} 