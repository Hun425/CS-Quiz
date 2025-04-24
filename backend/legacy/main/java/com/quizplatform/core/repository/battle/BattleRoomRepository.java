package com.quizplatform.core.repository.battle;

import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {

    // 상태별 대결방 조회
    List<BattleRoom> findByStatus(BattleRoomStatus status);

    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithQuizQuestionsEagerly(@Param("id") Long id);

    // 사용자별 활성 대결방 조회
    @Query("SELECT br FROM BattleRoom br JOIN br.participants p WHERE p.user = :user AND br.status = :status")
    Optional<BattleRoom> findActiveRoomByUser(@Param("user") User user, @Param("status") BattleRoomStatus status);

    // 기본 정보만 로드하는 쿼리
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithBasicDetails(@Param("id") Long id);

    // 참가자 정보까지 로드하는 쿼리
    @Query("SELECT DISTINCT br FROM BattleRoom br " +
            "LEFT JOIN FETCH br.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH br.quiz " +
            "WHERE br.id = :id")
    Optional<BattleRoom> findByIdWithDetails(@Param("id") Long id);

    // 퀴즈 문제까지 로드하는 쿼리
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