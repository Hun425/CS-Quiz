package com.quizplatform.apigateway.security;

import com.quizplatform.common.security.BaseWebFluxSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig extends BaseWebFluxSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Bean
    @Override
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> {
                    // 공통으로 허용하는 경로 설정
                    exchanges
                            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            // API Gateway 특화 권한 설정
                            .pathMatchers("/api/auth/**").permitAll()
                            .pathMatchers("/api/users/public/**").permitAll()
                            .pathMatchers("/api/battles/public/**").permitAll()
                            .pathMatchers("/api/quizzes/public/**").permitAll()
                            
                            // Swagger UI 관련 경로 모두 명시적으로 허용
                            .pathMatchers("/swagger-ui.html").permitAll()
                            .pathMatchers("/swagger-ui/**").permitAll()
                            .pathMatchers("/v3/api-docs/**").permitAll()
                            .pathMatchers("/swagger-resources/**").permitAll()
                            .pathMatchers("/api-docs/**").permitAll()
                            .pathMatchers("/webjars/**").permitAll()
                            .pathMatchers("/users/v3/api-docs/**").permitAll()
                            .pathMatchers("/battles/v3/api-docs/**").permitAll()
                            .pathMatchers("/quizzes/v3/api-docs/**").permitAll()
                            
                            // Health 체크 및 Actuator 엔드포인트
                            .pathMatchers("/actuator/**").permitAll()
                            // 그 외 모든 API 요청은 인증 필요
                            .anyExchange().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(authenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
                .build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(jwtTokenProvider);
    }

    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return logoutSuccessHandler;
    }

    @Override
    protected void configureAdditionalAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        // 이미 상단에서 모든 권한 설정을 완료했으므로 비워둡니다.
    }
}