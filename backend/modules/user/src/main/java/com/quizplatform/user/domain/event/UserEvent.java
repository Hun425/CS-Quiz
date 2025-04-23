package com.quizplatform.user.domain.event;

import com.quizplatform.common.event.DomainEvent;

/**
 * 사용자 도메인 이벤트 인터페이스
 * 
 * <p>사용자 도메인에서 발생하는 이벤트를 나타내는 인터페이스입니다.
 * 모든 사용자 관련 이벤트는 이 인터페이스를 구현해야 합니다.</p>
 */
public interface UserEvent extends DomainEvent {
    /**
     * 이벤트 타입 반환
     * @return 이벤트 타입 문자열
     */
    String getEventType();
} 