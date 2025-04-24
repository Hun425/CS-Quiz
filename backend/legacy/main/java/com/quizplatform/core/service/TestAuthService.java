package com.quizplatform.core.service;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.user.TestTokenResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final com.quizplatform.core.repository.user.UserLevelRepository userLevelRepository;

    /**
     * 테스트용 JWT 토큰 생성
     * 사용자 이름으로 사용자를 검색하고, 존재하지 않으면 임시 사용자를 생성하여 토큰을 발급합니다.
     * 
     * @param username 사용자 이름
     * @return 테스트 토큰 응답 객체
     */
    @Transactional
    public TestTokenResponse generateTestToken(String username) {
        // 사용자 검색 또는 생성
        User user = userRepository.findByEmail(username + "@test.com")
                .orElseGet(() -> createTemporaryUser(username));
        
        log.info("Generating test token for user: {}, id: {}", user.getUsername(), user.getId());
        
        // 인증 객체 생성
        Authentication authentication = createTestAuthentication(user);
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        log.info("Generated tokens - Access token: {} (first 20 chars)", 
                accessToken.length() > 20 ? accessToken.substring(0, 20) + "..." : accessToken);
        
        return TestTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
    
    /**
     * 테스트용 임시 사용자 생성
     */
    private User createTemporaryUser(String username) {
        // 이미 같은 이름의 사용자가 있는지 확인
        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, "이미 존재하는 사용자명입니다: " + username);
        }
        
        log.info("Creating temporary user: {}", username);
        
        User user = User.builder()
                .email(username + "@test.com")
                .username(username)
                .profileImage("https://via.placeholder.com/150")
                .provider(AuthProvider.GOOGLE)  // TEST 대신 기존 제공자 사용
                .providerId("test-" + username)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created user with ID: {}", savedUser.getId());
        
        // UserLevel 정보도 함께 생성
        createUserLevel(savedUser);
        
        return savedUser;
    }
    
    /**
     * 사용자의 UserLevel 정보를 생성합니다.
     */
    private void createUserLevel(User user) {
        try {
            com.quizplatform.core.domain.user.UserLevel userLevel = new com.quizplatform.core.domain.user.UserLevel(user);
            com.quizplatform.core.domain.user.UserLevel savedLevel = userLevelRepository.save(userLevel);
            log.info("Created UserLevel for user ID: {}, level: {}", user.getId(), savedLevel.getLevel());
        } catch (Exception e) {
            log.error("Failed to create UserLevel for user ID: {}, error: {}", user.getId(), e.getMessage(), e);
            // UserLevel 생성에 실패해도 사용자는 계속 사용 가능하게
        }
    }
    
    /**
     * 테스트용 인증 객체 생성
     */
    private Authentication createTestAuthentication(User user) {
        // UserPrincipal 객체 생성 대신 ID를 직접 문자열로 사용하여 단순화
        // 인증 필터에서 loadUserByUsername 메서드를 통해 UserPrincipal로 변환됨
        return new UsernamePasswordAuthenticationToken(
                user.getId().toString(),
                null, // 테스트이므로 비밀번호는 null
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}