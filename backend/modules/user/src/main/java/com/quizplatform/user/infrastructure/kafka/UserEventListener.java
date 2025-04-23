package com.quizplatform.user.infrastructure.kafka;

import com.quizplatform.common.event.quiz.QuizCompletedEvent;
import com.quizplatform.common.event.quiz.UserAchievementEvent;
import com.quizplatform.user.domain.event.UserCreatedEvent;
import com.quizplatform.user.domain.service.UserService;
import com.quizplatform.common.event.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * User 모듈 이벤트 리스너
 * 
 * <p>다른 모듈에서 발행한 이벤트를 수신하여 처리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * 퀴즈 완료 이벤트 처리
     * 
     * <p>사용자가 퀴즈를 완료했을 때 발생하는 이벤트를 수신하여
     * 사용자에게 경험치와 포인트를 부여합니다.</p>
     * 
     * @param event 퀴즈 완료 이벤트
     */
    @KafkaListener(topics = "quiz.completed", groupId = "user-service")
    public void handleQuizCompletedEvent(QuizCompletedEvent event) {
        log.info("Received quiz completed event: {}", event);
        
        try {
            // 사용자 ID를 Long으로 변환
            Long userId = Long.parseLong(event.getUserId());
            
            // 획득한 경험치 만큼 사용자 경험치 증가
            boolean leveledUp = userService.giveExperience(userId, event.getExperienceGained());
            
            // 획득한 포인트 만큼 사용자 포인트 증가
            userService.givePoints(userId, event.getPointsGained());
            
            log.info("User {} gained {} experience and {} points from quiz {}. Level up: {}",
                    userId, event.getExperienceGained(), event.getPointsGained(), 
                    event.getQuizId(), leveledUp);
        } catch (Exception e) {
            log.error("Error processing quiz completed event", e);
        }
    }
    
    /**
     * 사용자 업적 이벤트 처리
     * 
     * <p>사용자가 업적을 달성했을 때 발생하는 이벤트를 수신하여
     * 사용자에게 보너스 경험치와 포인트를 부여합니다.</p>
     * 
     * @param event 사용자 업적 이벤트
     */
    @KafkaListener(topics = "user.achievement", groupId = "user-service")
    public void handleUserAchievementEvent(UserAchievementEvent event) {
        log.info("Received user achievement event: {}", event);
        
        try {
            // 사용자 ID를 Long으로 변환
            Long userId = Long.parseLong(event.getUserId());
            
            // 업적에 따른 보너스 경험치와 포인트 획득
            userService.giveExperience(userId, event.getBonusExperience());
            userService.givePoints(userId, event.getBonusPoints());
            
            log.info("User {} received achievement {} with {} bonus experience and {} bonus points",
                    userId, event.getAchievementType(), 
                    event.getBonusExperience(), event.getBonusPoints());
        } catch (Exception e) {
            log.error("Error processing user achievement event", e);
        }
    }
    
    /**
     * 사용자 생성 이벤트 처리
     * 
     * <p>사용자가 생성되면 해당 이벤트를 다른 모듈로 발행합니다.</p>
     * 
     * @param event 사용자 생성 이벤트
     */
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Publishing user created event: {}", event);
        
        try {
            // 도메인 이벤트를 common 모듈의 이벤트로 변환
            com.quizplatform.common.event.user.UserRegisteredEvent commonEvent = 
                new com.quizplatform.common.event.user.UserRegisteredEvent(
                    event.getUserId().toString(), 
                    event.getUsername(), 
                    event.getEmail()
                );
            
            // Kafka로 이벤트 발행
            kafkaTemplate.send(Topics.USER_REGISTERED, commonEvent.getEventId(), commonEvent);
            
            log.info("User created event published for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error publishing user created event", e);
        }
    }
} 