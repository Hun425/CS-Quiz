package com.quizplatform.modules.user.api.impl;

import com.quizplatform.modules.user.api.UserModuleApi;
import com.quizplatform.modules.user.dto.AuthResponse;
import com.quizplatform.modules.user.dto.UserProfileDto;
import com.quizplatform.modules.user.dto.UserProfileUpdateRequest;
import com.quizplatform.modules.user.dto.UserResponse;
import com.quizplatform.modules.user.service.UserService;
import com.quizplatform.modules.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 모듈 API 구현체
 * <p>
 * UserModuleApi 인터페이스를 구현하여 사용자 관련 API를 제공합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserModuleApiImpl implements UserModuleApi {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public ResponseEntity<UserResponse> getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Fetching profile for authenticated user: {}", authentication.getName());
        
        Long userId = Long.parseLong(authentication.getName());
        UserProfileDto profileDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(profileDto);
    }

    @Override
    public ResponseEntity<UserProfileDto> updateUserProfile(UserProfileUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Updating profile for authenticated user: {}", authentication.getName());
        
        Long userId = Long.parseLong(authentication.getName());
        UserProfileDto updatedProfile = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @Override
    public ResponseEntity<AuthResponse> validateToken(String token) {
        log.debug("Validating token");
        
        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().build();
        }
        
        String userId = tokenProvider.getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 새 토큰 생성
        String newToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationMs())
                .email(userDetails.getUsername()) // 이 부분은 실제 구현에 맞게 수정 필요
                .username(userDetails.getUsername()) // 이 부분은 실제 구현에 맞게 수정 필요
                .build();
        
        return ResponseEntity.ok(authResponse);
    }
}