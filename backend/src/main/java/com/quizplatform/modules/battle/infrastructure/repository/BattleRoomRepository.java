package com.quizplatform.modules.battle.infrastructure.repository;

import com.quizplatform.modules.battle.domain.entity.BattleRoom;
import com.quizplatform.modules.battle.domain.vo.BattleRoomStatus;
import com.quizplatform.modules.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배틀룸 엔티티에 대한 데이터 접근 인터페이스
 * 
 * <p>배틀룸의 생성, 조회, 수정, 삭제 등의 DB 연산을 수행합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Repository
public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {

    /**
     * 상태별 대결방 조회
     * 
     * @param status 대결방 상태
     * @return 해당 상태의 대결방 목록
     */
    List<BattleRoom> findByStatus(BattleRoomStatus status);

    /**
     * 특정 사용자가 참여 중인 특정 상태의 활성 대결방 조회
     * 
     * @param user 사용자 엔티티
     * @param status 대결방 상태
     * @return 사용자의 활성 대결방 Optional 객체
     */
    @Query("SELECT br FROM BattleRoom br JOIN br.participants p WHERE p.user = :user AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUser(@Param("user") User user, @Param("status") BattleRoomStatus status);

    /**
     * ID로 대결방 조회 시 참가자 및 관련 정보(사용자, 퀴즈) 즉시 로딩 (상세 정보)
     * 
     * @param id 대결방 ID
     * @return 참가자 정보가 포함된 대결방 Optional 객체
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithDetails(@Param("id") Long id);

    /**
     * ID 목록으로 여러 대결방의 상세 정보(참가자, 사용자)를 즉시 로딩하여 조회
     *
     * @param ids 조회할 대결방 ID 목록
     * @return 상세 정보가 포함된 대결방 리스트
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
           "LEFT JOIN FETCH br.participants p " +
           "LEFT JOIN FETCH p.user " +
           "WHERE br.id IN :ids")
    List<BattleRoom> findByIdWithDetailsIn(@Param("ids") List<Long> ids);
}