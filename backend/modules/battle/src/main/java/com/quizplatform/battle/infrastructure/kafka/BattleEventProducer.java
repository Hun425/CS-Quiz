package com.quizplatform.battle.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.battle.domain.event.BattleCompletedEvent;
import com.quizplatform.battle.domain.event.BattleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 배틀 관련 이벤트를 Kafka 토픽으로 발행하는 프로듀서 클래스
 * 
 * <p>다른 서비스 모듈과의 비동기 통신을 위해 이벤트를 발행합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BattleEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // Kafka 토픽 이름 상수
    private static final String BATTLE_COMPLETED_TOPIC = "battle-completed-events";
    private static final String BATTLE_STARTED_TOPIC = "battle-started-events";
    
    /**
     * 배틀 완료 이벤트를 Kafka 토픽으로 발행합니다.
     * 
     * @param event 발행할 배틀 완료 이벤트
     */
    public void publishBattleCompletedEvent(BattleCompletedEvent event) {
        publishEvent(event, BATTLE_COMPLETED_TOPIC);
    }
    
    /**
     * 이벤트를 JSON으로 직렬화하여 지정된 Kafka 토픽으로 발행합니다.
     * 
     * @param event 발행할 이벤트
     * @param topic 발행할 토픽
     */
    private void publishEvent(BattleEvent event, String topic) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getEventId(), eventJson);
            log.info("이벤트 발행 완료 - 토픽: {}, 이벤트 타입: {}, 이벤트 ID: {}", 
                    topic, event.getEventType(), event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("이벤트 발행 실패: {}", e.getMessage(), e);
        }
    }
} 