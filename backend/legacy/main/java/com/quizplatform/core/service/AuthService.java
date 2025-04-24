package com.quizplatform.core.service;

import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.config.security.oauth.CustomOAuth2UserService;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.AuthResponse;
import com.quizplatform.core.exception.OAuth2AuthenticationProcessingException;
import com.quizplatform.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String authorizedRedirectUri;

    /**
     * 소셜 로그인 제공자의 인증 URL을 생성합니다.
     * 이 URL로 사용자를 리다이렉트하면 소셜 로그인 프로세스가 시작됩니다.
     */
    public String getAuthorizationUrl(String provider) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider.toLowerCase());

        if (registration == null) {
            throw new OAuth2AuthenticationProcessingException(
                    String.format("지원하지 않는 소셜 로그인 제공자입니다: %s", provider)
            );
        }

        // OAuth2 인증 URL 생성
        return UriComponentsBuilder
                .fromUriString(registration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", registration.getRedirectUri())
                .queryParam("scope", String.join(" ", registration.getScopes()))
                .queryParam("state", UUID.randomUUID().toString())
                .build().toUriString();
    }

    /**
     * OAuth2 인증 코드를 처리하고 사용자 정보와 토큰을 포함한 인증 응답을 생성합니다.
     * 사용자가 존재하지 않으면 새로 생성하고, 이미 존재하면 정보를 업데이트합니다.
     */
    @Transactional
    public AuthResponse processOAuth2Login(String provider, String code) {
        // OAuth2 인증 처리
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);

        // OAuth2 토큰 요청 및 사용자 정보 조회
        OAuth2AccessToken accessToken = getOAuth2AccessToken(registration, code);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(registration, accessToken);
        OAuth2User oauth2User = customOAuth2UserService.loadUser(userRequest);

        // 사용자 정보 추출 및 처리
        User user = processUserDetails(oauth2User, provider);

        // JWT 토큰 생성
        String jwtToken = jwtTokenProvider.generateAccessToken(createAuthentication(user));
        String refreshToken = jwtTokenProvider.generateRefreshToken(createAuthentication(user));

        // 인증 응답 생성
        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .build();
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     * 사용자 정보도 함께 반환하여 클라이언트가 필요한 정보를 모두 가질 수 있도록 합니다.
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("리프레시 토큰 처리 시작: {}", refreshToken);
        
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("유효하지 않은 리프레시 토큰: {}", refreshToken);
            throw new OAuth2AuthenticationProcessingException("유효하지 않은 리프레시 토큰입니다.");
        }

        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            log.info("토큰에서 추출한 사용자 ID: {}", userId);
            
            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new OAuth2AuthenticationProcessingException("사용자를 찾을 수 없습니다."));

            log.info("사용자 조회 성공: {}", user.getUsername());

            // 새로운 액세스 토큰 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(createAuthentication(user));
            log.info("새 액세스 토큰 생성 완료: {}", newAccessToken);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                    .build();
        } catch (Exception e) {
            log.error("리프레시 토큰 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationProcessingException("토큰 갱신 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 토큰을 무효화하고 사용자를 로그아웃 처리합니다.
     */
    @Transactional
    public void logout(String userId) {
        jwtTokenProvider.invalidateToken(Long.parseLong(userId));
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     */
    public String getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(
                token.replace("Bearer ", "")
        );
    }

    public String getAuthorizedRedirectUri() {
        return authorizedRedirectUri;
    }

    /**
     * OAuth2 인증 코드를 사용하여 액세스 토큰을 요청합니다.
     * 각 소셜 로그인 제공자의 토큰 엔드포인트로 요청을 보내 액세스 토큰을 받아옵니다.
     */
    private OAuth2AccessToken getOAuth2AccessToken(ClientRegistration registration, String code) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();

        // 공통 파라미터 설정
        parameters.add(OAuth2ParameterNames.GRANT_TYPE, "authorization_code");
        parameters.add(OAuth2ParameterNames.CODE, code);
        parameters.add(OAuth2ParameterNames.REDIRECT_URI, registration.getRedirectUri());

        // 제공자별 처리
        switch(registration.getRegistrationId().toLowerCase()) {
            case "google":
                // Google은 헤더에 인증 정보를 넣지 않고 본문에만 포함
                parameters.add(OAuth2ParameterNames.CLIENT_ID, registration.getClientId());
                parameters.add(OAuth2ParameterNames.CLIENT_SECRET, registration.getClientSecret());
                break;

            case "github":
            case "kakao":
                // GitHub, Kakao는 Basic Auth 헤더 사용
                parameters.add(OAuth2ParameterNames.CLIENT_ID, registration.getClientId());
                parameters.add(OAuth2ParameterNames.CLIENT_SECRET, registration.getClientSecret());

                // Basic Auth 추가 (일부 제공자는 이를 요구함)
                String credentials = registration.getClientId() + ":" + registration.getClientSecret();
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                headers.add("Authorization", "Basic " + encodedCredentials);
                break;
        }

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // HTTP 요청 생성 및 전송
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);

        try {
            ResponseEntity<OAuth2AccessTokenResponse> response = restTemplate.exchange(
                    registration.getProviderDetails().getTokenUri(),
                    HttpMethod.POST,
                    request,
                    OAuth2AccessTokenResponse.class
            );

            OAuth2AccessTokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null) {
                throw new OAuth2AuthenticationProcessingException("토큰 응답이 비어있습니다.");
            }

            // 액세스 토큰 생성 및 반환
            return new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    tokenResponse.getAccessToken().getTokenValue(),
                    tokenResponse.getAccessToken().getIssuedAt(),
                    tokenResponse.getAccessToken().getExpiresAt(),
                    Set.copyOf(tokenResponse.getAccessToken().getScopes())
            );
        } catch (Exception e) {
            log.error("OAuth2 토큰 요청 실패: {}", e.getMessage());
            throw new OAuth2AuthenticationProcessingException("OAuth2 토큰 요청에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * OAuth2 사용자 정보를 처리하고 사용자 엔티티를 생성하거나 업데이트합니다.
     * CustomOAuth2UserService의 로직과 유사하게 동작하지만, 토큰 발급을 위한 추가 처리를 포함합니다.
     */
    private User processUserDetails(OAuth2User oauth2User, String provider) {
        // 제공자별 사용자 정보 추출
        String email = extractEmail(oauth2User, provider);
        String name = extractName(oauth2User, provider);
        String imageUrl = extractImageUrl(oauth2User, provider);
        String providerId = extractProviderId(oauth2User, provider);

        // 이메일 필수 검증
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationProcessingException(
                    "이메일 정보를 찾을 수 없습니다. " + provider + " 계정에 이메일이 등록되어 있는지 확인해주세요."
            );
        }

        // 기존 사용자 조회 또는 새 사용자 생성
        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name, imageUrl))
                .orElseGet(() -> registerNewUser(email, name, imageUrl, providerId, provider));

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        return userRepository.save(user);
    }

    /**
     * 사용자 정보를 바탕으로 Authentication 객체를 생성합니다.
     * 이 Authentication 객체는 JWT 토큰 생성에 사용됩니다.
     */
    private Authentication createAuthentication(User user) {
        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // OAuth2User 구현체 생성
        OAuth2User oauth2User = new DefaultOAuth2User(
                authorities,
                Map.of(
                        "sub", user.getId().toString(),
                        "email", user.getEmail(),
                        "name", user.getUsername(),
                        "picture", user.getProfileImage()
                ),
                "sub"  // nameAttributeKey - ID를 기본 식별자로 사용
        );

        // OAuth2 인증 토큰 생성
        return new OAuth2AuthenticationToken(
                oauth2User,
                authorities,
                user.getProvider().toString().toLowerCase()
        );
    }

    // 사용자 정보 추출 유틸리티 메서드들
    private String extractEmail(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                return (String) attributes.get("email");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            default:
                throw new OAuth2AuthenticationProcessingException("지원하지 않는 로그인 제공자입니다: " + provider);
        }
    }

    private String extractName(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                return (String) attributes.get("login");
            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                return properties != null ? (String) properties.get("nickname") : null;
            default:
                throw new OAuth2AuthenticationProcessingException("지원하지 않는 로그인 제공자입니다: " + provider);
        }
    }

    private String extractImageUrl(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                return properties != null ? (String) properties.get("profile_image") : null;
            default:
                throw new OAuth2AuthenticationProcessingException("지원하지 않는 로그인 제공자입니다: " + provider);
        }
    }

    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                return String.valueOf(attributes.get("id"));
            case "kakao":
                return String.valueOf(attributes.get("id"));
            default:
                throw new OAuth2AuthenticationProcessingException("지원하지 않는 로그인 제공자입니다: " + provider);
        }
    }

    private User updateExistingUser(User existingUser, String name, String imageUrl) {
        existingUser.updateProfile(name, imageUrl);
        return existingUser;
    }

    private User registerNewUser(String email, String name, String imageUrl, String providerId, String provider) {
        return User.builder()
                .email(email)
                .username(generateUniqueUsername(name))
                .profileImage(imageUrl)
                .provider(AuthProvider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .build();
    }

    private String generateUniqueUsername(String baseName) {
        String username = baseName;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = String.format("%s%d", baseName, suffix++);
        }

        return username;
    }
}