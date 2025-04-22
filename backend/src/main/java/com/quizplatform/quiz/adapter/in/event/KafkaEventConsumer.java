package com.quizplatform.quiz.adapter.in.event;

import com.quizplatform.quiz.application.service.QuizAttemptService;
import com.quizplatform.quiz.application.service.QuizCommandService;
import com.quizplatform.quiz.application.service.QuizStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 이벤트 수신 어댑터 클래스
 * 다른 모듈에서 발생한 이벤트를 수신하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final QuizCommandService quizCommandService;
    private final QuizAttemptService quizAttemptService;
    private final QuizStatisticsService quizStatisticsService;

    /**
     * 사용자 레벨업 이벤트 수신
     */
    @KafkaListener(topics = "user-level-up", groupId = "quiz-service")
    public void consumeUserLevelUpEvent(Map<String, Object> event) {
        log.info("Received user level up event: {}", event);
        
        try {
            Long userId = Long.valueOf(event.get("userId").toString());
            Integer newLevel = Integer.valueOf(event.get("newLevel").toString());
            
            // 사용자 레벨에 따른 추천 퀴즈 업데이트 등의 작업 수행
            log.info("User ID: {} upgraded to level: {}", userId, newLevel);
            
            // 필요한 비즈니스 로직 호출
        } catch (Exception e) {
            log.error("Error processing user level up event", e);
        }
    }

    /**
     * 사용자 업적 획득 이벤트 수신
     */
    @KafkaListener(topics = "user-achievement-earned", groupId = "quiz-service")
    public void consumeUserAchievementEarnedEvent(Map<String, Object> event) {
        log.info("Received user achievement earned event: {}", event);
        
        try {
            Long userId = Long.valueOf(event.get("userId").toString());
            String achievementCode = event.get("achievementCode").toString();
            
            // 사용자 업적 획득에 따른 특별 퀴즈 활성화 등의 작업 수행
            log.info("User ID: {} earned achievement: {}", userId, achievementCode);
            
            // 필요한 비즈니스 로직 호출
        } catch (Exception e) {
            log.error("Error processing user achievement earned event", e);
        }
    }

    /**
     * 사용자 등록 이벤트 수신
     */
    @KafkaListener(topics = "user-registered", groupId = "quiz-service")
    public void consumeUserRegisteredEvent(Map<String, Object> event) {
        log.info("Received user registered event: {}", event);
        
        try {
            Long userId = Long.valueOf(event.get("userId").toString());
            String username = event.get("username").toString();
            
            // 신규 사용자를 위한 초기 추천 퀴즈 등록 등의 작업 수행
            log.info("New user registered - ID: {}, Username: {}", userId, username);
            
            // 필요한 비즈니스 로직 호출
        } catch (Exception e) {
            log.error("Error processing user registered event", e);
        }
    }
}
