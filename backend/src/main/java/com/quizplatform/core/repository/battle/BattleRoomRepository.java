package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {
    @Query("SELECT br FROM BattleRoom br WHERE br.status = :status")
    List<BattleRoom> findByStatus(@Param("status") BattleRoomStatus status);

    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "JOIN br.participants p " +
            "WHERE p.user = :user AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUser(
            @Param("user") User user,
            @Param("status") BattleRoomStatus status
    );

    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz q " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.answers " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithAllDetails(@Param("id") Long id);
}