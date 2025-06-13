package com.quizplatform.user.application.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.user.adapter.in.web.dto.AuthLoginRequest;
import com.quizplatform.user.adapter.in.web.dto.AuthUserResponse;
import com.quizplatform.user.adapter.in.web.dto.OAuth2UserRequest;
import com.quizplatform.user.adapter.in.web.dto.RegisterRequest;
import com.quizplatform.user.adapter.out.persistence.repository.UserRepository;
import com.quizplatform.user.domain.model.AuthProvider;
import com.quizplatform.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // OAuth2 전용 로그인으로 전환하여 일반 로그인/회원가입 메서드는 제거
    
    /**
     * 사용자 ID로 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public AuthUserResponse getUserById(Long userId) {
        log.debug("Getting user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        return AuthUserResponse.from(user);
    }
    
    /**
     * OAuth2 사용자 정보 처리 (조회 또는 생성)
     * 
     * @param request OAuth2 사용자 정보 요청
     * @return 처리된 사용자 정보
     */
    @Transactional
    public AuthUserResponse processOAuth2User(OAuth2UserRequest request) {
        log.info("Processing OAuth2 user: {} from provider: {}", request.email(), request.provider());
        
        // Provider와 ProviderId로 기존 사용자 조회
        AuthProvider authProvider = AuthProvider.valueOf(request.provider());
        User existingUser = userRepository.findByProviderAndProviderId(authProvider, request.providerId())
                .orElse(null);
        
        if (existingUser != null) {
            // 기존 사용자 정보 업데이트
            existingUser.updateOAuth2Info(request.email(), request.name(), request.profileImage());
            existingUser.updateLastLogin();
            
            log.info("Updated existing OAuth2 user: {} (ID: {})", request.email(), existingUser.getId());
            return AuthUserResponse.from(existingUser);
        }
        
        // 이메일로 기존 사용자 조회 (다른 Provider로 가입된 경우)
        User userByEmail = userRepository.findByEmail(request.email())
                .orElse(null);
        
        if (userByEmail != null) {
            // 같은 이메일로 이미 다른 Provider로 가입된 경우
            log.warn("User with email {} already exists with different provider: {}", 
                    request.email(), userByEmail.getProvider());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, 
                    "해당 이메일로 이미 가입된 계정이 있습니다. 기존 로그인 방식을 사용해주세요.");
        }
        
        // 새 사용자 생성
        User newUser = User.builder()
                .provider(authProvider)
                .providerId(request.providerId())
                .email(request.email())
                .username(generateUniqueUsername(request.name(), request.email()))
                .displayName(request.name())
                .profileImageUrl(request.profileImage())
                .build();
        
        User savedUser = userRepository.save(newUser);
        
        log.info("Created new OAuth2 user: {} (ID: {})", request.email(), savedUser.getId());
        return AuthUserResponse.from(savedUser);
    }
    
    /**
     * 중복되지 않는 사용자명 생성
     */
    private String generateUniqueUsername(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            name = email.split("@")[0];
        }
        
        String baseUsername = name.replaceAll("[^a-zA-Z0-9가-힣]", "");
        if (baseUsername.isEmpty()) {
            baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        }
        
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}