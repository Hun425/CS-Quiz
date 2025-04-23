package com.quizplatform.common.event;

/**
 * 도메인 이벤트 인터페이스
 * 모든 도메인 이벤트는 이 인터페이스를 구현해야 함
 */
public interface DomainEvent {
    /**
     * 이벤트 ID 반환
     * @return 이벤트 고유 ID
     */
    String getEventId();
    
    /**
     * 이벤트 발생 시간 반환
     * @return 이벤트 발생 시간 (milliseconds)
     */
    long getTimestamp();
}
