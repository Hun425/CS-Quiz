package com.quizplatform.battle.infrastructure.repository;

import com.quizplatform.battle.domain.model.BattleParticipant;
import com.quizplatform.battle.domain.model.BattleRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배틀 참가자 엔티티에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스
 */
@Repository
public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {
    
    /**
     * 특정 배틀방의 모든 참가자를 조회합니다.
     *
     * @param battleRoom 조회할 배틀방
     * @return 해당 배틀방의 모든 참가자 목록
     */
    List<BattleParticipant> findByBattleRoom(BattleRoom battleRoom);
    
    /**
     * 특정 배틀방의 특정 사용자 참가자를 조회합니다.
     *
     * @param battleRoom 조회할 배틀방
     * @param userId 조회할 사용자 ID
     * @return 해당 배틀방의 해당 사용자 참가자
     */
    Optional<BattleParticipant> findByBattleRoomAndUserId(BattleRoom battleRoom, Long userId);
    
    /**
     * 특정 배틀방에 특정 사용자가 참가했는지 확인합니다.
     *
     * @param battleRoom 확인할 배틀방
     * @param userId 확인할 사용자 ID
     * @return 참가 여부
     */
    boolean existsByBattleRoomAndUserId(BattleRoom battleRoom, Long userId);
    
    /**
     * 점수 순으로 정렬된 특정 배틀방의 모든 참가자를 조회합니다.
     *
     * @param battleRoomId 배틀방 ID
     * @return 점수 순으로 정렬된 참가자 목록
     */
    @Query("SELECT p FROM BattleParticipant p WHERE p.battleRoom.id = :roomId ORDER BY p.score DESC")
    List<BattleParticipant> findByBattleRoomIdOrderByScoreDesc(@Param("roomId") Long battleRoomId);
    
    /**
     * 특정 사용자가 참가한 모든 배틀을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자가 참가한 모든 배틀 참가자 정보
     */
    List<BattleParticipant> findByUserId(Long userId);
} 