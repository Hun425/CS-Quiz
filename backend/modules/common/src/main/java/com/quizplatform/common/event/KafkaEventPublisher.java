package com.quizplatform.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka를 사용한 이벤트 발행 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public void publish(DomainEvent event, String topic) {
        log.info("Publishing event to topic {}: {}", topic, event);
        kafkaTemplate.send(topic, event.getEventId(), event);
    }
}
