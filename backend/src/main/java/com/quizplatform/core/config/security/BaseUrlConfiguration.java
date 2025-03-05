package com.quizplatform.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 인증 URL 설정을 위한 구성 클래스
 * 배포 환경에서 baseUrl이 올바르게 설정되도록 합니다.
 */
@Configuration
public class BaseUrlConfiguration {

    @Value("${app.base-url:#{null}}")
    private String baseUrl;

    /**
     * OAuth2 인증 요청 리졸버를 커스터마이징합니다.
     * 설정된 baseUrl 값을 사용하여 인증 요청이 올바른 서버 주소로 전달되도록 합니다.
     */
    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/api/oauth2/authorize");

        // baseUrl이 설정된 경우 인증 요청 URI를 커스터마이징
        if (baseUrl != null && !baseUrl.isEmpty()) {
            resolver.setAuthorizationRequestCustomizer(customizer -> {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder
                        .fromUriString(customizer.getAuthorizationRequestUri());

                // 리디렉션 URI를 baseUrl을 포함하도록 수정
                String redirectUri = customizer.getRedirectUri();
                if (redirectUri != null && !redirectUri.startsWith(baseUrl)) {
                    redirectUri = UriComponentsBuilder
                            .fromUriString(baseUrl)
                            .path(redirectUri.startsWith("/") ? redirectUri : "/" + redirectUri)
                            .build()
                            .toUriString();
                    customizer.redirectUri(redirectUri);
                }
            });
        }

        return resolver;
    }
}