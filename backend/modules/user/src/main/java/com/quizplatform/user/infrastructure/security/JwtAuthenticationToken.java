package com.quizplatform.user.infrastructure.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT 인증을 위한 Authentication 구현체
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    
    @Getter
    private final String token;

    /**
     * 인증 전 토큰 생성자
     * @param token JWT 토큰
     */
    public JwtAuthenticationToken(String token) {
        super(null);
        this.principal = null;
        this.token = token;
        setAuthenticated(false);
    }

    /**
     * 인증 완료 후 토큰 생성자
     * @param principal 사용자 정보
     * @param token JWT 토큰
     * @param authorities 권한 목록
     */
    public JwtAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
