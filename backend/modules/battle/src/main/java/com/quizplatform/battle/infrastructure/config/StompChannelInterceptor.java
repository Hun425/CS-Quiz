package com.quizplatform.battle.infrastructure.config;

import com.quizplatform.battle.domain.event.SessionDisconnectEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * STOMP 채널 인터셉터 - 클라이언트 연결/해제 처리
 * 이벤트 기반 아키텍처 적용으로 순환 의존성 제거
 */
@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {

    private final ApplicationEventPublisher eventPublisher;

    public StompChannelInterceptor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        log.info("StompChannelInterceptor: 이벤트 발행자 주입 완료");
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);

        // 클라이언트 연결 해제 처리
        if (SimpMessageType.DISCONNECT.equals(accessor.getMessageType())) {
            String sessionId = accessor.getSessionId();
            log.info("WebSocket 연결 해제 감지: sessionId={}", sessionId);

            // 세션에 저장된 사용자 정보 및 방 ID 확인
            Object userId = accessor.getSessionAttributes() != null ?
                    accessor.getSessionAttributes().get("userId") : null;
            Object roomId = accessor.getSessionAttributes() != null ?
                    accessor.getSessionAttributes().get("roomId") : null;

            if (userId != null && roomId != null) {
                Long userIdLong = (Long) userId;
                Long roomIdLong = (Long) roomId;

                log.info("사용자 연결 해제 이벤트 발행: userId={}, roomId={}", userIdLong, roomIdLong);

                try {
                    // 세션 연결 해제 이벤트 발행
                    eventPublisher.publishEvent(new SessionDisconnectEvent(this, userIdLong, roomIdLong, sessionId));
                } catch (Exception e) {
                    log.error("연결 해제 이벤트 발행 중 오류 발생", e);
                }
            }
        }
    }
} 