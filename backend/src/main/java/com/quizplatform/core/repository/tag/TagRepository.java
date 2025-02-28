package com.quizplatform.core.repository.tag;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Repository interfaces
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.children WHERE t.parent IS NULL")
    List<Tag> findAllRootTags();

    @Query("SELECT DISTINCT t FROM Tag t " +
            "LEFT JOIN FETCH t.children " +
            "WHERE t IN ( " +
            "   SELECT qt FROM Quiz q JOIN q.tags qt " +
            "   WHERE q.difficultyLevel = :difficultyLevel" +
            ")")
    List<Tag> findByQuizDifficultyLevel(@Param("difficultyLevel") DifficultyLevel difficultyLevel);

    boolean existsByName(String name);

    @Query(value = """
        SELECT t.id, t.name,
               COUNT(DISTINCT qa.id) AS quizzes_taken,
               AVG(qa.score) AS average_score,
               SUM(CASE WHEN qatt.is_correct THEN 1 ELSE 0 END) * 100.0 / COUNT(qatt.id) AS correct_rate
        FROM tags t
        JOIN quiz_tags qt ON t.id = qt.tag_id
        JOIN quizzes q ON qt.quiz_id = q.id
        JOIN quiz_attempts qa ON q.id = qa.quiz_id
        JOIN question_attempts qatt ON qa.id = qatt.quiz_attempt_id
        WHERE qa.user_id = :userId
        GROUP BY t.id, t.name
        HAVING COUNT(DISTINCT qa.id) > 0
        ORDER BY average_score DESC
        """, nativeQuery = true)
    List<Object[]> getTagPerformanceByUserId(@Param("userId") Long userId);

}
