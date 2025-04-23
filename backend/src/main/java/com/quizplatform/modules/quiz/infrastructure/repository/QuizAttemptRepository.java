package com.quizplatform.modules.quiz.infrastructure.repository;

import com.quizplatform.modules.quiz.domain.entity.Quiz;
import com.quizplatform.modules.quiz.domain.entity.QuizAttempt;
import com.quizplatform.modules.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QuizAttempt 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 퀴즈 시도 기록을 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    /**
     * 특정 퀴즈(Quiz)에 대한 모든 시도(QuizAttempt) 목록을 조회합니다.
     *
     * @param quiz 조회할 대상 퀴즈 객체
     * @return 해당 퀴즈에 대한 QuizAttempt 엔티티 리스트
     */
    List<QuizAttempt> findByQuiz(Quiz quiz);

    /**
     * 특정 사용자(User)의 퀴즈 시도 목록을 페이징 처리하여 조회합니다.
     *
     * @param user     조회할 대상 사용자 객체
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 해당 사용자의 QuizAttempt 엔티티 페이지 객체
     */
    Page<QuizAttempt> findByUser(User user, Pageable pageable);

    /**
     * 특정 사용자가 특정 기간(시작일 ~ 종료일) 동안 생성한 퀴즈 시도 목록을 조회합니다.
     *
     * @param user      조회할 대상 사용자 객체
     * @param startDate 조회 시작 일시
     * @param endDate   조회 종료 일시
     * @return 해당 기간 내 생성된 QuizAttempt 엔티티 리스트
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user = :user " +
            "AND qa.createdAt BETWEEN :startDate AND :endDate")
    List<QuizAttempt> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 사용자가 특정 시점(since) 이후에 생성한 퀴즈 시도 목록을 조회합니다.
     * 주로 최근 활동을 조회하는 데 사용됩니다.
     *
     * @param user  조회할 대상 사용자 객체
     * @param since 조회 기준 시점 (이 시점 이후 생성된 시도만 포함)
     * @return 해당 시점 이후 생성된 QuizAttempt 엔티티 리스트
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user = :user AND qa.createdAt >= :since")
    List<QuizAttempt> findRecentAttempts(
            @Param("user") User user,
            @Param("since") LocalDateTime since
    );

    /**
     * 특정 퀴즈에 대해 높은 점수를 얻은 상위 시도 목록을 조회합니다.
     * 점수(score) 기준 내림차순으로 정렬됩니다.
     *
     * @param quiz     조회할 대상 퀴즈 객체
     * @param pageable 페이징 정보 (상위 몇 개를 조회할지 등)
     * @return 점수 순으로 정렬된 상위 QuizAttempt 엔티티 리스트
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.quiz = :quiz " +
            "ORDER BY qa.score DESC")
    List<QuizAttempt> findTopAttemptsByQuiz(
            @Param("quiz") Quiz quiz,
            Pageable pageable
    );

    /**
     * 특정 사용자가 특정 퀴즈를 이미 완료했는지 여부를 확인합니다.
     * isCompleted 플래그가 true인 시도가 하나라도 존재하면 true를 반환합니다.
     *
     * @param user 조회할 대상 사용자 객체
     * @param quiz 확인할 대상 퀴즈 객체
     * @return 해당 사용자가 해당 퀴즈를 완료했다면 true, 아니면 false
     */
    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa " +
            "WHERE qa.user = :user AND qa.quiz = :quiz AND qa.isCompleted = true")
    boolean hasCompletedQuiz(
            @Param("user") User user,
            @Param("quiz") Quiz quiz
    );

    /**
     * 특정 사용자의 총 퀴즈 시도 횟수를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 총 퀴즈 시도 횟수 (long)
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자의 완료된 퀴즈 시도 횟수를 조회합니다.
     * (isCompleted = true 인 시도만 카운트)
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자의 완료된 퀴즈 시도 횟수 (long)
     */
    long countByUserIdAndIsCompletedTrue(Long userId);

    /**
     * 특정 사용자의 모든 퀴즈 시도에 대한 평균 점수를 계산하여 반환합니다.
     * 시도 기록이 없으면 null을 반환합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 평균 점수 (Double) 또는 null
     */
    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 모든 퀴즈 시도에 소요된 총 시간(초)을 합산하여 반환합니다.
     * timeTaken 값이 기록된 시도만 합산됩니다. 시도 기록이 없거나 시간이 기록되지 않았으면 null을 반환할 수 있습니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 총 소요 시간 합계 (Integer) 또는 null
     */
    @Query("SELECT SUM(qa.timeTaken) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Integer getTotalTimeTakenByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 모든 퀴즈 시도에서 얻은 최고 점수를 반환합니다.
     * 시도 기록이 없으면 null을 반환합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 최고 점수 (Integer) 또는 null
     */
    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Integer getMaxScoreByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 모든 퀴즈 시도에서 얻은 최저 점수(0점 제외)를 반환합니다.
     * 0점 초과 점수를 기록한 시도가 없으면 null을 반환합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 0점 초과의 최저 점수 (Integer) 또는 null
     */
    @Query("SELECT MIN(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.score > 0")
    Integer getMinScoreByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 퀴즈 시도 목록을 생성 시각(createdAt) 기준 내림차순(최신순)으로 페이징 처리하여 조회합니다.
     *
     * @param userId   조회할 사용자의 ID
     * @param pageable 페이징 정보 (페이지 번호, 크기 등)
     * @return 최신순으로 정렬된 QuizAttempt 엔티티 리스트 (해당 페이지)
     */
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId ORDER BY qa.createdAt DESC")
    List<QuizAttempt> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자의 현재 연속 데일리 퀴즈 완료 일수를 계산합니다.
     * 네이티브 쿼리를 사용하여 날짜 기반의 연속성을 확인합니다.
     * CTE(Common Table Expressions)를 사용하여 가독성과 단계별 계산을 수행합니다.
     * - daily_participation: 사용자의 데일리 퀴즈 참여 날짜 목록 (중복 제거, 최신순)
     * - date_diff: 각 참여 날짜와 바로 이전 참여 날짜 간의 차이 계산 (LEAD 함수 사용)
     * - 최종 SELECT: 날짜 차이가 1이 아닌 경우 새로운 그룹으로 간주하고, 가장 최신 그룹(group_id = 0)의 크기를 세어 연속 일수 계산
     *
     * @param userId 연속 일수를 조회할 사용자의 ID
     * @return 현재 연속 데일리 퀴즈 완료 일수 (int)
     */
    @Query(value = """
        WITH daily_participation AS ( 
            SELECT DISTINCT DATE(created_at) as quiz_date -- 참여 날짜 (시간 제외) 중복 제거
            FROM quiz_attempts
            WHERE user_id = :userId
              AND quiz_type = 'DAILY' -- 데일리 퀴즈 타입 필터링
              AND is_completed = true -- 완료된 시도만 필터링
            ORDER BY quiz_date DESC -- 최신 날짜 순 정렬
        ),
        date_diff AS (
            SELECT 
                quiz_date, 
                -- 현재 날짜와 다음 날짜(LEAD)의 차이 계산 (PostgreSQL의 경우 '-' 연산자로 일수 차이 계산 가능)
                -- MySQL의 경우 DATEDIFF(quiz_date, LEAD(quiz_date) OVER (...)) 사용 필요
                (quiz_date - LEAD(quiz_date, 1, quiz_date) OVER (ORDER BY quiz_date DESC)) as day_diff 
            FROM daily_participation
        ),
        grouped_dates AS (
             SELECT 
                 quiz_date, 
                 day_diff,
                 -- 날짜 차이가 1일이 아닌 경우(연속이 끊긴 경우) 새로운 그룹 시작 (SUM 누적)
                 SUM(CASE WHEN day_diff != 1 THEN 1 ELSE 0 END) OVER (ORDER BY quiz_date DESC) as group_id
             FROM date_diff
        )
        -- 가장 최신 그룹(group_id = 0)에 속한 날짜의 개수 = 현재 연속 일수
        SELECT COUNT(*) 
        FROM grouped_dates
        WHERE group_id = 0 
        """, nativeQuery = true)
    int getDailyQuizStreakByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 완료한 퀴즈 시도 중에서 가장 짧은 소요 시간(초)을 반환합니다.
     * 완료된 시도(isCompleted = true)만 대상으로 합니다. 완료된 시도가 없으면 null을 반환합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 가장 짧은 소요 시간 (Integer) 또는 null
     */
    @Query("SELECT MIN(qa.timeTaken) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.isCompleted = true")
    Integer getFastestTimeTakenByUserId(@Param("userId") Long userId);
}