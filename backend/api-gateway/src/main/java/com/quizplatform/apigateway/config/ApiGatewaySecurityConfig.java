package com.quizplatform.apigateway.config;

import com.quizplatform.core.config.SecurityConfig;
import com.quizplatform.core.security.SecurityConfigurer;
import com.quizplatform.core.security.jwt.JwtAuthenticationFilter;
import com.quizplatform.core.security.oauth.CustomOAuth2UserService;
import com.quizplatform.core.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * API 게이트웨이의 보안 설정 구현체
 * <p>
 * 모든 모듈의 보안 설정을 통합하여 제공합니다.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class ApiGatewaySecurityConfig implements SecurityConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * 보안 필터 체인을 설정합니다.
     *
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 구성 중 발생할 수 있는 예외
     */
    @Override
    @Bean
    public SecurityFilterChain configureFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (REST API는 상태를 유지하지 않으므로)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                        "http://localhost:5173", 
                        "http://localhost:3000", 
                        "http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com",
                        "http://13.125.187.28"
                    ));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    config.setExposedHeaders(List.of("Authorization"));
                    return config;
                }))
                // 세션 관리 설정 (무상태 세션 정책)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 예외 처리 설정
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\",\"code\":\"AUTH_REQUIRED\"}");
                        })
                )
                // HTTP 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(getWhiteList()).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // 기본 HTTP 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 폼 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/callback/*")
                        )
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/authorize")
                        )
                )
                // JWT 인증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 인증이 필요 없는 경로(화이트 리스트) 목록을 반환합니다.
     *
     * @return 화이트 리스트 경로 배열
     */
    @Override
    public String[] getWhiteList() {
        return SecurityConfig.WHITE_LIST;
    }
}