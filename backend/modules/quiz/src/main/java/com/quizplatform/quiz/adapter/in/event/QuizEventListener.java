package com.quizplatform.quiz.adapter.in.event;

import com.quizplatform.common.event.user.UserLevelUpEvent;
import com.quizplatform.common.event.user.UserRegisteredEvent;
import com.quizplatform.quiz.domain.service.UserEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Quiz 모듈 이벤트 리스너
 * 
 * <p>다른 모듈에서 발행한 이벤트를 수신하여 처리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizEventListener {

    private final UserEventProcessor userEventProcessor;
    
    /**
     * 사용자 생성 이벤트 처리
     * 
     * <p>새로운 사용자가 등록되었을 때 발생하는 이벤트를 수신하여
     * 퀴즈 모듈에 사용자 정보를 캐싱합니다.</p>
     * 
     * @param event 사용자 등록 이벤트
     */
    @KafkaListener(topics = "user.created", groupId = "quiz-service")
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received user registered event: {}", event);
        
        try {
            userEventProcessor.processUserRegisteredEvent(event);
            log.info("New user information cached in Quiz module: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user registered event", e);
        }
    }
    
    /**
     * 사용자 레벨업 이벤트 처리
     * 
     * <p>사용자가 레벨업 했을 때 발생하는 이벤트를 수신하여
     * 레벨에 맞는 새 퀴즈를 추천하거나 난이도를 조정합니다.</p>
     * 
     * @param event 사용자 레벨업 이벤트
     */
    @KafkaListener(topics = "user.level-up", groupId = "quiz-service")
    public void handleUserLevelUpEvent(UserLevelUpEvent event) {
        log.info("Received user level up event: {}", event);
        
        try {
            userEventProcessor.processUserLevelUpEvent(event);
            log.info("Quiz recommendations adjusted for user {} at level {}", 
                     event.getUserId(), event.getNewLevel());
        } catch (Exception e) {
            log.error("Error processing user level up event", e);
        }
    }
} 