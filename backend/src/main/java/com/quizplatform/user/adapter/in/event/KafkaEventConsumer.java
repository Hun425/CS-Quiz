package com.quizplatform.user.adapter.in.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.quizplatform.user.application.service.UserEventHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 다른 모듈에서 발행된 이벤트를 수신하는 Kafka 컨슈머
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final UserEventHandlerService userEventHandlerService;

    /**
     * 퀴즈 모듈에서 발행한 퀴즈 완료 이벤트 수신
     */
    @KafkaListener(topics = "${app.kafka.topics.quiz-events:quiz-events}", groupId = "${app.kafka.group-id:user-service}")
    public void consumeQuizEvents(@Payload JsonNode event) {
        try {
            String eventType = event.get("eventType").asText();
            log.info("Received quiz event: {}", eventType);

            // 이벤트 타입에 따른 처리
            switch (eventType) {
                case "QUIZ_COMPLETED":
                    handleQuizCompletedEvent(event);
                    break;
                default:
                    log.info("Ignoring unhandled quiz event: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing quiz event", e);
        }
    }

    /**
     * 업적 모듈에서 발행한 업적 이벤트 수신
     */
    @KafkaListener(topics = "${app.kafka.topics.achievement-events:achievement-events}", groupId = "${app.kafka.group-id:user-service}")
    public void consumeAchievementEvents(@Payload JsonNode event) {
        try {
            String eventType = event.get("eventType").asText();
            log.info("Received achievement event: {}", eventType);

            // 이벤트 타입에 따른 처리
            switch (eventType) {
                case "ACHIEVEMENT_UNLOCKED":
                    handleAchievementUnlockedEvent(event);
                    break;
                default:
                    log.info("Ignoring unhandled achievement event: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing achievement event", e);
        }
    }

    /**
     * 퀴즈 완료 이벤트 처리 (경험치 부여, 레벨업 등)
     */
    private void handleQuizCompletedEvent(JsonNode event) {
        Long userId = event.get("userId").asLong();
        int score = event.get("score").asInt();
        int experienceGained = event.get("experienceGained").asInt();
        
        userEventHandlerService.handleQuizCompleted(userId, score, experienceGained);
        log.info("Processed quiz completed event for user {}", userId);
    }

    /**
     * 업적 획득 이벤트 처리 (업적 이력 저장, 포인트 부여 등)
     */
    private void handleAchievementUnlockedEvent(JsonNode event) {
        Long userId = event.get("userId").asLong();
        String achievementId = event.get("achievementId").asText();
        String achievementName = event.get("achievementName").asText();
        int pointsAwarded = event.get("pointsAwarded").asInt();
        
        userEventHandlerService.handleAchievementUnlocked(userId, achievementId, achievementName, pointsAwarded);
        log.info("Processed achievement unlocked event for user {}", userId);
    }
}