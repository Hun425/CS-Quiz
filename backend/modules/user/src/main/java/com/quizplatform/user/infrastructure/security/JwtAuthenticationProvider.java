package com.quizplatform.user.infrastructure.security;

import com.quizplatform.common.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 토큰 기반 인증 공급자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        String token = jwtAuthenticationToken.getToken();

        try {
            if (jwtTokenUtil.validateToken(token)) {
                Claims claims = jwtTokenUtil.getClaims(token);
                String userId = claims.getSubject();
                
                // 역할 정보 추출
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
                
                // 인증된 사용자 정보 생성
                JwtUserDetails userDetails = new JwtUserDetails(
                        userId,
                        claims.get("name", String.class),
                        claims.get("provider", String.class),
                        authorities
                );
                
                return new JwtAuthenticationToken(userDetails, token, authorities);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다");
        }
        
        throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
    /**
     * 클레임에서 권한 정보 추출
     */
    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object roles = claims.get("roles");
        
        if (roles instanceof List) {
            return ((List<String>) roles).stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        
        // 역할 정보가 없거나 다른 형식인 경우 기본 역할 부여
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
