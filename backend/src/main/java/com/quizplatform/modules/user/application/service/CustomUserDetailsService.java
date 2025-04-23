package com.quizplatform.modules.user.application.service;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.modules.user.domain.entity.User;
import com.quizplatform.modules.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security의 UserDetailsService 인터페이스를 구현하여
 * 사용자 인증 시 사용자 정보를 로드하는 서비스입니다.
 * 이메일 또는 사용자 ID를 기반으로 사용자 정보를 조회하고,
 * Spring Security가 사용할 수 있는 UserDetails 객체(UserPrincipal)를 반환합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 이름(여기서는 이메일 또는 사용자 ID 문자열)을 기반으로 사용자 정보를 로드합니다.
     * 입력된 문자열이 숫자로만 구성된 경우 사용자 ID로 간주하고 {@link #loadUserById(Long)}를 호출합니다.
     * 그렇지 않은 경우 이메일로 간주하여 사용자를 검색합니다.
     *
     * @param username 사용자 식별자 (이메일 또는 사용자 ID 문자열)
     * @return Spring Security UserDetails 객체 (UserPrincipal)
     * @throws UsernameNotFoundException 해당 이메일 또는 ID의 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Authenticating user with identifier: {}", username);

        // username이 숫자로만 이루어져 있다면 사용자 ID로 간주
        if (username.matches("\\d+")) {
            try {
                Long userId = Long.parseLong(username);
                log.debug("Identifier '{}' recognized as user ID, attempting to load by ID.", username);
                return loadUserById(userId); // ID 기반 로드 메서드 호출
            } catch (NumberFormatException e) {
                // Long으로 파싱 실패 시 (매우 큰 숫자 등), 이메일로 처리 계속 진행
                log.warn("Failed to parse identifier '{}' as Long, proceeding as email.", username, e);
            } catch (UsernameNotFoundException e) {
                // ID로 사용자를 찾지 못한 경우, 해당 ID 없음 예외 그대로 던짐
                log.warn("User not found with ID: {}", username);
                throw e;
            }
        }

        // 숫자가 아니거나 ID 파싱 실패 시 이메일로 간주하고 검색
        log.debug("Identifier '{}' recognized as email, attempting to load by email.", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    // 이메일로 사용자를 찾지 못한 경우
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        log.info("User successfully loaded by email: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        // 조회된 User 엔티티를 UserPrincipal 객체로 변환하여 반환
        return UserPrincipal.create(user);
    }

    /**
     * 사용자 ID를 기반으로 사용자 정보를 로드합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return Spring Security UserDetails 객체 (UserPrincipal)
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.info("Loading user by ID: {}", id);

        // 사용자 ID로 사용자 검색
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    // ID로 사용자를 찾지 못한 경우
                    log.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with ID: " + id);
                });

        log.info("User successfully loaded by ID: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        // 조회된 User 엔티티를 UserPrincipal 객체로 변환하여 반환
        return UserPrincipal.create(user);
    }
}