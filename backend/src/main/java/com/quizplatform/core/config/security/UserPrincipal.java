package com.quizplatform.core.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString(exclude = {"user", "attributes", "password"})
@Schema(description = "인증된 사용자 정보")
public class UserPrincipal implements OAuth2User, UserDetails {
    
    @Schema(description = "사용자 ID", example = "1")
    private final Long id;
    
    @Schema(description = "이메일", example = "user@example.com")
    private final String email;
    
    @JsonIgnore
    private final String password;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private final String profileImage;
    
    @Schema(description = "인증 제공자", example = "GOOGLE")
    private final AuthProvider provider;
    
    @Schema(description = "사용자 권한", example = "[\"ROLE_USER\"]")
    private final Collection<? extends GrantedAuthority> authorities;
    
    @JsonIgnore
    private final User user;  // 도메인 User 객체를 저장하는 필드
    
    @JsonIgnore
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

    // 사용자 권한을 문자열 리스트로 반환하는 편의 메서드 (Swagger 문서화용)
    @Schema(description = "사용자 권한 목록")
    public List<String> getAuthorityNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    // 나머지 UserDetails, OAuth2User 메서드 구현...
    @Override
    @Schema(description = "사용자 이름(이메일)")
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return id.toString();
    }
}