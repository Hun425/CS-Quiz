package com.quizplatform.core.service;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import com.quizplatform.core.dto.user.TestTokenResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.user.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 테스트 인증 서비스
 * 
 * <p>개발 및 테스트 환경에서 JWT 토큰 발급 및 관리를 위한 서비스 클래스입니다.
 * 테스트용 사용자를 생성하고 임시 인증 토큰을 발급하는 기능을 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;

    /**
     * 테스트용 JWT 토큰을 생성합니다.
     * 
     * <p>사용자 이름으로 사용자를 검색하고, 존재하지 않으면 임시 사용자를 생성하여 토큰을 발급합니다.
     * 발급된 토큰은 실제 인증 시스템과 동일하게 작동합니다.</p>
     * 
     * @param username 테스트 사용자 이름
     * @return 테스트 토큰 응답 객체 (액세스 토큰, 리프레시 토큰 포함)
     * @throws BusinessException 사용자명이 이미 존재하는 경우 발생
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
     * 테스트용 임시 사용자를 생성합니다.
     * 
     * <p>사용자명에 기반한 임시 사용자 계정을 생성합니다. 
     * 생성된 사용자는 테스트 목적으로만 사용됩니다.</p>
     * 
     * @param username 생성할 사용자 이름
     * @return 생성된 사용자 엔티티
     * @throws BusinessException 동일한 사용자명이 이미 존재하는 경우 발생
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
     * 사용자의 레벨 정보를 생성합니다.
     * 
     * <p>새로 생성된 사용자를 위한 기본 레벨 정보를 초기화합니다.
     * 초기 레벨은 1로 설정됩니다.</p>
     * 
     * @param user 레벨 정보를 생성할 사용자 엔티티
     */
    private void createUserLevel(User user) {
        try {
            UserLevel userLevel = new UserLevel(user);
            UserLevel savedLevel = userLevelRepository.save(userLevel);
            log.info("Created UserLevel for user ID: {}, level: {}", user.getId(), savedLevel.getLevel());
        } catch (Exception e) {
            log.error("Failed to create UserLevel for user ID: {}, error: {}", user.getId(), e.getMessage(), e);
            // UserLevel 생성에 실패해도 사용자는 계속 사용 가능하도록 예외를 전파하지 않음
        }
    }
    
    /**
     * 테스트용 인증 객체를 생성합니다.
     * 
     * <p>Spring Security 인증 시스템에서 사용할 수 있는 
     * 인증 토큰 객체를 생성합니다.</p>
     * 
     * @param user 인증할 사용자 엔티티
     * @return 인증 객체 (Authentication)
     */
    private Authentication createTestAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                user.getId().toString(),
                null, // 테스트 목적이므로 비밀번호는 null
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}