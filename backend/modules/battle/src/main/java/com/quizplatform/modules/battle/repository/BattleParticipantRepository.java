package com.quizplatform.modules.battle.repository;

import com.quizplatform.modules.battle.domain.BattleParticipant;
import com.quizplatform.modules.battle.domain.BattleRoom;

import com.quizplatform.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배틀 참가자 엔티티에 대한 데이터 접근 인터페이스
 * 
 * <p>배틀 참가자 정보의 생성, 조회, 수정, 삭제 등의 DB 연산을 수행합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Repository
public interface BattleParticipantRepository extends JpaRepository<BattleParticipant, Long> {

    /**
     * 특정 대결방과 사용자로 참가자 조회
     * 
     * @param battleRoom 대결방 엔티티
     * @param user 사용자 엔티티
     * @return 참가자 Optional 객체
     */
    Optional<BattleParticipant> findByBattleRoomAndUser(BattleRoom battleRoom, User user);

    /**
     * 특정 대결방과 사용자로 참가 여부 확인
     * 
     * @param battleRoom 대결방 엔티티
     * @param user 사용자 엔티티
     * @return 참가 여부 (true/false)
     */
    boolean existsByBattleRoomAndUser(BattleRoom battleRoom, User user);

    /**
     * 특정 대결방의 모든 참가자 조회
     * 
     * @param battleRoom 대결방 엔티티
     * @return 참가자 목록
     */
    List<BattleParticipant> findByBattleRoom(BattleRoom battleRoom);

    /**
     * 특정 대결방의 모든 참가자와 답변 정보 조회
     * 
     * @param roomId 대결방 ID
     * @return 답변 정보가 포함된 참가자 목록
     */
    @Query("SELECT DISTINCT p FROM BattleParticipant p " +
            "LEFT JOIN FETCH p.answers " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.battleRoom.id = :roomId")
    List<BattleParticipant> findByBattleRoomIdWithAnswers(@Param("roomId") Long roomId);

    /**
     * 특정 참가자의 답변 정보 조회
     * 
     * @param id 참가자 ID
     * @return 답변 정보가 포함된 참가자 Optional 객체
     */
    @Query("SELECT DISTINCT p FROM BattleParticipant p " +
            "LEFT JOIN FETCH p.answers " +
            "WHERE p.id = :id")
    Optional<BattleParticipant> findByIdWithAnswers(@Param("id") Long id);

    /**
     * 특정 대결방의 활성 참가자 수 조회
     * 
     * @param roomId 대결방 ID
     * @return 활성 참가자 수
     */
    @Query("SELECT COUNT(p) FROM BattleParticipant p WHERE p.battleRoom.id = :roomId AND p.active = true")
    long countActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 사용자의 모든 참가 대결 조회
     * 
     * @param user 사용자 엔티티
     * @return 사용자가 참가한 대결 목록
     */
    List<BattleParticipant> findByUser(User user);
}