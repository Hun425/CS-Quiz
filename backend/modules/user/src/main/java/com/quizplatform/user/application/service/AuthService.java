package com.quizplatform.user.application.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.user.adapter.in.web.dto.AuthLoginRequest;
import com.quizplatform.user.adapter.in.web.dto.AuthUserResponse;
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
    
    /**
     * 사용자 로그인 인증
     * 
     * @param request 로그인 요청
     * @return 인증된 사용자 정보
     */
    public AuthUserResponse authenticateUser(AuthLoginRequest request) {
        log.info("Authenticating user with email: {}", request.email());
        
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", request.email());
                    return new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        
        // 활성화된 계정인지 확인
        if (!user.isActive()) {
            log.warn("Inactive user login attempt: {}", request.email());
            throw new BusinessException(ErrorCode.USER_INACTIVE, "비활성화된 계정입니다.");
        }
        
        // LOCAL 인증 제공자인지 확인
        if (!user.isLocalProvider()) {
            log.warn("Non-local provider login attempt: {} (provider: {})", request.email(), user.getProvider());
            throw new BusinessException(ErrorCode.INVALID_AUTH_PROVIDER, "소셜 로그인 계정입니다. 해당 플랫폼으로 로그인해주세요.");
        }
        
        // 비밀번호 검증
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", request.email());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
        }
        
        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        
        log.info("User authenticated successfully: {}", request.email());
        return AuthUserResponse.from(user);
    }
    
    /**
     * 사용자 회원가입
     * 
     * @param request 회원가입 요청
     * @return 생성된 사용자 정보
     */
    @Transactional
    public AuthUserResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());
        
        // 이메일 중복 검사
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Email already exists: {}", request.email());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 사용 중인 이메일입니다.");
        }
        
        // 사용자명 중복 검사
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Username already exists: {}", request.username());
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS, "이미 사용 중인 사용자명입니다.");
        }
        
        // 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(request.password());
        
        // 사용자 생성
        User user = User.builder()
                .provider(AuthProvider.LOCAL)
                .providerId(request.email()) // LOCAL 인증의 경우 이메일을 providerId로 사용
                .email(request.email())
                .username(request.username())
                .passwordHash(hashedPassword)
                .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully: {} (ID: {})", request.email(), savedUser.getId());
        return AuthUserResponse.from(savedUser);
    }
    
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
}