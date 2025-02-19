package com.quizplatform.core.repository.tag;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Repository interfaces
public interface TagRepository extends JpaRepository<Tag, UUID> {

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
}
