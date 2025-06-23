package com.quizplatform.core.repository.user;

import com.quizplatform.core.domain.user.LevelUpRecord;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserLevel 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 레벨 및 경험치 정보를 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {

    /**
     * 특정 사용자 ID(userId)에 해당하는 UserLevel 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 UserLevel 정보를 담은 Optional 객체
     */
    Optional<UserLevel> findByUserId(Long userId);

    /**
     * 특정 사용자의 최근 레벨업 이력을 지정된 개수(limit)만큼 조회합니다.
     * 네이티브 쿼리를 사용하여 user_level_history 테이블에서 결과를 조회합니다.
     * 업데이트 시각(updated_at) 기준 내림차순(최신순)으로 정렬됩니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param limit  조회할 최대 이력 개수
     * @return Object 배열 리스트. 각 배열의 요소는 [id, oldLevel, newLevel, occurredAt] 순서입니다.
     */
    @Query(value = "SELECT id, previous_level AS oldLevel, level AS newLevel, updated_at AS occurredAt FROM user_level_history WHERE user_id = :userId ORDER BY updated_at DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findRecentLevelUpsByUserIdRaw(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 특정 사용자(User) 객체에 해당하는 UserLevel 정보를 조회합니다.
     *
     * @param user 조회할 대상 사용자 객체
     * @return 해당 사용자의 UserLevel 정보를 담은 Optional 객체
     */
    Optional<UserLevel> findByUser(User user);

    /**
     * 지정된 최소 레벨(minLevel) 이상의 사용자들의 UserLevel 정보를 조회합니다.
     * 결과는 현재 경험치(currentExp) 기준 내림차순(높은 경험치 순)으로 정렬됩니다.
     *
     * @param minLevel 조회할 최소 레벨
     * @return 해당 레벨 이상인 사용자들의 UserLevel 엔티티 리스트
     */
    @Query("SELECT ul FROM UserLevel ul WHERE ul.level >= :minLevel ORDER BY ul.currentExp DESC")
    List<UserLevel> findByLevelGreaterThanEqualOrderByCurrentExpDesc(@Param("minLevel") int minLevel);

    /**
     * 모든 사용자의 UserLevel 정보를 페이징 처리하여 조회합니다. (랭킹/리더보드 목적)
     * 결과는 레벨(level) 내림차순, 레벨이 같으면 현재 경험치(currentExp) 내림차순으로 정렬됩니다.
     *
     * @param pageable 페이징 정보 (페이지 번호, 크기 등, 정렬 정보는 쿼리에서 지정됨)
     * @return 정렬된 UserLevel 엔티티 페이지 객체
     */
    @Query("SELECT ul FROM UserLevel ul " +
            "ORDER BY ul.level DESC, ul.currentExp DESC")
    Page<UserLevel> findTopUsers(Pageable pageable);
}