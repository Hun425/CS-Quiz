package com.quizplatform.core.service.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher implements ApplicationEventPublisherAware {

    private static ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        DomainEventPublisher.publisher = applicationEventPublisher;
    }

    public static void publishEvent(ApplicationEvent event) {
        if (publisher == null) {
            throw new IllegalStateException("ApplicationEventPublisher is not initialized.");
        }
        publisher.publishEvent(event);
    }
}