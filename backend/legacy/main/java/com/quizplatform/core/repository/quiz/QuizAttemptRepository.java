package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByQuiz(Quiz quiz);
    Page<QuizAttempt> findByUser(User user, Pageable pageable);

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user = :user " +
            "AND qa.createdAt BETWEEN :startDate AND :endDate")
    List<QuizAttempt> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.user = :user AND qa.createdAt >= :since")
    List<QuizAttempt> findRecentAttempts(
            @Param("user") User user,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT qa FROM QuizAttempt qa " +
            "WHERE qa.quiz = :quiz " +
            "ORDER BY qa.score DESC")
    List<QuizAttempt> findTopAttemptsByQuiz(
            @Param("quiz") Quiz quiz,
            Pageable pageable
    );

    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa " +
            "WHERE qa.user = :user AND qa.quiz = :quiz AND qa.isCompleted = true")
    boolean hasCompletedQuiz(
            @Param("user") User user,
            @Param("quiz") Quiz quiz
    );

    long countByUserId(Long userId);

    long countByUserIdAndIsCompletedTrue(Long userId);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Double getAverageScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(qa.timeTaken) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Integer getTotalTimeTakenByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    Integer getMaxScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(qa.score) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.score > 0")
    Integer getMinScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId ORDER BY qa.createdAt DESC")
    List<QuizAttempt> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 데일리 퀴즈 연속 참여 일수 조회
     */
    @Query(value = """
        WITH daily_participation AS (
            SELECT DISTINCT DATE(created_at) as quiz_date
            FROM quiz_attempts
            WHERE user_id = :userId
              AND quiz_type = 'DAILY'
            ORDER BY quiz_date DESC
        ),
        date_diff AS (
            SELECT quiz_date, 
                   (quiz_date - LEAD(quiz_date) OVER (ORDER BY quiz_date DESC)) as day_diff
            FROM daily_participation
        )
        SELECT COUNT(*) 
        FROM (
            SELECT quiz_date, day_diff,
                   SUM(CASE WHEN day_diff != 1 THEN 1 ELSE 0 END) OVER (ORDER BY quiz_date DESC) as group_id
            FROM date_diff
        ) t
        WHERE group_id = 0
        """, nativeQuery = true)
    int getDailyQuizStreakByUserId(@Param("userId") Long userId);
    
    /**
     * 가장 빠른 퀴즈 완료 시간(초) 조회
     */
    @Query("SELECT MIN(qa.timeTaken) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.isCompleted = true")
    Integer getFastestTimeTakenByUserId(@Param("userId") Long userId);
}