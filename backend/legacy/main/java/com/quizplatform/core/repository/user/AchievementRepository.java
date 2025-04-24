package com.quizplatform.core.repository.user;

import com.quizplatform.core.domain.user.UserAchievementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<UserAchievementHistory, Long> {

    @Query(value = "SELECT id, user_id, achievement, achievement_name, earned_at FROM user_achievement_history WHERE user_id = :userId ORDER BY earned_at DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findRecentAchievementsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Query(value = "SELECT id, user_id, achievement, achievement_name, earned_at FROM user_achievement_history WHERE user_id = :userId AND achievement = :achievement ORDER BY earned_at DESC LIMIT 1", nativeQuery = true)
    Object[] findByUserIdAndAchievement(@Param("userId") Long userId, @Param("achievement") String achievement);
    
    // 더 안전한 방법으로 업적 조회 (Optional 반환)
    @Query(value = "SELECT earned_at FROM user_achievement_history WHERE user_id = :userId AND achievement = :achievement ORDER BY earned_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> findEarnedAtByUserIdAndAchievement(@Param("userId") Long userId, @Param("achievement") String achievement);
}