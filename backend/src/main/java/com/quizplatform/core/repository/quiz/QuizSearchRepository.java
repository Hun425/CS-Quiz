package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Quiz 엔티티의 검색 및 필터링 기능을 제공하는 리포지토리 인터페이스
 * 
 * 주요 기능:
 * - 태그별 퀴즈 검색
 * - 조건별 퀴즈 필터링
 * - 데일리 퀴즈 후보 검색
 * - 타입별 퀴즈 조회
 * 
 * @author 채기훈
 */
public interface QuizSearchRepository extends Repository<Quiz, Long> {

    /**
     * 특정 태그를 포함하는 퀴즈 목록을 페이징 처리하여 조회합니다.
     *
     * @param tag      조회할 대상 태그 객체
     * @param pageable 페이징 정보
     * @return 해당 태그를 포함하는 Quiz 엔티티 페이지 객체
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "JOIN FETCH q.tags t " +
            "WHERE :tag MEMBER OF q.tags")
    Page<Quiz> findByTags(@Param("tag") Tag tag, Pageable pageable);

    /**
     * 데일리 퀴즈 후보를 찾습니다.
     * 최근에 사용된 태그나 난이도를 제외하고 공개된 일반 퀴즈를 반환합니다.
     *
     * @param recentTagIds       최근 사용된 태그 ID Set
     * @param recentDifficulties 최근 사용된 난이도 Set
     * @return 조건에 맞는 데일리 퀴즈 후보 리스트
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN q.tags t " +
            "WHERE q.quizType != 'DAILY' " +
            "AND q.isPublic = true " +
            "AND t.id NOT IN :recentTagIds " +
            "AND q.difficultyLevel NOT IN :recentDifficulties")
    List<Quiz> findQuizCandidatesForDaily(
            @Param("recentTagIds") Set<Long> recentTagIds,
            @Param("recentDifficulties") Set<DifficultyLevel> recentDifficulties
    );

    /**
     * 특정 퀴즈 타입 중에서 공개된 퀴즈 목록을 조회합니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @return 공개된 Quiz 엔티티 리스트
     */
    List<Quiz> findByQuizTypeAndIsPublicTrue(QuizType quizType);
}