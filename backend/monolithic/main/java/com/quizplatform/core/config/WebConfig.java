package com.quizplatform.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정 클래스
 * 기본 응답 형식 및 컨텐츠 타입 설정을 담당합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // 기본 응답 형식을 application/json으로 설정
        configurer
            .defaultContentType(MediaType.APPLICATION_JSON)
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(false)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("X-Cache-Status", "x-cache-status", "Content-Type", "content-type")
            .maxAge(3600);
    }
}
