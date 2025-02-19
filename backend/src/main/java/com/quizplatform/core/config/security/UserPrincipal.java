package com.quizplatform.core.config.security;

import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {
    private final UUID id;
    private final String email;
    private final String password;
    private final String profileImage;
    private final AuthProvider provider;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;  // 도메인 User 객체를 저장하는 필드
    private Map<String, Object> attributes;

    // 생성자에 User 객체를 추가하여 초기화합니다.
    public UserPrincipal(User user, String email, String password, String profileImage,
                         AuthProvider provider, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.id = user.getId();
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.provider = provider;
        this.authorities = authorities;
    }

    // 정적 팩토리 메서드에서 User 객체를 전달합니다.
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user,
                user.getEmail(),
                "",  // 패스워드가 필요하다면 user.getPassword()를 사용
                user.getProfileImage(),
                user.getProvider(),
                authorities
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    // 이 메서드를 통해 도메인 User 객체를 반환할 수 있습니다.
    public User getUser() {
        return user;
    }

    // 나머지 UserDetails, OAuth2User 메서드 구현...
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

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return id.toString();
    }
}
