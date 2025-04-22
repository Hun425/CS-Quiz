package com.quizplatform.user.domain.event;

import com.quizplatform.user.domain.model.User;
import lombok.Getter;

import java.util.UUID;

/**
 * 사용자 업적 획득 이벤트
 */
@Getter
public class UserAchievementEarnedEvent extends DomainEvent {
    private final Long userId;
    private final String username;
    private final String achievementId;
    private final String achievementName;
    private final int pointsEarned;

    public UserAchievementEarnedEvent(User user, String achievementId, String achievementName, int pointsEarned) {
        super(UUID.randomUUID().toString(), "USER_ACHIEVEMENT_EARNED");
        this.userId = user.getId();
        this.username = user.getUsername();
        this.achievementId = achievementId;
        this.achievementName = achievementName;
        this.pointsEarned = pointsEarned;
    }
}
