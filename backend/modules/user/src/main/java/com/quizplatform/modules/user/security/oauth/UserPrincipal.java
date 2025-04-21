package com.quizplatform.modules.user.security.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quizplatform.modules.user.domain.AuthProvider;
import com.quizplatform.modules.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Security에서 인증된 사용자를 나타내는 Principal 객체입니다.
 * {@link UserDetails} (일반 로그인)와 {@link OAuth2User} (OAuth2 로그인) 인터페이스를 모두 구현하여
 * 다양한 인증 방식에 대한 사용자 정보를 일관되게 처리합니다.
 */
@Getter
@ToString(exclude = {"user", "attributes", "password"})
@Schema(description = "인증된 사용자 정보 (Principal)")
public class UserPrincipal implements OAuth2User, UserDetails {

    @Schema(description = "사용자 고유 ID", example = "1")
    private final Long id;

    @Schema(description = "사용자 이메일 (로그인 ID로 사용)", example = "user@example.com")
    private final String email;

    @JsonIgnore
    private final String password;

    @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private final String profileImage;

    @Schema(description = "사용자 가입 시 사용된 인증 제공자 (LOCAL, GOOGLE, KAKAO 등)", example = "GOOGLE")
    private final AuthProvider provider;

    @Schema(description = "사용자 권한", example = "[\"ROLE_USER\"]")
    private final Collection<? extends GrantedAuthority> authorities;

    @JsonIgnore
    private final User user;

    @JsonIgnore
    private Map<String, Object> attributes;

    /**
     * UserPrincipal 객체를 생성합니다.
     */
    public UserPrincipal(@NonNull User user, String email, String password, String profileImage,
                         AuthProvider provider, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.id = user.getId();
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.provider = provider;
        this.authorities = authorities != null ? new ArrayList<>(authorities) : Collections.emptyList();
    }

    /**
     * User 도메인 객체로부터 UserPrincipal 객체를 생성하는 정적 팩토리 메서드입니다.
     */
    public static UserPrincipal create(@NonNull User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user,
                user.getEmail(),
                "",
                user.getProfileImage(),
                user.getProvider(),
                authorities
        );
    }

    /**
     * User 도메인 객체와 OAuth2 속성 맵으로부터 UserPrincipal 객체를 생성하는 정적 팩토리 메서드입니다.
     */
    public static UserPrincipal create(@NonNull User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    /**
     * 사용자 권한 목록을 문자열 리스트 형태로 반환합니다.
     */
    @Schema(description = "사용자 권한 목록 (문자열)", example = "[\"ROLE_USER\"]")
    public List<String> getAuthorityNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    @Override
    @Schema(description = "사용자 이름 (로그인 ID, 여기서는 이메일)", example = "user@example.com")
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

    /**
     * OAuth2 속성 맵을 설정합니다.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}