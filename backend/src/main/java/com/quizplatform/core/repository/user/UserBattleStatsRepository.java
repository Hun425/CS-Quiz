package com.quizplatform.core.repository.user;// UserBattleStatsRepository.java


import com.quizplatform.core.domain.user.UserBattleStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserBattleStatsRepository extends JpaRepository<UserBattleStats, UUID> {
}