package com.quizplatform.core.service.event;

import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserLevelUpEvent extends ApplicationEvent {
    private final User user;
    private final int oldLevel;
    private final int newLevel;

    public UserLevelUpEvent(UserLevel userLevel, int oldLevel) {
        super(userLevel);
        this.user = userLevel.getUser();
        this.oldLevel = oldLevel;
        this.newLevel = userLevel.getLevel();
    }
}
