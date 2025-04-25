package com.quizplatform.common.event.user;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 사용자 등록 이벤트
 * 
 * <p>새로운 사용자가 등록되었을 때 발생하는 이벤트입니다.
 * User 모듈에서 발행하고, 다른 모듈에서 수신하여 처리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
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

    /**
     * 사용자 등록 이벤트 생성자
     * 
     * @param userId 사용자 ID
     * @param username 사용자 이름
     * @param email 사용자 이메일
     */
    public UserRegisteredEvent(String userId, String username, String email) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
