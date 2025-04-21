package com.quizplatform.apigateway.config;

import com.quizplatform.core.client.ModuleClient;
import com.quizplatform.core.client.RestTemplateModuleClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 모듈 클라이언트 설정 클래스
 * <p>
 * 각 모듈에 대한 클라이언트 빈을 설정합니다.
 * </p>
 */
@Configuration
public class ModuleClientConfig {

    @Value("${module.services.user}")
    private String userServiceUrl;

    @Value("${module.services.quiz}")
    private String quizServiceUrl;

    @Value("${module.services.tag}")
    private String tagServiceUrl;

    @Value("${module.services.battle}")
    private String battleServiceUrl;

    @Value("${module.services.search}")
    private String searchServiceUrl;

    /**
     * RestTemplate 빈 설정
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 사용자 모듈 클라이언트 빈 설정
     *
     * @param restTemplate RestTemplate 인스턴스
     * @return 사용자 모듈 클라이언트
     */
    @Bean
    public ModuleClient userModuleClient(RestTemplate restTemplate) {
        return new RestTemplateModuleClient(restTemplate, userServiceUrl);
    }

    /**
     * 퀴즈 모듈 클라이언트 빈 설정
     *
     * @param restTemplate RestTemplate 인스턴스
     * @return 퀴즈 모듈 클라이언트
     */
    @Bean
    public ModuleClient quizModuleClient(RestTemplate restTemplate) {
        return new RestTemplateModuleClient(restTemplate, quizServiceUrl);
    }

    /**
     * 태그 모듈 클라이언트 빈 설정
     *
     * @param restTemplate RestTemplate 인스턴스
     * @return 태그 모듈 클라이언트
     */
    @Bean
    public ModuleClient tagModuleClient(RestTemplate restTemplate) {
        return new RestTemplateModuleClient(restTemplate, tagServiceUrl);
    }

    /**
     * 배틀 모듈 클라이언트 빈 설정
     *
     * @param restTemplate RestTemplate 인스턴스
     * @return 배틀 모듈 클라이언트
     */
    @Bean
    public ModuleClient battleModuleClient(RestTemplate restTemplate) {
        return new RestTemplateModuleClient(restTemplate, battleServiceUrl);
    }

    /**
     * 검색 모듈 클라이언트 빈 설정
     *
     * @param restTemplate RestTemplate 인스턴스
     * @return 검색 모듈 클라이언트
     */
    @Bean
    public ModuleClient searchModuleClient(RestTemplate restTemplate) {
        return new RestTemplateModuleClient(restTemplate, searchServiceUrl);
    }
}