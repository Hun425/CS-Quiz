package com.quizplatform.modules.user.repository;


import com.quizplatform.modules.user.domain.UserAchievementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserAchievementHistory 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 업적 획득 이력을 관리합니다.
 * (참고: 인터페이스 이름은 AchievementRepository이지만, 실제 관리 대상은 UserAchievementHistory 입니다.)
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Repository
public interface AchievementRepository extends JpaRepository<UserAchievementHistory, Long> {

    /**
     * 특정 사용자의 최근 업적 획득 이력을 지정된 개수(limit)만큼 조회합니다.
     * 네이티브 쿼리를 사용하여 결과를 획득 시각(earned_at) 기준 내림차순(최신순)으로 정렬합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param limit  조회할 최대 이력 개수
     * @return 업적 획득 이력 정보를 담은 Object 배열 리스트.
     * 각 배열의 요소는 [id, user_id, achievement(Enum명), achievement_name, earned_at] 순서입니다.
     */
    @Query(value = "SELECT id, user_id, achievement, achievement_name, earned_at FROM user_achievement_history WHERE user_id = :userId ORDER BY earned_at DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findRecentAchievementsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 특정 사용자가 특정 업적(achievement Enum 이름)을 가장 최근에 획득한 이력을 조회합니다.
     * 네이티브 쿼리를 사용하며, 해당 업적을 획득한 적이 없으면 null을 반환할 수 있습니다.
     *
     * @param userId      조회할 사용자의 ID
     * @param achievement 조회할 업적의 Enum 이름 (문자열)
     * @return 가장 최근의 업적 획득 이력 정보를 담은 Object 배열 또는 null.
     * 배열의 요소는 [id, user_id, achievement(Enum명), achievement_name, earned_at] 순서입니다.
     * @deprecated Use {@link #findEarnedAtByUserIdAndAchievement(Long, String)} for a safer way to get the earned timestamp.
     */
    @Deprecated // Object[] 반환 대신 특정 필드만 가져오는 것이 더 안전할 수 있음
    @Query(value = "SELECT id, user_id, achievement, achievement_name, earned_at FROM user_achievement_history WHERE user_id = :userId AND achievement = :achievement ORDER BY earned_at DESC LIMIT 1", nativeQuery = true)
    Object[] findByUserIdAndAchievement(@Param("userId") Long userId, @Param("achievement") String achievement);

    /**
     * 특정 사용자가 특정 업적(achievement Enum 이름)을 가장 최근에 획득한 시각(earned_at)을 조회합니다.
     * 네이티브 쿼리를 사용하며, 결과는 Optional<LocalDateTime>으로 반환하여 획득 기록이 없는 경우를 안전하게 처리합니다.
     *
     * @param userId      조회할 사용자의 ID
     * @param achievement 조회할 업적의 Enum 이름 (문자열)
     * @return 가장 최근의 업적 획득 시각을 담은 Optional<LocalDateTime>. 획득 기록이 없으면 Optional.empty() 반환.
     */
    @Query(value = "SELECT earned_at FROM user_achievement_history WHERE user_id = :userId AND achievement = :achievement ORDER BY earned_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocalDateTime> findEarnedAtByUserIdAndAchievement(@Param("userId") Long userId, @Param("achievement") String achievement);
}