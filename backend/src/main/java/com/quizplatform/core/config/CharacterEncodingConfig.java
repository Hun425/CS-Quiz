package com.quizplatform.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

/**
 * 문자 인코딩 구성 클래스
 * 
 * <p>애플리케이션 전체에서 사용할 문자 인코딩(UTF-8)을 설정합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
public class CharacterEncodingConfig {

    /**
     * UTF-8 인코딩 필터를 구성합니다.
     * 모든 HTTP 요청과 응답에 UTF-8 인코딩을 강제 적용합니다.
     * 
     * @return CharacterEncodingFilter 객체
     */
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding(StandardCharsets.UTF_8.name());
        filter.setForceEncoding(true); // 요청과 응답 모두에 인코딩 적용 강제
        return filter;
    }

    /**
     * UTF-8 인코딩을 사용하는 StringHttpMessageConverter를 구성합니다.
     * 
     * @return StringHttpMessageConverter 객체
     */
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}