package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
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
     * ID로 대결방을 조회하면서 퀴즈와 문제들을 즉시 로딩
     * 
     * @param id 대결방 ID
     * @return 퀴즈와 문제가 포함된 대결방 Optional 객체
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithQuizQuestionsEagerly(@Param("id") Long id);

    /**
     * 사용자별 활성 대결방 조회
     * 
     * @param user 사용자 엔티티
     * @param status 대결방 상태
     * @return 사용자의 활성 대결방 Optional 객체
     */
    @Query("SELECT br FROM BattleRoom br JOIN br.participants p WHERE p.user = :user AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUser(@Param("user") User user, @Param("status") BattleRoomStatus status);

    /**
     * 기본 정보만 로드하는 쿼리
     * 
     * @param id 대결방 ID
     * @return 기본 정보가 포함된 대결방 Optional 객체
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithBasicDetails(@Param("id") Long id);

    /**
     * 참가자 정보까지 로드하는 쿼리
     * 
     * @param id 대결방 ID
     * @return 참가자 정보가 포함된 대결방 Optional 객체
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithDetails(@Param("id") Long id);

    /**
     * 퀴즈 문제까지 로드하는 쿼리
     * 
     * @param id 대결방 ID
     * @return 퀴즈 문제가 포함된 대결방 Optional 객체
     */
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithQuizQuestions(@Param("id") Long id);

    // 문제가 되는 메서드는 제거합니다
    // @Query("SELECT DISTINCT br FROM BattleRoom br " +
    //        "LEFT JOIN FETCH br.participants p " +
    //        "LEFT JOIN FETCH p.answers " +
    //        "LEFT JOIN FETCH p.user " +
    //        "LEFT JOIN FETCH br.quiz q " +
    //        "LEFT JOIN FETCH q.questions " +
    //        "WHERE br.id = :id")
    // Optional<BattleRoom> findByIdWithAllDetails(@Param("id") Long id);
}