package com.quizplatform.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.lang.NonNull; // For indicating non-null parameters/returns if needed
import java.util.function.Consumer;

/**
 * OAuth2 인증 요청 시 사용될 기본 URL(Base URL) 관련 설정을 위한 구성 클래스입니다.
 * 특히 애플리케이션이 리버스 프록시 또는 로드 밸런서 뒤에서 실행될 때,
 * OAuth2 제공자에게 올바른 리다이렉션 URI(redirect_uri)를 전달하기 위해 사용됩니다.
 * application.yml 등의 설정 파일에서 `app.base-url` 값을 읽어와 적용합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
public class BaseUrlConfiguration {

    /**
     * 애플리케이션의 기본 URL. 리버스 프록시 등의 환경에서 외부에서 접근 가능한 주소입니다.
     * (예: https://mydomain.com)
     * application.yml 파일의 `app.base-url` 속성에서 값을 주입받습니다. 설정되지 않은 경우 null입니다.
     */
    @Value("${app.base-url:#{null}}")
    private String baseUrl;

    /**
     * OAuth2 인증 요청 리졸버(OAuth2AuthorizationRequestResolver) 빈을 생성하고 구성합니다.
     * 기본 리졸버를 사용하되, `baseUrl` 속성이 설정되어 있는 경우
     * {@link #customizeAuthorizationRequest()}를 통해 생성된 커스터마이저를 적용하여
     * 인증 요청 시 redirect_uri 파라미터에 `baseUrl`이 포함되도록 수정합니다.
     *
     * @param clientRegistrationRepository 클라이언트 등록 정보를 관리하는 리포지토리
     * @return 구성된 OAuth2AuthorizationRequestResolver 빈 객체
     */
    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver(
            @NonNull ClientRegistrationRepository clientRegistrationRepository) {

        // 기본 OAuth2 인증 요청 리졸버 생성 (기본 인증 엔드포인트 경로: /api/oauth2/authorize)
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/api/oauth2/authorize");

        // baseUrl이 설정되어 있고 비어있지 않은 경우에만 커스터마이저 적용
        if (baseUrl != null && !baseUrl.isEmpty()) {

            resolver.setAuthorizationRequestCustomizer(customizeAuthorizationRequest());
        } else {

        }

        return resolver;
    }

    /**
     * OAuth2 인증 요청 빌더를 커스터마이징하는 Consumer를 생성하여 반환합니다. (내부 헬퍼 메서드)
     * 이 Consumer는 인증 요청 객체 생성 과정에서 호출되어, redirectUri에 `baseUrl`을
     * 올바르게 접두사로 추가하는 역할을 합니다. baseUrl과 기존 redirectUri 사이의 슬래시(/)를
     * 적절히 처리합니다.
     *
     * @return OAuth2AuthorizationRequest.Builder를 수정하는 Consumer 함수 객체
     */
    private Consumer<OAuth2AuthorizationRequest.Builder> customizeAuthorizationRequest() {
        return builder -> {
            // 빌더로부터 현재 설정된 리다이렉션 URI 가져오기
            String originalRedirectUri = builder.build().getRedirectUri();

            // 리다이렉션 URI가 존재하고, 아직 baseUrl로 시작하지 않는 경우 수정
            if (originalRedirectUri != null && !originalRedirectUri.startsWith(baseUrl)) {
                String newRedirectUri = baseUrl;
                // 슬래시 처리: baseUrl 끝과 originalRedirectUri 시작에 슬래시가 없으면 추가, 둘 다 있으면 하나 제거
                if (!originalRedirectUri.startsWith("/") && !baseUrl.endsWith("/")) {
                    newRedirectUri += "/"; // 예: http://base + path -> http://base/path
                } else if (originalRedirectUri.startsWith("/") && baseUrl.endsWith("/")) {
                    newRedirectUri = baseUrl.substring(0, baseUrl.length() - 1); // 예: http://base/ + /path -> http://base/path
                }
                newRedirectUri += originalRedirectUri;


                // 수정된 리다이렉션 URI를 빌더에 다시 설정
                builder.redirectUri(newRedirectUri);
            } else if (originalRedirectUri != null) {

            }
        };
    }
}