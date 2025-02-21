package com.quizplatform.core.repository.level;

import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    Optional<UserLevel> findByUser(User user);

    @Query("SELECT ul FROM UserLevel ul WHERE ul.level >= :minLevel ORDER BY ul.currentExp DESC")
    List<UserLevel> findByLevelGreaterThanEqualOrderByCurrentExpDesc(@Param("minLevel") int minLevel);

    @Query("SELECT ul FROM UserLevel ul " +
            "ORDER BY ul.level DESC, ul.currentExp DESC")
    Page<UserLevel> findTopUsers(Pageable pageable);
}
