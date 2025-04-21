package com.quizplatform.core.event;

import java.time.LocalDateTime;

/**
 * 모든 도메인 이벤트가 구현해야 하는 인터페이스입니다.
 * 도메인 이벤트의 기본 속성과 메서드를 정의합니다.
 *
 * @author Claude
 * @since JDK 17
 */
public interface DomainEvent {
    
    /**
     * 이벤트의 고유 식별자를 반환합니다.
     * 
     * @return 이벤트 식별자
     */
    String getEventId();
    
    /**
     * 이벤트가 발생한 시간을 반환합니다.
     * 
     * @return 이벤트 발생 시간
     */
    LocalDateTime getOccurredOn();
    
    /**
     * 이벤트의 유형을 반환합니다.
     * 
     * @return 이벤트 유형
     */
    String getEventType();
}
