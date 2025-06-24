package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.dto.quiz.QuizDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Quiz 엔티티의 기본 CRUD 및 조회 기능을 제공하는 리포지토리 인터페이스
 * 
 * 주요 기능:
 * - 기본 CRUD 작업 (JpaRepository 상속)
 * - 연관관계 포함 조회 (FetchJoin)
 * - DTO 직접 조회 (N+1 방지)
 * - 특정 조건 조회
 * 
 * @author 채기훈
 */
public interface QuizBasicRepository extends JpaRepository<Quiz, Long> {

    /**
     * 특정 ID의 퀴즈를 조회하며, 연관된 모든 상세 정보(질문 목록, 태그 목록, 생성자 정보)를
     * 즉시 로딩(fetch join)합니다.
     *
     * @param id 조회할 퀴즈의 ID
     * @return 모든 상세 정보가 포함된 Optional<Quiz> 객체
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "LEFT JOIN FETCH q.tags " +
            "LEFT JOIN FETCH q.creator " +
            "WHERE q.id = :id")
    Optional<Quiz> findByIdWithAllDetails(@Param("id") Long id);

    /**
     * 특정 ID의 퀴즈를 조회하며, 연관된 일부 상세 정보(태그 목록, 생성자 정보)를
     * 즉시 로딩(fetch join)합니다. 질문 목록은 로딩하지 않습니다.
     *
     * @param id 조회할 퀴즈의 ID
     * @return 태그와 생성자 정보가 포함된 Optional<Quiz> 객체
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.tags " +
            "LEFT JOIN FETCH q.creator " +
            "WHERE q.id = :id")
    Optional<Quiz> findByIdWithDetails(@Param("id") Long id);

    /**
     * 주어진 ID 목록에 해당하는 모든 퀴즈를 조회하며, 각 퀴즈의 태그 목록을
     * 즉시 로딩합니다.
     *
     * @param ids 조회할 퀴즈 ID 리스트
     * @return ID 목록에 해당하는 퀴즈 엔티티 리스트 (태그 정보 포함)
     */
    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT q FROM Quiz q WHERE q.id IN :ids")
    List<Quiz> findAllByIdWithTags(@Param("ids") List<Long> ids);

    /**
     * 퀴즈 요약 정보를 조회합니다. (N+1 문제 방지)
     *
     * @param id 조회할 퀴즈 ID
     * @return 퀴즈 엔티티 (태그 정보 포함)
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.tags WHERE q.id = :id")
    Optional<Quiz> findQuizForSummaryById(@Param("id") Long id);

    /**
     * 퀴즈 상세 정보를 DTO로 직접 조회합니다. (N+1 문제 방지)
     *
     * @param id 조회할 퀴즈 ID
     * @return 퀴즈 상세 정보 DTO
     */
    @Query("SELECT new com.quizplatform.core.dto.quiz.QuizDetailResponse(" +
           "q.id, q.title, q.description, q.quizType, q.difficultyLevel, q.questionCount, " +
           "q.timeLimit, q.creator.id, q.creator.username, q.creator.profileImage, " +
           "q.viewCount, q.attemptCount, q.avgScore, q.createdAt, q.isPublic, " +
           "null, null, null) " +
           "FROM Quiz q WHERE q.id = :id")
    Optional<QuizDetailResponse> findQuizDetailResponseById(@Param("id") Long id);

    /**
     * 특정 ID 목록에 해당하는 퀴즈의 태그 정보를 조회합니다.
     *
     * @param quizIds 조회할 퀴즈 ID 목록
     * @return [퀴즈ID, 태그ID, 태그명] 형태의 배열 리스트
     */
    @Query("SELECT q.id, t.id, t.name FROM Quiz q JOIN q.tags t WHERE q.id IN :quizIds")
    List<Object[]> findTagsByQuizIds(@Param("quizIds") Collection<Long> quizIds);

    /**
     * 특정 사용자가 생성한 퀴즈 목록을 페이징 처리하여 조회합니다.
     *
     * @param creatorId 조회할 생성자의 ID
     * @param pageable  페이징 정보
     * @return 해당 생성자가 만든 Quiz 엔티티 페이지 객체
     */
    @Query("SELECT q FROM Quiz q WHERE q.creator.id = :creatorId")
    Page<Quiz> findByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);
}