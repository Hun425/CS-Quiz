package com.quizplatform.battle.adapter.out.persistence.repository;

import com.quizplatform.battle.domain.model.BattleAnswer;
import com.quizplatform.battle.domain.model.BattleParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배틀 답변 엔티티에 대한 데이터 액세스를 제공하는 리포지토리 인터페이스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface BattleAnswerRepository extends JpaRepository<BattleAnswer, Long> {
    
    /**
     * 특정 참가자의 모든 답변을 조회합니다.
     *
     * @param participant 답변을 조회할 참가자
     * @return 해당 참가자의 모든 답변 목록
     */
    List<BattleAnswer> findByParticipant(BattleParticipant participant);
    
    /**
     * 특정 참가자의 특정 문제 인덱스에 대한 답변을 조회합니다.
     *
     * @param participant 답변을 조회할 참가자
     * @param questionIndex 답변을 조회할 문제 인덱스
     * @return 해당 참가자의 해당 문제에 대한 답변
     */
    Optional<BattleAnswer> findByParticipantAndQuestionIndex(BattleParticipant participant, int questionIndex);
    
    /**
     * 특정 배틀방의 특정 문제 인덱스에 대한 모든 참가자의 답변을 조회합니다.
     *
     * @param battleRoomId 배틀방 ID
     * @param questionIndex 문제 인덱스
     * @return 해당 배틀방의 해당 문제에 대한 모든 참가자의 답변 목록
     */
    @Query("SELECT ba FROM BattleAnswer ba " +
           "JOIN ba.participant p " +
           "WHERE p.battleRoom.id = :roomId AND ba.questionIndex = :questionIndex")
    List<BattleAnswer> findByBattleRoomIdAndQuestionIndex(
            @Param("roomId") Long battleRoomId, 
            @Param("questionIndex") int questionIndex);
    
    /**
     * 특정 참가자가 답변한 문제 개수를 조회합니다.
     *
     * @param participant 조회할 참가자
     * @return 해당 참가자가 답변한 문제 개수
     */
    long countByParticipant(BattleParticipant participant);
    
    /**
     * 특정 참가자의 정답 개수를 조회합니다.
     *
     * @param participant 조회할 참가자
     * @return 해당 참가자의 정답 개수
     */
    long countByParticipantAndIsCorrectTrue(BattleParticipant participant);
} 