package com.quizplatform.apigateway.service;

import com.quizplatform.apigateway.dto.*;
import com.quizplatform.apigateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;
    private final OAuth2ClientService oauth2ClientService;
    
    /**
     * 사용자 로그인
     */
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return userServiceClient.authenticateUser(loginRequest)
                .map(userInfo -> {
                    // JWT 토큰 생성
                    JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
                            userInfo.id(),
                            userInfo.email(),
                            userInfo.roles()
                    );
                    
                    // 응답 생성
                    return LoginResponse.builder()
                            .accessToken(tokenPair.accessToken())
                            .refreshToken(tokenPair.refreshToken())
                            .tokenType("Bearer")
                            .expiresIn(tokenPair.expiresIn())
                            .user(LoginResponse.UserInfo.builder()
                                    .id(userInfo.id())
                                    .email(userInfo.email())
                                    .displayName(userInfo.displayName())
                                    .roles(userInfo.roles())
                                    .build())
                            .build();
                })
                .doOnSuccess(response -> log.info("Login successful for user: {}", loginRequest.email()))
                .doOnError(error -> log.error("Login failed for user: {}", loginRequest.email(), error));
    }
    
    /**
     * 토큰 갱신
     */
    public Mono<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
                    // 리프레시 토큰 검증
                    if (!jwtTokenProvider.validateToken(request.refreshToken())) {
                        throw new IllegalArgumentException("Invalid refresh token");
                    }
                    
                    if (!jwtTokenProvider.isRefreshToken(request.refreshToken())) {
                        throw new IllegalArgumentException("Token is not a refresh token");
                    }
                    
                    // 사용자 ID 추출
                    Long userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken());
                    return userId;
                })
                .flatMap(userServiceClient::getUserById)
                .map(userInfo -> {
                    // 새로운 액세스 토큰 생성
                    String newAccessToken = jwtTokenProvider.generateAccessToken(
                            userInfo.id(),
                            userInfo.email(),
                            userInfo.roles()
                    );
                    
                    return RefreshTokenResponse.builder()
                            .accessToken(newAccessToken)
                            .tokenType("Bearer")
                            .expiresIn(3600L) // 1시간
                            .build();
                })
                .doOnSuccess(response -> log.info("Token refreshed successfully"))
                .doOnError(error -> log.error("Token refresh failed", error));
    }
    
    /**
     * OAuth2 로그인
     */
    public Mono<LoginResponse> oauth2Login(OAuth2CallbackRequest request) {
        log.info("Processing OAuth2 login for provider: {}", request.provider());
        
        return oauth2ClientService.getUserInfo(request.provider(), request.code())
                .flatMap(oauth2UserInfo -> {
                    // User Service에 OAuth2 사용자 정보 전달하여 사용자 조회/생성
                    OAuth2UserRequest userRequest = OAuth2UserRequest.builder()
                            .provider(oauth2UserInfo.provider())
                            .providerId(oauth2UserInfo.providerId())
                            .email(oauth2UserInfo.email())
                            .displayName(oauth2UserInfo.name())
                            .profileImageUrl(oauth2UserInfo.profileImage())
                            .build();
                    
                    return userServiceClient.processOAuth2User(userRequest);
                })
                .map(userInfo -> {
                    // JWT 토큰 생성
                    JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.generateTokenPair(
                            userInfo.id(),
                            userInfo.email(),
                            userInfo.roles()
                    );
                    
                    // 응답 생성
                    return LoginResponse.builder()
                            .accessToken(tokenPair.accessToken())
                            .refreshToken(tokenPair.refreshToken())
                            .tokenType("Bearer")
                            .expiresIn(tokenPair.expiresIn())
                            .user(LoginResponse.UserInfo.builder()
                                    .id(userInfo.id())
                                    .email(userInfo.email())
                                    .displayName(userInfo.displayName())
                                    .roles(userInfo.roles())
                                    .build())
                            .build();
                })
                .doOnSuccess(response -> log.info("OAuth2 login successful for provider: {}", request.provider()))
                .doOnError(error -> log.error("OAuth2 login failed for provider: {}", request.provider(), error));
    }
}