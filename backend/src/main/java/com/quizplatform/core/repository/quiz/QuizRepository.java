package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// 기본 QuizRepository 인터페이스
public interface QuizRepository extends JpaRepository<Quiz, Long>, CustomQuizRepository {

    // 데일리 퀴즈 조회
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.validUntil > :now")
    Optional<Quiz> findCurrentDailyQuiz(@Param("now") LocalDateTime now);

    // 최근 데일리 퀴즈 목록 조회
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quiz> findRecentDailyQuizzes(@Param("since") LocalDateTime since);

    // 특정 태그들을 가진 퀴즈 수 조회
    @Query("SELECT COUNT(DISTINCT q) FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    int countByTagIds(@Param("tagIds") Set<Long> tagIds);

    @Query("SELECT AVG(" +
            "  CASE q.difficultyLevel " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.BEGINNER THEN 50 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.INTERMEDIATE THEN 100 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.ADVANCED THEN 150 " +
            "    ELSE 0 " +
            "  END" +
            ") FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    Double calculateAverageDifficultyByTagIds(@Param("tagIds") Set<Long> tagIds);

    // 특정 사용자가 생성한 퀴즈 목록
    @Query("SELECT q FROM Quiz q WHERE q.creator.id = :creatorId")
    Page<Quiz> findByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);

    // 데일리 퀴즈 후보 조회 (최근에 사용되지 않은 태그와 난이도의 퀴즈들)
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN q.tags t " +
            "WHERE q.quizType != 'DAILY' " +
            "AND t.id NOT IN :recentTagIds " +
            "AND q.difficultyLevel NOT IN :recentDifficulties " +
            "AND q.isPublic = true " +
            "ORDER BY RAND()")
    Optional<Quiz> findQuizForDaily(
            @Param("recentTagIds") Set<Long> recentTagIds,
            @Param("recentDifficulties") Set<DifficultyLevel> recentDifficulties
    );

    @Query("SELECT DISTINCT q FROM Quiz q " +
            "JOIN q.tags t " +
            "WHERE t = :tag " +
            "ORDER BY q.title ASC") // 명시적인 정렬 조건 추가
    Page<Quiz> findByTags(@Param("tag") Tag tag, Pageable pageable);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions LEFT JOIN FETCH q.creator WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.tags WHERE q.id = :id")
    Optional<Quiz> findByIdWithTags(@Param("id") Long id);
}