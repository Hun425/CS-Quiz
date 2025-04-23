package com.quizplatform.modules.user.infrastructure.repository;

import com.quizplatform.modules.user.domain.entity.UserBattleStats;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserBattleStats 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 배틀 모드 관련 통계 정보(승/패, 점수, 연승 등)를 관리합니다.
 * 기본적인 CRUD 기능은 JpaRepository로부터 상속받아 사용합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface UserBattleStatsRepository extends JpaRepository<UserBattleStats, Long> {
    // 기본적인 CRUD 메서드 (save, findById, findAll, delete 등)는 JpaRepository에서 제공됩니다.
    // UserBattleStats 관련 특정 조회 로직이 필요할 경우 여기에 메서드를 추가할 수 있습니다.
    // 예: Optional<UserBattleStats> findByUser(User user);
}