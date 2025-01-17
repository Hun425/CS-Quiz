package com.quizplatform.core.repository;

import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}