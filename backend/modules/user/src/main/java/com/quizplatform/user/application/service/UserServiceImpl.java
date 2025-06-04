package com.quizplatform.user.application.service;

import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserLevelHistory;
import com.quizplatform.user.domain.model.UserRole;
import com.quizplatform.user.adapter.out.persistence.repository.UserRepository;
import com.quizplatform.user.adapter.out.event.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스 구현체
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        // 사용자 생성 이벤트 발행
        eventPublisher.publishUserCreated(savedUser);
        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public boolean giveExperience(Long userId, int experience) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            return false;
        }
        
        User user = userOpt.get();
        int oldLevel = user.getLevel();
        boolean leveledUp = user.gainExperience(experience);
        
        if (leveledUp) {
            // 레벨업 이벤트 발행
            eventPublisher.publishUserLevelUp(user, oldLevel);
            log.info("User level up: {} from level {} to {}", user.getId(), oldLevel, user.getLevel());
        }
        
        userRepository.save(user);
        return leveledUp;
    }

    @Override
    @Transactional
    public void givePoints(Long userId, int points) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            return;
        }
        
        User user = userOpt.get();
        user.addPoints(points);
        userRepository.save(user);
        log.info("Added {} points to user: {}", points, userId);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String username, String profileImage) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        user.updateProfile(username, profileImage);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User toggleActive(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        user.toggleActive();
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateRole(Long userId, UserRole role) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        user.updateRole(role);
        return userRepository.save(user);
    }
} 