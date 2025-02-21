package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {
    List<BattleParticipant> findByBattleRoom(BattleRoom battleRoom);

    Optional<BattleParticipant> findByBattleRoomAndUser(BattleRoom battleRoom, User user);

    @Query("SELECT bp FROM BattleParticipant bp " +
            "WHERE bp.battleRoom = :room " +
            "AND bp.user = :user " +
            "AND bp.active = true")
    Optional<BattleParticipant> findActiveParticipant(
            @Param("room") BattleRoom room,
            @Param("user") User user
    );

    @Query("SELECT COUNT(bp) > 0 FROM BattleParticipant bp " +
            "WHERE bp.battleRoom = :room " +
            "AND bp.user = :user")
    boolean existsByBattleRoomAndUser(
            @Param("room") BattleRoom room,
            @Param("user") User user
    );
}