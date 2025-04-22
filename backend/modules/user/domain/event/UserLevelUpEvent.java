package com.quizplatform.user.domain.event;

import com.quizplatform.user.domain.model.User;
import lombok.Getter;

import java.util.UUID;

/**
 * 사용자 레벨업 이벤트
 */
@Getter
public class UserLevelUpEvent extends DomainEvent {
    private final Long userId;
    private final String username;
    private final int previousLevel;
    private final int newLevel;

    public UserLevelUpEvent(User user, int previousLevel, int newLevel) {
        super(UUID.randomUUID().toString(), "USER_LEVEL_UP");
        this.userId = user.getId();
        this.username = user.getUsername();
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }
}
