package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
import com.quizplatform.core.domain.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Repository interfaces
public interface BattleRoomRepository extends JpaRepository<BattleRoom, UUID> {
    @Query("SELECT br FROM BattleRoom br WHERE br.status = :status")
    List<BattleRoom> findByStatus(@Param("status") BattleRoomStatus status);

    @Query("SELECT br FROM BattleRoom br " +
            "JOIN br.participants p " +
            "WHERE p.user = :user AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUser(
            @Param("user") User user,
            @Param("status") BattleRoomStatus status
    );
}