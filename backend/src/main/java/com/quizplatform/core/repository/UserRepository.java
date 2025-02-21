package com.quizplatform.core.repository;

import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}