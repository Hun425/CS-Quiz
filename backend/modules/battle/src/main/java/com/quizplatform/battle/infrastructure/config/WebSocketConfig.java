package com.quizplatform.battle.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;
    
    @Value("${websocket.allowed-origins}")
    private List<String> allowedOrigins;
    
    @Value("${websocket.disconnect-delay:30000}")
    private int disconnectDelay;

    public WebSocketConfig(StompChannelInterceptor stompChannelInterceptor) {
        this.stompChannelInterceptor = stompChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: 브로드캐스트 메시지를 위한 prefix
        // /queue: 특정 사용자를 위한 메시지 prefix
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");

        // 유저별 구독 prefix 설정
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-battle")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS()
                .setDisconnectDelay(disconnectDelay)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }
} 