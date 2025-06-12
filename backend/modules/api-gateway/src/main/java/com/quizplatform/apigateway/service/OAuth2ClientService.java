package com.quizplatform.apigateway.service;

import com.quizplatform.apigateway.dto.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2ClientService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    
    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;
    
    @Value("${oauth2.github.client-id}")
    private String githubClientId;
    
    @Value("${oauth2.github.client-secret}")
    private String githubClientSecret;
    
    @Value("${oauth2.github.redirect-uri}")
    private String githubRedirectUri;
    
    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${oauth2.kakao.client-secret}")
    private String kakaoClientSecret;
    
    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    
    /**
     * Authorization Code를 Access Token으로 교환하고 사용자 정보 조회
     */
    public Mono<OAuth2UserInfo> getUserInfo(String provider, String code) {
        return switch (provider.toLowerCase()) {
            case "google" -> getGoogleUserInfo(code);
            case "github" -> getGithubUserInfo(code);
            case "kakao" -> getKakaoUserInfo(code);
            default -> Mono.error(new IllegalArgumentException("Unsupported OAuth2 provider: " + provider));
        };
    }
    
    /**
     * Google OAuth2 사용자 정보 조회
     */
    private Mono<OAuth2UserInfo> getGoogleUserInfo(String code) {
        log.info("Getting Google user info with code");
        
        // 1. Access Token 교환
        return webClientBuilder.build()
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(String.format(
                    "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
                    googleClientId, googleClientSecret, code, googleRedirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    
                    // 2. 사용자 정보 조회
                    return webClientBuilder.build()
                            .get()
                            .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(Map.class);
                })
                .map(userInfo -> OAuth2UserInfo.builder()
                        .provider("GOOGLE")
                        .providerId((String) userInfo.get("id"))
                        .email((String) userInfo.get("email"))
                        .name((String) userInfo.get("name"))
                        .profileImage((String) userInfo.get("picture"))
                        .build())
                .doOnSuccess(user -> log.info("Google user info retrieved: {}", user.email()))
                .doOnError(error -> log.error("Failed to get Google user info", error));
    }
    
    /**
     * GitHub OAuth2 사용자 정보 조회
     */
    private Mono<OAuth2UserInfo> getGithubUserInfo(String code) {
        log.info("Getting GitHub user info with code");
        
        // 1. Access Token 교환
        return webClientBuilder.build()
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(String.format(
                    "client_id=%s&client_secret=%s&code=%s",
                    githubClientId, githubClientSecret, code
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    
                    // 2. 사용자 정보 조회
                    return webClientBuilder.build()
                            .get()
                            .uri("https://api.github.com/user")
                            .header("Authorization", "Bearer " + accessToken)
                            .header("Accept", "application/vnd.github.v3+json")
                            .retrieve()
                            .bodyToMono(Map.class);
                })
                .map(userInfo -> OAuth2UserInfo.builder()
                        .provider("GITHUB")
                        .providerId(String.valueOf(userInfo.get("id")))
                        .email((String) userInfo.get("email"))
                        .name((String) userInfo.get("name"))
                        .profileImage((String) userInfo.get("avatar_url"))
                        .build())
                .doOnSuccess(user -> log.info("GitHub user info retrieved: {}", user.email()))
                .doOnError(error -> log.error("Failed to get GitHub user info", error));
    }
    
    /**
     * Kakao OAuth2 사용자 정보 조회
     */
    private Mono<OAuth2UserInfo> getKakaoUserInfo(String code) {
        log.info("Getting Kakao user info with code");
        
        // 1. Access Token 교환
        return webClientBuilder.build()
                .post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(String.format(
                    "grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s",
                    kakaoClientId, kakaoClientSecret, kakaoRedirectUri, code
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    
                    // 2. 사용자 정보 조회
                    return webClientBuilder.build()
                            .get()
                            .uri("https://kapi.kakao.com/v2/user/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                            .retrieve()
                            .bodyToMono(Map.class);
                })
                .map(userInfo -> {
                    Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    
                    return OAuth2UserInfo.builder()
                            .provider("KAKAO")
                            .providerId(String.valueOf(userInfo.get("id")))
                            .email((String) kakaoAccount.get("email"))
                            .name((String) profile.get("nickname"))
                            .profileImage((String) profile.get("profile_image_url"))
                            .build();
                })
                .doOnSuccess(user -> log.info("Kakao user info retrieved: {}", user.email()))
                .doOnError(error -> log.error("Failed to get Kakao user info", error));
    }
}