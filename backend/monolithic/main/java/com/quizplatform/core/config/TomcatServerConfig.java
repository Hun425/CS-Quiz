package com.quizplatform.core.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat 서버 구성 클래스
 * 
 * <p>내장 Tomcat 서버의 동작을 커스터마이징합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
public class TomcatServerConfig {

    /**
     * 내장 Tomcat 서버의 URL 인코딩 관련 설정을 변경합니다.
     * 
     * @return WebServerFactoryCustomizer 객체
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            // URI 인코딩에 대한 원활한 처리를 위해 relaxedQueryChars와 relaxedPathChars 설정을 추가
            factory.addConnectorCustomizers(connector -> {
                connector.setProperty("relaxedQueryChars", "[]|{}^\\`\"<>%\\");
                connector.setProperty("relaxedPathChars", "[]|{}^\\`\"<>%\\");
                
                // HTTP 요청과 헤더의 최대 크기 설정
                connector.setProperty("maxHttpHeaderSize", "65536"); // 64KB
                connector.setProperty("maxPostSize", "10485760"); // 10MB
            });
        };
    }
}