package com.quizplatform.modules.user.service.impl;

import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.user.dto.UserProfileDto;
import com.quizplatform.modules.user.dto.UserProfileUpdateRequest;
import com.quizplatform.modules.user.dto.UserResponse;
import com.quizplatform.modules.user.mapper.UserMapper;
import com.quizplatform.modules.user.repository.UserRepository;
import com.quizplatform.modules.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService 구현체
 * <p>
 * 사용자 관련 비즈니스 로직을 처리합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getUserById(Long userId) {
        log.debug("Finding user by ID: {}", userId);
        User user = findUserById(userId);
        return userMapper.toDto(user);
    }

    @Override
    public UserProfileDto getUserProfile(Long userId) {
        log.debug("Getting profile for user ID: {}", userId);
        User user = findUserById(userId);
        return userMapper.toProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.debug("Updating profile for user ID: {}", userId);
        User user = findUserById(userId);
        
        // 사용자명이 변경되었고, 이미 사용 중인 경우 확인
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + request.getUsername());
        }
        
        // 사용자 정보 업데이트
        user.updateProfile(request.getUsername(), request.getProfileImage());
        user.setBio(request.getBio());
        
        User savedUser = userRepository.save(user);
        log.info("User profile updated for ID: {}", userId);
        
        return userMapper.toProfileDto(savedUser);
    }
    
    /**
     * 사용자 ID로 사용자를 조회합니다.
     * 존재하지 않는 경우 EntityNotFoundException을 발생시킵니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws EntityNotFoundException 사용자가 존재하지 않는 경우
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }
}