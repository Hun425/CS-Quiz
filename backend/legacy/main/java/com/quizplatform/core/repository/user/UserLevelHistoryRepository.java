package com.quizplatform.core.repository.user;

import com.quizplatform.core.domain.user.UserLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {

    @Query(value = "SELECT id, user_id, previous_level as oldLevel, level as newLevel, updated_at as occurredAt FROM user_level_history WHERE user_id = :userId ORDER BY updated_at DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findRecentLevelUpsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}