package com.quizplatform.battle.adapter.out.persistence.repository;

import com.quizplatform.battle.domain.model.BattleRoom;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배틀룸 엔티티에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {
    
    /**
     * 배틀방 상태로 배틀방 목록을 조회합니다.
     *
     * @param status 조회할 배틀방 상태
     * @return 해당 상태의 배틀방 목록
     */
    List<BattleRoom> findByStatus(BattleRoomStatus status);
    
    /**
     * 배틀방 ID로 참가자 정보를 포함한 상세 배틀방을 조회합니다.
     *
     * @param id 조회할 배틀방 ID
     * @return 상세 정보가 포함된 배틀방
     */
    @Query("SELECT br FROM BattleRoom br LEFT JOIN FETCH br.participants WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithDetails(@Param("id") Long id);
    
    /**
     * 특정 사용자가 참여한 배틀방 중 특정 상태인 배틀방을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 배틀방 상태
     * @return 사용자가 참여한 해당 상태의 배틀방
     */
    @Query("SELECT br FROM BattleRoom br " +
           "JOIN br.participants p " +
           "WHERE p.userId = :userId AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUserId(
            @Param("userId") Long userId, 
            @Param("status") BattleRoomStatus status);
    
    /**
     * 고유 코드로 배틀방을 조회합니다.
     *
     * @param roomCode 배틀방 고유 코드
     * @return 조회된 배틀방
     */
    Optional<BattleRoom> findByRoomCode(String roomCode);
} 