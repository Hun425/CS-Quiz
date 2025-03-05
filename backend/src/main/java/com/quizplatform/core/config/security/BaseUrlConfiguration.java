package com.quizplatform.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import java.util.function.Consumer;

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
            resolver.setAuthorizationRequestCustomizer(customizeAuthorizationRequest());
        }

        return resolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> customizeAuthorizationRequest() {
        return builder -> {
            // 기존 리디렉션 URI 가져오기
            String redirectUri = builder.build().getRedirectUri();

            // 리디렉션 URI에 baseUrl 포함시키기
            if (redirectUri != null && !redirectUri.startsWith(baseUrl)) {
                String newRedirectUri = baseUrl;
                if (!redirectUri.startsWith("/") && !baseUrl.endsWith("/")) {
                    newRedirectUri += "/";
                } else if (redirectUri.startsWith("/") && baseUrl.endsWith("/")) {
                    newRedirectUri = baseUrl.substring(0, baseUrl.length() - 1);
                }
                newRedirectUri += redirectUri;

                // 새 리디렉션 URI 설정
                builder.redirectUri(newRedirectUri);
            }
        };
    }
}