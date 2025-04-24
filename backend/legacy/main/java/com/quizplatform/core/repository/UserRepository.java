package com.quizplatform.core.repository;

import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"battleStats"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.battleStats " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithStats(@Param("id") Long id);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.battleStats " +
            "LEFT JOIN FETCH u.quizAttempts " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithStatsAndAttempts(@Param("id") Long id);
}