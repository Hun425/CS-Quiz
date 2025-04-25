package com.quizplatform.apigateway.controller;

import com.quizplatform.apigateway.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * 인증 관련 API를 제공하는 컨트롤러
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 현재 인증된 사용자 정보 조회
     */
    @GetMapping("/me")
    @Operation(summary = "현재 인증된 사용자 정보 조회", description = "JWT 토큰으로 인증된 사용자의 정보를 반환합니다.")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(ServerHttpRequest request) {
        // 헤더, 쿠키 등에서 토큰 추출
        String token = resolveToken(request);
        
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            Claims claims = tokenProvider.getClaims(token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", claims.getSubject());
            userInfo.put("name", claims.get("name"));
            userInfo.put("provider", claims.get("provider"));
            userInfo.put("roles", claims.get("roles"));
            
            return Mono.just(ResponseEntity.ok(userInfo));
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    /**
     * 테스트용 JWT 토큰 발급 API
     */
    @GetMapping("/test-token")
    @Operation(summary = "테스트용 JWT 토큰 발급", description = "Swagger UI에서 API 테스트용 JWT 토큰을 발급합니다. 실제 서비스에서는 사용하지 않습니다.")
    public Mono<ResponseEntity<Map<String, String>>> generateTestToken() {
        // 테스트용 사용자 정보 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("name", "테스트 사용자");
        claims.put("provider", "test");
        claims.put("roles", Arrays.asList("ROLE_USER"));
        
        // 테스트 토큰 생성
        String accessToken = tokenProvider.generateTokenFromClaims(claims);
        
        Map<String, String> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("message", "이 토큰을 Swagger UI의 Authorize 버튼을 눌러 입력하세요. 형식: Bearer [token]");
        
        return Mono.just(ResponseEntity.ok(response));
    }
    
    /**
     * 토큰 갱신 API
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    public Mono<ResponseEntity<Map<String, String>>> refreshToken(ServerWebExchange exchange) {
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get("refresh_token");
        
        if (cookies != null && !cookies.isEmpty()) {
            String refreshToken = cookies.get(0).getValue();
            
            if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                String userId = tokenProvider.getUserIdFromToken(refreshToken);
                
                // 간단한 클레임 생성 (실제로는 사용자 서비스에서 상세 정보를 가져와야 함)
                Map<String, Object> claims = new HashMap<>();
                claims.put("sub", userId);
                
                // 새 액세스 토큰 생성
                String newAccessToken = tokenProvider.generateTokenFromClaims(claims);
                
                Map<String, String> response = new HashMap<>();
                response.put("access_token", newAccessToken);
                
                return Mono.just(ResponseEntity.ok(response));
            }
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        // 쿠키 삭제 로직 (HTTP Only 쿠키 삭제는 서버에서 수행해야 함)
        return Mono.just(ResponseEntity.ok().build());
    }
    
    /**
     * 요청에서 JWT 토큰 추출
     */
    private String resolveToken(ServerHttpRequest request) {
        // 1. Authorization 헤더에서 먼저 확인
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        // 2. 쿠키에서 확인
        List<HttpCookie> cookies = request.getCookies().get("access_token");
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).getValue();
        }
        
        return null;
    }
}
