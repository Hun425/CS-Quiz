package com.quizplatform.user.domain.service;

import com.quizplatform.common.event.EventPublisher;
import com.quizplatform.user.domain.event.UserCreatedEvent;
import com.quizplatform.user.domain.event.UserEvent;
import com.quizplatform.user.domain.event.UserLevelUpEvent;
import com.quizplatform.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 사용자 이벤트 발행 서비스
 * 
 * <p>사용자 도메인에서 발생하는 이벤트를 발행하는 서비스입니다.
 * 다른 모듈과의 통신을 위해 Kafka 토픽으로 이벤트를 발행합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {
    
    private final EventPublisher eventPublisher;
    
    // Kafka 토픽 상수
    private static final String USER_CREATED_TOPIC = "user.created";
    private static final String USER_LEVEL_UP_TOPIC = "user.level-up";
    
    /**
     * 사용자 생성 이벤트 발행
     * 
     * @param user 생성된 사용자
     */
    public void publishUserCreated(User user) {
        UserCreatedEvent event = new UserCreatedEvent(user);
        publishEvent(event, USER_CREATED_TOPIC);
        log.info("User created event published for user: {}", user.getId());
    }
    
    /**
     * 사용자 레벨업 이벤트 발행
     * 
     * @param user 레벨업한 사용자
     * @param oldLevel 이전 레벨
     */
    public void publishUserLevelUp(User user, int oldLevel) {
        UserLevelUpEvent event = new UserLevelUpEvent(user, oldLevel);
        publishEvent(event, USER_LEVEL_UP_TOPIC);
        log.info("User level-up event published for user: {}, old level: {}, new level: {}", 
                user.getId(), oldLevel, user.getLevel());
    }
    
    /**
     * 이벤트 발행 공통 메소드
     * 
     * @param event 발행할 이벤트
     * @param topic Kafka 토픽
     */
    private void publishEvent(UserEvent event, String topic) {
        eventPublisher.publish(event, topic);
    }
} 