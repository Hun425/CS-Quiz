package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {

    // 특정 대결방과 사용자로 참가자 조회
    Optional<BattleParticipant> findByBattleRoomAndUser(BattleRoom battleRoom, User user);

    // 특정 대결방과 사용자로 참가 여부 확인
    boolean existsByBattleRoomAndUser(BattleRoom battleRoom, User user);

    // 특정 대결방의 모든 참가자 조회
    List<BattleParticipant> findByBattleRoom(BattleRoom battleRoom);

    // 특정 대결방의 모든 참가자와 답변 정보 조회
    @Query("SELECT DISTINCT p FROM BattleParticipant p " +
            "LEFT JOIN FETCH p.answers " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.battleRoom.id = :roomId")
    List<BattleParticipant> findByBattleRoomIdWithAnswers(@Param("roomId") Long roomId);

    // 특정 참가자의 답변 정보 조회
    @Query("SELECT DISTINCT p FROM BattleParticipant p " +
            "LEFT JOIN FETCH p.answers " +
            "WHERE p.id = :id")
    Optional<BattleParticipant> findByIdWithAnswers(@Param("id") Long id);

    // 특정 대결방의 활성 참가자 수 조회
    @Query("SELECT COUNT(p) FROM BattleParticipant p WHERE p.battleRoom.id = :roomId AND p.active = true")
    long countActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    // 특정 사용자의 모든 참가 대결 조회
    List<BattleParticipant> findByUser(User user);
}