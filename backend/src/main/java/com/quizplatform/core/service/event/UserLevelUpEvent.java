package com.quizplatform.core.service.event;

import com.quizplatform.modules.user.domain.entity.User;
import com.quizplatform.modules.user.domain.entity.UserLevel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 사용자가 레벨업 했을 때 발생하는 도메인 이벤트 클래스입니다.
 * 이 이벤트는 레벨업한 사용자 정보, 이전 레벨, 새로운 레벨 정보를 포함합니다.
 * Spring의 ApplicationEvent를 상속받아 이벤트 발행 메커니즘을 활용합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public class UserLevelUpEvent extends ApplicationEvent {
    /** 레벨업한 사용자 엔티티 */
    private final User user;
    /** 레벨업 이전의 레벨 */
    private final int oldLevel;
    /** 레벨업 이후의 새로운 레벨 */
    private final int newLevel;

    /**
     * UserLevelUpEvent의 생성자입니다.
     * 레벨업이 발생한 UserLevel 객체와 이전 레벨을 받아 이벤트를 생성합니다.
     * 이벤트의 source로는 UserLevel 객체가 사용됩니다.
     *
     * @param userLevel 레벨업 정보가 포함된 UserLevel 객체 (이벤트의 source)
     * @param oldLevel  레벨업 이전의 레벨
     */
    public UserLevelUpEvent(UserLevel userLevel, int oldLevel) {
        super(userLevel); // ApplicationEvent의 source로 UserLevel 객체를 전달
        this.user = userLevel.getUser(); // UserLevel 객체에서 사용자 정보 추출
        this.oldLevel = oldLevel;
        this.newLevel = userLevel.getLevel(); // UserLevel 객체에서 새로운 레벨 정보 추출
    }
}