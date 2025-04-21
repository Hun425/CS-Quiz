package com.quizplatform.common.security.oauth;

import com.quizplatform.modules.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Principal 사용자 정보 클래스
 * 
 * <p>Spring Security와 OAuth2 사용자 정보를 통합적으로 관리하는 클래스입니다.
 * UserDetails와 OAuth2User 인터페이스를 모두 구현하여 두 인증 방식에 모두 사용 가능합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    /**
     * 생성자
     * 
     * @param id 사용자 ID
     * @param email 이메일
     * @param password 비밀번호
     * @param authorities 권한 목록
     */
    public UserPrincipal(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * 사용자 엔티티로부터 UserPrincipal 객체 생성
     * 
     * @param user 사용자 엔티티
     * @return UserPrincipal 객체
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                "", // OAuth2 사용자는 비밀번호가 없음
                authorities
        );
    }

    /**
     * OAuth2 속성을 포함한 UserPrincipal 객체 생성
     * 
     * @param user 사용자 엔티티
     * @param attributes OAuth2 사용자 속성
     * @return UserPrincipal 객체
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    /**
     * OAuth2 속성 설정
     * 
     * @param attributes OAuth2 사용자 속성
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}