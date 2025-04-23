package com.quizplatform.core.service.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트를 발행하는 역할을 담당하는 컴포넌트입니다.
 * Spring의 ApplicationEventPublisher를 사용하여 시스템 전체에 이벤트를 전파합니다.
 * ApplicationEventPublisherAware 인터페이스를 구현하여 ApplicationContext로부터
 * ApplicationEventPublisher 인스턴스를 주입받아 정적 필드에 저장하고,
 * 정적 메서드 publishEvent를 통해 어디서든 이벤트를 발행할 수 있도록 지원합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Component
public class DomainEventPublisher implements ApplicationEventPublisherAware {

    private static ApplicationEventPublisher publisher;

    /**
     * Spring ApplicationContext가 ApplicationEventPublisher 인스턴스를 주입할 때 호출됩니다.
     * 주입된 publisher를 정적 필드에 저장하여 publishEvent 메서드에서 사용할 수 있도록 합니다.
     *
     * @param applicationEventPublisher 주입될 ApplicationEventPublisher 인스턴스
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        DomainEventPublisher.publisher = applicationEventPublisher;
    }

    /**
     * 주어진 도메인 이벤트를 발행합니다.
     * 내부에 저장된 ApplicationEventPublisher를 사용하여 이벤트를 시스템에 전파합니다.
     * publisher가 초기화되지 않은 상태에서 호출되면 IllegalStateException을 발생시킵니다.
     *
     * @param event 발행할 ApplicationEvent 객체
     * @throws IllegalStateException ApplicationEventPublisher가 초기화되지 않았을 경우 발생
     */
    public static void publishEvent(ApplicationEvent event) {
        if (publisher == null) {
            // publisher가 초기화되지 않았다는 것은 Spring 컨텍스트가 제대로 로드되지 않았거나
            // 이 컴포넌트가 Bean으로 등록되지 않았음을 의미할 수 있습니다.
            throw new IllegalStateException("ApplicationEventPublisher is not initialized. Ensure DomainEventPublisher is managed by Spring.");
        }
        publisher.publishEvent(event);
    }
}