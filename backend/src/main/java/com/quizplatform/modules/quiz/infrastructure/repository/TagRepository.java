package com.quizplatform.modules.quiz.infrastructure.repository;

import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 태그 엔티티에 대한 데이터 접근 인터페이스
 * 
 * <p>태그의 조회, 검색, 통계 등 다양한 데이터 접근 메서드를 제공합니다.
 * 태그 계층 구조 및 퀴즈와의 연관 관계를 처리하는 복잡한 쿼리들을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그 이름으로 태그를 찾습니다.
     * 
     * @param name 찾을 태그 이름
     * @return 태그 Optional 객체
     */
    Optional<Tag> findByName(String name);

    /**
     * 부모가 없는 모든 루트 태그를 조회합니다.
     * 자식 태그들도 함께 Fetch Join으로 로딩합니다.
     * 
     * @return 루트 태그 목록
     */
    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.children WHERE t.parent IS NULL")
    List<Tag> findAllRootTags();

    /**
     * 특정 난이도의 퀴즈에 사용된 태그들을 조회합니다.
     * 
     * @param difficultyLevel 퀴즈 난이도
     * @return 해당 난이도 퀴즈에 사용된 태그 목록
     */
    @Query("SELECT DISTINCT t FROM Tag t " +
            "LEFT JOIN FETCH t.children " +
            "WHERE t IN ( " +
            "   SELECT qt FROM Quiz q JOIN q.tags qt " +
            "   WHERE q.difficultyLevel = :difficultyLevel" +
            ")")
    List<Tag> findByQuizDifficultyLevel(@Param("difficultyLevel") DifficultyLevel difficultyLevel);

    /**
     * 특정 이름의 태그가 존재하는지 확인합니다.
     * 
     * @param name 확인할 태그 이름
     * @return 태그 존재 여부
     */
    boolean existsByName(String name);

    /**
     * 사용자별 태그 성능 지표를 조회합니다.
     * 
     * <p>사용자가 시도한 퀴즈들의 태그별 성능 통계(시도 횟수, 평균 점수, 정답률)를 제공합니다.</p>
     * 
     * @param userId 사용자 ID
     * @return 태그별 성능 통계 목록
     */
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

    /**
     * 사용자가 시도한 서로 다른 태그의 개수를 반환합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자가 시도한 고유 태그 수
     */
    @Query(value = """
        SELECT COUNT(DISTINCT t.id)
        FROM tags t
        JOIN quiz_tags qt ON t.id = qt.tag_id
        JOIN quizzes q ON qt.quiz_id = q.id
        JOIN quiz_attempts qa ON q.id = qa.quiz_id
        WHERE qa.user_id = :userId
        """, nativeQuery = true)
    int countDistinctTagsAttemptedByUserId(@Param("userId") Long userId);

    // 추가 메소드

    /**
     * 이름으로 태그 검색 (대소문자 무시)합니다.
     * 
     * @param name 검색할 태그 이름 부분 문자열
     * @param pageable 페이지 정보
     * @return 페이지네이션된 태그 목록
     */
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * 퀴즈 수가 많은 순으로 태그를 조회합니다.
     * 
     * @param pageable 페이지 정보
     * @return 퀴즈 수와 함께 태그 객체 배열 목록
     */
    @Query("SELECT t, COUNT(q) as quizCount FROM Tag t JOIN t.quizzes q GROUP BY t ORDER BY quizCount DESC")
    List<Object[]> findTopTagsByQuizCount(Pageable pageable);

    /**
     * 인기 태그 조회 (퀴즈 개수 기준)합니다.
     * 
     * @param limit 조회할 태그 수
     * @return 퀴즈 수와 함께 태그 객체 배열 목록
     */
    @Query("SELECT t, COUNT(q) as quizCount FROM Tag t JOIN t.quizzes q GROUP BY t ORDER BY quizCount DESC")
    List<Object[]> findTopTagsByQuizCount(int limit);

    /**
     * 태그별 퀴즈 개수를 조회합니다.
     * 
     * @param tagId 태그 ID
     * @return 태그에 연결된 퀴즈 수
     */
    @Query("SELECT COUNT(q) FROM Quiz q JOIN q.tags t WHERE t.id = :tagId")
    int countQuizzesForTag(@Param("tagId") Long tagId);

    /**
     * 동의어로 태그를 검색합니다.
     * 
     * @param synonym 검색할 동의어
     * @return 태그 Optional 객체
     */
    @Query("SELECT t FROM Tag t JOIN t.synonyms s WHERE s = :synonym")
    Optional<Tag> findBySynonym(@Param("synonym") String synonym);

    /**
     * 계층 구조로 모든 태그를 조회합니다.
     * 
     * <p>루트 태그부터 시작하여 2단계까지의 자식 태그를 함께 로딩합니다.</p>
     * 
     * @return 계층 구조의 태그 목록
     */
    @Query("SELECT DISTINCT t FROM Tag t LEFT JOIN FETCH t.children c LEFT JOIN FETCH c.children WHERE t.parent IS NULL")
    List<Tag> findAllWithHierarchy();

    /**
     * 부모 ID로 직계 자식 태그를 조회합니다.
     * 
     * @param parentId 부모 태그 ID
     * @return 자식 태그 목록
     */
    List<Tag> findByParentId(Long parentId);

    /**
     * 해당 태그와 모든 하위 태그를 조회합니다.
     * 
     * <p>지정된 태그 및 그 태그의 자식, 손자 태그까지 최대 3단계 깊이로 조회합니다.</p>
     * 
     * @param tagId 태그 ID
     * @return 태그와 모든 하위 태그 목록
     */
    @Query("SELECT t FROM Tag t WHERE t.id = :tagId OR t.parent.id = :tagId OR t.parent.parent.id = :tagId")
    List<Tag> findTagAndAllDescendants(@Param("tagId") Long tagId);
}