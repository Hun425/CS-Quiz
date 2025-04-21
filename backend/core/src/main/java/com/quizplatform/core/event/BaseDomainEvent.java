package com.quizplatform.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 기본 도메인 이벤트 구현 클래스입니다.
 * Spring의 ApplicationEvent를 확장하고 DomainEvent 인터페이스를 구현합니다.
 * 모든 도메인 이벤트의 공통 기능을 제공합니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Getter
public abstract class BaseDomainEvent extends ApplicationEvent implements DomainEvent {
    
    /** 이벤트 고유 식별자 */
    private final String eventId;
    
    /** 이벤트 발생 시간 */
    private final LocalDateTime occurredOn;
    
    /**
     * 기본 생성자로, 이벤트 소스와 함께 고유 식별자와 발생 시간을 초기화합니다.
     *
     * @param source 이벤트 소스 객체
     */
    protected BaseDomainEvent(Object source) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }
    
    /**
     * 이벤트 유형을 반환합니다.
     * 기본적으로 클래스의 단순 이름을 사용합니다.
     *
     * @return 이벤트 유형 (클래스명)
     */
    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
