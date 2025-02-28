package com.quizplatform.core.config;

import com.quizplatform.core.config.security.jwt.JwtAuthenticationFilter;

import com.quizplatform.core.config.security.oauth.CustomOAuth2UserService;
import com.quizplatform.core.config.security.oauth.OAuth2SuccessHandler;
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
            "/oauth2/**"
    };



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
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
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}