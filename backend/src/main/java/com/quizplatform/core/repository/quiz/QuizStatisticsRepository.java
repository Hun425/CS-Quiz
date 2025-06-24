package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * Quiz 엔티티의 통계 처리 및 원자적 업데이트 기능을 제공하는 리포지토리 인터페이스
 * 
 * 주요 기능:
 * - 퀴즈 통계 계산 (개수, 평균 난이도)
 * - 원자적 카운터 업데이트 (조회수, 시도수, 평균점수)
 * - 동시성 안전 업데이트
 * 
 * @author 채기훈
 */
public interface QuizStatisticsRepository extends Repository<Quiz, Long> {

    /**
     * 주어진 태그 ID 목록 중 하나라도 포함하는 퀴즈의 총 개수를 조회합니다.
     *
     * @param tagIds 조회할 태그 ID Set
     * @return 해당 태그들을 포함하는 퀴즈의 개수
     */
    @Query("SELECT COUNT(DISTINCT q) FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    int countByTagIds(@Param("tagIds") Set<Long> tagIds);

    /**
     * 주어진 태그 ID 목록 중 하나라도 포함하는 퀴즈들의 평균 난이도를 계산합니다.
     *
     * @param tagIds 조회할 태그 ID Set
     * @return 계산된 평균 난이도 점수
     */
    @Query("SELECT AVG(" +
            "  CASE q.difficultyLevel " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.BEGINNER THEN 50 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.INTERMEDIATE THEN 100 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.ADVANCED THEN 150 " +
            "    ELSE 0 " +
            "  END" +
            ") FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    Double calculateAverageDifficultyByTagIds(@Param("tagIds") Set<Long> tagIds);

    // ===== 동시성 처리를 위한 원자적 업데이트 메서드들 =====

    /**
     * 퀴즈 조회수를 원자적으로 1 증가시킵니다.
     * 여러 사용자가 동시에 같은 퀴즈를 조회해도 안전하게 카운트됩니다.
     *
     * @param quizId 조회수를 증가시킬 퀴즈 ID
     * @return 업데이트된 행의 수 (성공 시 1)
     */
    @Modifying
    @Query("UPDATE Quiz q SET q.viewCount = q.viewCount + 1 WHERE q.id = :quizId")
    int incrementViewCountAtomic(@Param("quizId") Long quizId);

    /**
     * 퀴즈 시도 횟수를 원자적으로 1 증가시킵니다.
     * 여러 사용자가 동시에 같은 퀴즈를 시도해도 안전하게 카운트됩니다.
     *
     * @param quizId 시도 횟수를 증가시킬 퀴즈 ID
     * @return 업데이트된 행의 수 (성공 시 1)
     */
    @Modifying
    @Query("UPDATE Quiz q SET q.attemptCount = q.attemptCount + 1 WHERE q.id = :quizId")
    int incrementAttemptCountAtomic(@Param("quizId") Long quizId);

    /**
     * 퀴즈 통계(시도 횟수, 평균 점수)를 원자적으로 업데이트합니다.
     * 새로운 시도가 발생했을 때 안전하게 통계를 계산하여 업데이트합니다.
     *
     * @param quizId 업데이트할 퀴즈 ID
     * @param newScore 새로운 점수
     * @return 업데이트된 행의 수 (성공 시 1)
     */
    @Modifying
    @Query("UPDATE Quiz q SET " +
           "q.attemptCount = q.attemptCount + 1, " +
           "q.avgScore = ((q.avgScore * q.attemptCount) + :newScore) / (q.attemptCount + 1) " +
           "WHERE q.id = :quizId")
    int updateQuizStatsAtomic(@Param("quizId") Long quizId, @Param("newScore") double newScore);

    /**
     * 퀴즈 조회수와 시도 횟수를 동시에 원자적으로 증가시킵니다.
     * 퀴즈 시작 시 조회수와 시도수를 한 번에 업데이트할 때 사용합니다.
     *
     * @param quizId 업데이트할 퀴즈 ID
     * @return 업데이트된 행의 수 (성공 시 1)
     */
    @Modifying
    @Query("UPDATE Quiz q SET " +
           "q.viewCount = q.viewCount + 1, " +
           "q.attemptCount = q.attemptCount + 1 " +
           "WHERE q.id = :quizId")
    int incrementViewAndAttemptCountAtomic(@Param("quizId") Long quizId);
}