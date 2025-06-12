package com.quizplatform.user.adapter.out.persistence.repository;

import com.quizplatform.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 리포지토리 인터페이스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     * 
     * @param username 사용자명
     * @return 사용자 Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자 조회
     * 
     * @param email 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 인증 제공자와 제공자 ID로 사용자 조회
     * 
     * @param provider 인증 제공자
     * @param providerId 제공자 ID
     * @return 사용자 Optional
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    /**
     * AuthProvider와 제공자 ID로 사용자 조회
     * 
     * @param provider 인증 제공자 enum
     * @param providerId 제공자 ID
     * @return 사용자 Optional
     */
    Optional<User> findByProviderAndProviderId(com.quizplatform.user.domain.model.AuthProvider provider, String providerId);
} 