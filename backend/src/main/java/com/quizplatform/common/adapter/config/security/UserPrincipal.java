package com.quizplatform.common.adapter.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
// TODO: AuthProvider 와 User 클래스의 실제 위치에 따라 import 경로 수정 필요 (현재는 user 모듈로 가정)
import com.quizplatform.user.domain.AuthProvider;
import com.quizplatform.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull; // NonNull 추가
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
 * 사용자의 ID, 이메일, 권한, 프로필 이미지, 인증 제공자 등의 핵심 정보를 포함합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
// toString 생성 시 user, attributes, password 필드는 제외하여 민감 정보 노출 방지 및 순환 참조 방지
@ToString(exclude = {"user", "attributes", "password"})
@Schema(description = "인증된 사용자 정보 (Principal)")
public class UserPrincipal implements OAuth2User, UserDetails {

    @Schema(description = "사용자 고유 ID", example = "1")
    private final Long id;

    @Schema(description = "사용자 이메일 (로그인 ID로 사용)", example = "user@example.com")
    private final String email;

    /** 사용자 비밀번호 (JSON 직렬화 시 제외) */
    @JsonIgnore // 응답 객체에 포함되지 않도록 제외
    private final String password; // 일반적으로 직접 사용되지 않음 (UserDetails 인터페이스 요구사항)

    @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private final String profileImage;

    @Schema(description = "사용자 가입 시 사용된 인증 제공자 (LOCAL, GOOGLE, KAKAO 등)", example = "GOOGLE")
    private final AuthProvider provider;

    /** 사용자에게 부여된 권한 목록 (예: ROLE_USER) */
    @Schema(description = "사용자 권한", example = "[\"ROLE_USER\"]")
    private final Collection<? extends GrantedAuthority> authorities;

    /** 이 Principal 객체에 해당하는 원본 User 도메인 객체 (JSON 직렬화 시 제외) */
    @JsonIgnore
    private final User user;

    /** OAuth2 로그인 시 제공자로부터 받은 원본 속성 맵 (JSON 직렬화 시 제외) */
    @JsonIgnore
    private Map<String, Object> attributes;

    /**
     * UserPrincipal 객체를 생성합니다.
     *
     * @param user        원본 User 도메인 객체
     * @param email       사용자 이메일
     * @param password    사용자 비밀번호 (일반적으로 인코딩된 값 또는 빈 문자열)
     * @param profileImage 사용자 프로필 이미지 URL
     * @param provider    인증 제공자
     * @param authorities 사용자 권한 컬렉션
     */
    public UserPrincipal(@NonNull User user, String email, String password, String profileImage,
                         AuthProvider provider, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.id = user.getId(); // User 객체에서 ID 추출
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.provider = provider;
        this.authorities = authorities != null ? new ArrayList<>(authorities) : Collections.emptyList();
    }

    /**
     * User 도메인 객체로부터 UserPrincipal 객체를 생성하는 정적 팩토리 메서드입니다.
     * 사용자의 Role을 기반으로 GrantedAuthority 목록을 생성합니다.
     *
     * @param user User 도메인 객체
     * @return 생성된 UserPrincipal 객체
     */
    public static UserPrincipal create(@NonNull User user) {
        // 사용자의 Role 정보를 기반으로 GrantedAuthority 생성 (예: "ROLE_USER")
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        // UserPrincipal 생성자 호출
        return new UserPrincipal(
                user,
                user.getEmail(),
                "", // User 엔티티의 비밀번호 사용 (암호화된 상태일 수 있음)
                user.getProfileImage(),
                user.getProvider(),
                authorities
        );
    }

    /**
     * User 도메인 객체와 OAuth2 속성 맵으로부터 UserPrincipal 객체를 생성하는 정적 팩토리 메서드입니다.
     * OAuth2 로그인 시 사용됩니다.
     *
     * @param user       User 도메인 객체
     * @param attributes OAuth2 제공자로부터 받은 속성 맵
     * @return 속성 정보가 포함된 UserPrincipal 객체
     */
    public static UserPrincipal create(@NonNull User user, Map<String, Object> attributes) {
        // 기본 UserPrincipal 생성
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        // OAuth2 속성 설정
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    /**
     * 사용자 권한 목록을 문자열 리스트 형태로 반환합니다. (주로 DTO 변환 또는 로깅 시 사용)
     * Swagger 문서화를 위해 @Schema 추가.
     *
     * @return 권한 이름 문자열 리스트 (예: ["ROLE_USER"])
     */
    @Schema(description = "사용자 권한 목록 (문자열)", example = "[\"ROLE_USER\"]")
    public List<String> getAuthorityNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    // --- UserDetails 인터페이스 구현 ---

    /**
     * UserDetails 인터페이스의 getUsername 메서드 구현입니다.
     * 여기서는 사용자 이메일을 사용자 이름(username)으로 사용합니다.
     *
     * @return 사용자 이메일 주소
     */
    @Override
    @Schema(description = "사용자 이름 (로그인 ID, 여기서는 이메일)", example = "user@example.com")
    public String getUsername() {
        return email;
    }

    /**
     * 계정 만료 여부를 반환합니다. (UserDetails 인터페이스 구현)
     * 기본적으로 만료되지 않음(true)으로 설정됩니다.
     *
     * @return 항상 true
     */
    @Override
    @JsonIgnore // 응답 제외
    public boolean isAccountNonExpired() {
        return true; // 필요 시 로직 추가 (예: user.isAccountExpired())
    }

    /**
     * 계정 잠김 여부를 반환합니다. (UserDetails 인터페이스 구현)
     * 기본적으로 잠기지 않음(true)으로 설정됩니다.
     *
     * @return 항상 true
     */
    @Override
    @JsonIgnore // 응답 제외
    public boolean isAccountNonLocked() {
        return true; // 필요 시 로직 추가 (예: user.isAccountLocked())
    }

    /**
     * 자격 증명(비밀번호) 만료 여부를 반환합니다. (UserDetails 인터페이스 구현)
     * 기본적으로 만료되지 않음(true)으로 설정됩니다.
     *
     * @return 항상 true
     */
    @Override
    @JsonIgnore // 응답 제외
    public boolean isCredentialsNonExpired() {
        return true; // 필요 시 로직 추가 (예: user.isCredentialsExpired())
    }

    /**
     * 계정 활성화(사용 가능) 여부를 반환합니다. (UserDetails 인터페이스 구현)
     * 기본적으로 활성화(true)으로 설정됩니다.
     *
     * @return 항상 true
     */
    @Override
    @JsonIgnore // 응답 제외
    public boolean isEnabled() {
        return true; // 필요 시 로직 추가 (예: user.isEnabled())
    }

    // --- OAuth2User 인터페이스 구현 ---

    /**
     * OAuth2 제공자로부터 받은 원본 속성 맵을 반환합니다. (OAuth2User 인터페이스 구현)
     *
     * @return 사용자 속성 맵
     */
    @Override
    @JsonIgnore // 응답 제외
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * OAuth2 속성 맵을 설정합니다.
     * 주로 {@link #create(User, Map)} 메서드 내부에서 사용됩니다.
     *
     * @param attributes 설정할 사용자 속성 맵
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * OAuth2 사용자의 이름 식별자를 반환합니다. (OAuth2User 인터페이스 구현)
     * 여기서는 시스템 내부 사용자 ID를 문자열로 변환하여 사용합니다.
     * (OAuth2 표준의 'name' 클레임과는 다를 수 있음)
     *
     * @return 사용자 ID (Long)를 문자열로 변환한 값
     */
    @Override
    @JsonIgnore // UserDetails의 getUsername과 혼동될 수 있으므로 응답 제외 권장
    public String getName() {
        // OAuth2User의 name은 일반적으로 고유 식별자를 의미함
        return String.valueOf(id);
    }

    /**
     * UserDetails 인터페이스에서 요구하는 비밀번호 필드를 반환합니다.
     * @return 사용자 비밀번호 (암호화된 형태일 수 있음)
     */
    @Override
    @JsonIgnore // 응답 제외
    public String getPassword() {
        return password;
    }

    /**
     * UserDetails 인터페이스에서 요구하는 권한 목록을 반환합니다.
     * @return 사용자 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
} 