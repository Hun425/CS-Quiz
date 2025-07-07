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

/**
 * Spring Security의 UserDetailsService 인터페이스를 확장하여
 * 사용자 인증 시 사용자 정보를 로드하는 서비스 인터페이스입니다.
 * 이메일 또는 사용자 ID를 기반으로 사용자 정보를 조회하고,
 * Spring Security가 사용할 수 있는 UserDetails 객체(UserPrincipal)를 반환합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * 사용자 ID를 기반으로 사용자 정보를 로드합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return Spring Security UserDetails 객체 (UserPrincipal)
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우 발생
     */
    UserDetails loadUserById(Long id);
}