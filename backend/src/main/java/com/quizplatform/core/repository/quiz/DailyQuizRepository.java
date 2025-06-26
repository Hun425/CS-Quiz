package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 데일리 퀴즈 전용 기능을 제공하는 리포지토리 인터페이스
 * 
 * 주요 기능:
 * - 현재 유효한 데일리 퀴즈 조회
 * - 데일리 퀴즈 후보 검색
 * - 데일리 퀴즈 이력 관리
 * - 유효기간 기반 조회
 * 
 * @author 채기훈
 */
public interface DailyQuizRepository extends Repository<Quiz, Long> {

    /**
     * 현재 시점 기준으로 유효한 데일리 퀴즈를 조회하며,
     * 연관된 태그 목록을 즉시 로딩합니다.
     *
     * @param now 현재 시각
     * @return 현재 유효한 데일리 퀴즈와 태그 정보가 포함된 Optional<Quiz> 객체
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.tags " +
            "WHERE q.quizType = 'DAILY' AND q.validUntil > :now")
    Optional<Quiz> findCurrentDailyQuizWithTags(@Param("now") LocalDateTime now);

    /**
     * 현재 시점 기준으로 유효한 데일리 퀴즈를 조회합니다.
     *
     * @param now 현재 시각
     * @return 현재 유효한 데일리 퀴즈 Optional<Quiz> 객체
     */
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.validUntil > :now")
    Optional<Quiz> findCurrentDailyQuiz(@Param("now") LocalDateTime now);

    /**
     * 특정 시점 이후에 생성된 데일리 퀴즈 목록을 조회합니다.
     * 생성 시각 기준 내림차순으로 정렬됩니다.
     *
     * @param since 조회 기준 시점
     * @return 해당 시점 이후 생성된 데일리 퀴즈 엔티티 리스트
     */
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quiz> findRecentDailyQuizzes(@Param("since") LocalDateTime since);

    /**
     * 특정 퀴즈 타입 중에서 유효 기간이 주어진 시작과 종료 시각 사이에 있는 퀴즈를 조회합니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @param start    유효 기간 시작 시각
     * @param end      유효 기간 종료 시각
     * @return 조건에 맞는 Optional<Quiz> 객체
     */
    Optional<Quiz> findByQuizTypeAndValidUntilBetween(QuizType quizType, LocalDateTime start, LocalDateTime end);

    /**
     * 특정 퀴즈 타입 중에서 유효 기간이 주어진 시각 이후인 퀴즈 목록을 조회합니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @param dateTime 비교 기준 시각
     * @return 유효 기간이 dateTime 이후인 Quiz 엔티티 리스트
     */
    List<Quiz> findByQuizTypeAndValidUntilAfter(QuizType quizType, LocalDateTime dateTime);

    /**
     * 데일리 퀴즈로 선정될 수 있는 후보 퀴즈 목록을 조회합니다.
     * 일반 타입, 공개 상태, 그리고 특정 시점 이후에 데일리 퀴즈로 사용된 적 없는 퀴즈를 반환합니다.
     *
     * @param since 조회 기준 시점
     * @return 데일리 퀴즈 후보 Quiz 엔티티 리스트
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE q.quizType = com.quizplatform.core.domain.quiz.QuizType.REGULAR " +
            "AND q.isPublic = true " +
            "AND q.id NOT IN (" +
            "   SELECT dq.id FROM Quiz dq " +
            "   WHERE dq.quizType = com.quizplatform.core.domain.quiz.QuizType.DAILY " +
            "   AND dq.createdAt > :since" +
            ") " +
            "ORDER BY q.createdAt DESC")
    List<Quiz> findEligibleQuizzesForDaily(@Param("since") LocalDateTime since);
}