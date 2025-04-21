package com.quizplatform.modules.user.repository;

import com.quizplatform.modules.user.domain.UserLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UserLevelHistory 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 레벨 변경 이력을 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Repository
public interface UserLevelHistoryRepository extends JpaRepository<UserLevelHistory, Long> {

    /**
     * 특정 사용자의 최근 레벨업 이력을 지정된 개수(limit)만큼 조회합니다.
     * 네이티브 쿼리를 사용하여 결과를 업데이트 시각(updated_at) 기준 내림차순(최신순)으로 정렬합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param limit  조회할 최대 이력 개수
     * @return 레벨업 이력 정보를 담은 Object 배열 리스트.
     * 각 배열의 요소는 [id, user_id, oldLevel(previous_level), newLevel(level), occurredAt(updated_at)] 순서입니다.
     */
    @Query(value = "SELECT id, user_id, previous_level as oldLevel, level as newLevel, updated_at as occurredAt FROM user_level_history WHERE user_id = :userId ORDER BY updated_at DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findRecentLevelUpsByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}