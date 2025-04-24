package com.quizplatform.core.service.user;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        
        // username이 숫자인 경우 ID로 간주하고 loadUserById를 호출
        if (username.matches("\\d+")) {
            try {
                Long userId = Long.parseLong(username);
                return loadUserById(userId);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse user ID: {}", username);
            }
        }
        
        // 이메일로 사용자 검색
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        log.info("User found: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.info("Loading user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        log.info("User found by ID: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        return UserPrincipal.create(user);
    }
}