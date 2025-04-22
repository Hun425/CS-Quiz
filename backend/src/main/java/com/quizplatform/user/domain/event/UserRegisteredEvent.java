package com.quizplatform.user.domain.event;

import com.quizplatform.user.domain.model.User;
import lombok.Getter;

import java.util.UUID;

/**
 * 사용자 등록 이벤트
 */
@Getter
public class UserRegisteredEvent extends DomainEvent {
    private final Long userId;
    private final String username;
    private final String email;

    public UserRegisteredEvent(User user) {
        super(UUID.randomUUID().toString(), "USER_REGISTERED");
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
