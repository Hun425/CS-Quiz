package com.quizplatform.user.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT에서 추출한 사용자 정보를 담는 클래스
 */
@Getter
public class JwtUserDetails implements UserDetails {

    private final String userId;
    private final String name;
    private final String provider;
    private final List<? extends GrantedAuthority> authorities;

    public JwtUserDetails(String userId, String name, String provider, List<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.name = name;
        this.provider = provider;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // JWT 인증에서는 비밀번호가 필요 없음
    }

    @Override
    public String getUsername() {
        return userId; // 사용자 식별자 (일반적으로 이메일)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
