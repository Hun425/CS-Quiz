package com.quizplatform.common.event;

/**
 * 이벤트 발행 인터페이스
 * 모든 이벤트 발행자는 이 인터페이스를 구현해야 함
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface EventPublisher {
    
    /**
     * 도메인 이벤트를 특정 토픽으로 발행
     * 
     * @param event 발행할 도메인 이벤트
     * @param topic 이벤트를 발행할 토픽 이름
     */
    void publish(DomainEvent event, String topic);
}
