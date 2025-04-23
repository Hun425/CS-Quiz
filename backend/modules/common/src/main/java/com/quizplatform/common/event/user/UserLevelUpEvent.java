package com.quizplatform.common.event.user;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 사용자 레벨업 이벤트
 * 
 * <p>사용자가 레벨업 했을 때 발생하는 이벤트입니다.
 * User 모듈에서 발행하고, 다른 모듈에서 수신하여 처리합니다.</p>
 */
@Getter
@ToString
@AllArgsConstructor
public class UserLevelUpEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String userId;
    private final String username;
    private final int oldLevel;
    private final int newLevel;
    
    /**
     * 사용자 레벨업 이벤트 생성자
     * 
     * @param userId 사용자 ID
     * @param username 사용자 이름
     * @param oldLevel 이전 레벨
     * @param newLevel 새 레벨
     */
    public UserLevelUpEvent(String userId, String username, int oldLevel, int newLevel) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.username = username;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }
}
