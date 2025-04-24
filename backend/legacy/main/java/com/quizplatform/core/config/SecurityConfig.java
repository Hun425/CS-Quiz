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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private static final String[] WHITE_LIST = {
            "/",
            "/api/v1/auth/**",
            "/api/test-auth/**",  // 테스트 인증 엔드포인트 추가
            "/swagger-ui/**",
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
            "/ws-battle/**",  // WebSocket 엔드포인트 추가
            "/topic/**",      // STOMP 구독 경로
            "/app/**"         // STOMP 메시지 발행 경로
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    // 프론트엔드 주소에 맞게 수정
                    config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://ec2-13-125-187-28.ap-northeast-2.compute.amazonaws.com","http://13.125.187.28"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    // 필요한 경우 노출할 헤더 추가 (예: Authorization)
                    config.setExposedHeaders(List.of("Authorization"));
                    return config;
                }))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\",\"code\":\"AUTH_REQUIRED\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/callback/*")  // 리다이렉션 엔드포인트 명시적 설정
                        )
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/authorize")    // 인증 엔드포인트 명시적 설정
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}