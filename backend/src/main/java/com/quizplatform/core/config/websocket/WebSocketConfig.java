package com.quizplatform.core.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * WebSocket 통신 설정 클래스
 * 
 * <p>실시간 대결 기능을 위한 WebSocket 설정을 담당합니다.
 * STOMP 프로토콜을 사용한 메시지 브로커 설정, 엔드포인트 등록,
 * 채널 인터셉터 구성 등을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 채널 인터셉터
     */
    private final StompChannelInterceptor stompChannelInterceptor;

    /**
     * 생성자
     * 
     * @param stompChannelInterceptor STOMP 채널 인터셉터
     */
    public WebSocketConfig(StompChannelInterceptor stompChannelInterceptor) {
        this.stompChannelInterceptor = stompChannelInterceptor;
    }

    /**
     * 메시지 브로커 설정
     * 
     * <p>메시지 목적지 접두사와 브로커 설정을 구성합니다.</p>
     * 
     * @param registry 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: 브로드캐스트 메시지를 위한 prefix
        // /queue: 특정 사용자를 위한 메시지 prefix
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {4000,4000})
                .setTaskScheduler(heartbeatScheduler());;

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");

        // 유저별 구독 prefix 설정
        registry.setUserDestinationPrefix("/user");

    }

    /**
     * 하트비트 스케줄러 등록
     *
     * @return ThreadPoolTaskScheduler
     */
    @Bean
    public TaskScheduler heartbeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    /**
     * STOMP 엔드포인트 등록
     * 
     * <p>클라이언트가 WebSocket 연결을 맺을 수 있는 엔드포인트를 등록합니다.</p>
     * 
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-battle")
                .setAllowedOrigins(
                    "http://localhost:5173", 
                    "http://localhost:3000",
                    "http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com",
                    "http://13.125.187.28"
                )
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS()
                .setDisconnectDelay(30 * 1000)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    /**
     * 클라이언트 인바운드 채널 설정
     * 
     * <p>클라이언트로부터 들어오는 메시지 채널에 인터셉터를 추가합니다.</p>
     * 
     * @param registration 채널 등록 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompChannelInterceptor);
    }
}