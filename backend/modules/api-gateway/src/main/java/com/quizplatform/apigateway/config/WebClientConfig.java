package com.quizplatform.apigateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 관련 설정 클래스
 * 마이크로서비스 간 통신에 사용되는 WebClient 구성
 * 
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Configuration
public class WebClientConfig {

    // 타임아웃 상수
    private static final int CONNECT_TIMEOUT = 10000; // 10초
    private static final int READ_TIMEOUT = 10000; // 10초
    private static final int WRITE_TIMEOUT = 10000; // 10초
    private static final int RESPONSE_TIMEOUT = 15000; // 15초

    /**
     * 로드 밸런싱을 지원하는 WebClient 빌더 빈 생성
     * 서비스 이름으로 요청을 라우팅할 수 있는 기능 제공
     * 타임아웃 설정 추가로 안정성 향상
     * 
     * @return 로드 밸런싱된 WebClient.Builder 인스턴스
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        // 타임아웃 설정이 적용된 HttpClient 생성
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
                .responseTimeout(Duration.ofMillis(RESPONSE_TIMEOUT))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT, TimeUnit.MILLISECONDS))
                );
        
        // 설정된 HttpClient를 사용하는 WebClient 빌더 반환
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
