package com.quizplatform.core.config;

import com.quizplatform.core.config.security.jwt.JwtAuthenticationFilter;
import com.quizplatform.core.config.security.oauth.CustomOAuth2UserService;
import com.quizplatform.core.config.security.oauth.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Spring Security 설정 클래스
 * 
 * <p>애플리케이션의 보안 설정을 담당하며, JWT 인증, OAuth2 로그인,
 * CORS 설정, 권한 기반 접근 제어 등을 구성합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * 인증이 필요 없는 경로(화이트 리스트) 목록
     */
    private static final String[] WHITE_LIST = {
            "/",
            "/api/v1/auth/**",
            "/api/test-auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/swagger-resources/**",
            "/api/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/v3/api-docs/**",
            "/api-docs/**",
            "/api/api-docs/**",
            "/api/v1/**",
            "/api/oauth2/**",
            "/h2-console/**",
            "/webjars/**",
            "/api/webjars/**",
            "/api/quizzes/**",
            "/oauth2/**",
            "/ws-battle/**",  // WebSocket 엔드포인트
            "/topic/**",      // STOMP 구독 경로
            "/app/**"         // STOMP 메시지 발행 경로
    };

    /**
     * 보안 필터 체인 구성
     * 
     * <p>애플리케이션의 HTTP 요청에 대한 보안 설정을 정의합니다.</p>
     * 
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 구성 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 헤더 설정 - 캐시 컨트롤은 기본 설정 유지
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .xssProtection(xss -> xss.disable())
                        // .cacheControl(cache -> cache.disable()) // 캐시 컨트롤 비활성화 제거
                        // 기본 캐시 제어 헤더 비활성화 추가!
                        .cacheControl(cache -> cache.disable())
                )
                // CSRF 보호 비활성화 (REST API는 상태를 유지하지 않으므로)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                        "http://localhost:5173", 
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com",
                        "http://13.125.187.28"
                    ));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    config.setExposedHeaders(List.of(
                            "Authorization",
                            "X-Cache-Status",
                            "x-cache-status",
                            "X-Cache",
                            "x-cache"
                    ));
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
                        .requestMatchers(WHITE_LIST).permitAll()
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
     * 비밀번호 인코더 빈 등록
     * 
     * @return BCrypt 알고리즘을 사용하는 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}