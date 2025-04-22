package com.quizplatform.battle.adapter.in.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka를 통해 외부 모듈의 이벤트를 구독하는 어댑터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    // 필요한 서비스와 포트 추가
    
    /**
     * User 모듈의 사용자 레벨업 이벤트 수신
     * 
     * @param event 사용자 레벨업 이벤트
     */
    @KafkaListener(topics = "user-level-up", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserLevelUpEvent(UserLevelUpEvent event) {
        log.info("Received user level up event: {}", event);
        
        // 사용자 레벨업에 따른 추가 배틀 관련 보상 또는 처리
        // 예: 특정 레벨 도달 시 배틀 보상 증가
    }

    /**
     * User 모듈의 사용자 업적 획득 이벤트 수신
     * 
     * @param event 사용자 업적 획득 이벤트
     */
    @KafkaListener(topics = "user-achievement-earned", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserAchievementEarnedEvent(UserAchievementEarnedEvent event) {
        log.info("Received user achievement earned event: {}", event);
        
        // 사용자 업적 획득에 따른 배틀 관련 보상 또는 처리
        // 예: 특정 업적 달성 시 배틀 특전 제공
    }

    /**
     * 사용자 레벨업 이벤트 DTO
     */
    public static class UserLevelUpEvent {
        private String userId;
        private int newLevel;
        private int oldLevel;
        private long timestamp;
        
        // getter, setter, toString 메서드
        
        @Override
        public String toString() {
            return "UserLevelUpEvent{" +
                    "userId='" + userId + '\'' +
                    ", newLevel=" + newLevel +
                    ", oldLevel=" + oldLevel +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * 사용자 업적 획득 이벤트 DTO
     */
    public static class UserAchievementEarnedEvent {
        private String userId;
        private String achievementId;
        private String achievementName;
        private long timestamp;
        
        // getter, setter, toString 메서드
        
        @Override
        public String toString() {
            return "UserAchievementEarnedEvent{" +
                    "userId='" + userId + '\'' +
                    ", achievementId='" + achievementId + '\'' +
                    ", achievementName='" + achievementName + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
