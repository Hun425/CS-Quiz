package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.tag.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Quiz 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 기본적인 CRUD 작업과 함께 커스텀 쿼리 메서드를 제공하며, CustomQuizRepository 기능도 상속받습니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuizRepository extends JpaRepository<Quiz, Long>, CustomQuizRepository {

    /**
     * 특정 ID의 퀴즈를 조회하며, 연관된 모든 상세 정보(질문 목록, 태그 목록, 생성자 정보)를
     * 즉시 로딩(fetch join)합니다.
     *
     * @param id 조회할 퀴즈의 ID
     * @return 모든 상세 정보가 포함된 Optional<Quiz> 객체
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " + // 질문 목록 즉시 로딩
            "LEFT JOIN FETCH q.tags " +      // 태그 목록 즉시 로딩
            "LEFT JOIN FETCH q.creator " +   // 생성자 정보 즉시 로딩
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
            "LEFT JOIN FETCH q.tags " +      // 태그 목록 즉시 로딩
            "LEFT JOIN FETCH q.creator " +   // 생성자 정보 즉시 로딩
            "WHERE q.id = :id")
    Optional<Quiz> findByIdWithDetails(@Param("id") Long id);

    /**
     * 현재 시점(now) 기준으로 유효한(validUntil > now) 데일리 퀴즈(DAILY type)를 조회하며,
     * 연관된 태그 목록을 즉시 로딩(fetch join)합니다.
     * 중복 퀴즈 방지를 위해 DISTINCT를 사용합니다.
     *
     * @param now 현재 시각 (LocalDateTime)
     * @return 현재 유효한 데일리 퀴즈와 태그 정보가 포함된 Optional<Quiz> 객체
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.tags " + // 태그 목록 즉시 로딩
            "WHERE q.quizType = 'DAILY' AND q.validUntil > :now")
    Optional<Quiz> findCurrentDailyQuizWithTags(@Param("now") LocalDateTime now);

    /**
     * 주어진 ID 목록에 해당하는 모든 퀴즈를 조회하며, 각 퀴즈의 태그 목록을
     * 즉시 로딩합니다. @EntityGraph를 사용하여 attributePaths에 명시된 연관 관계를 로딩합니다.
     *
     * @param ids 조회할 퀴즈 ID 리스트
     * @return ID 목록에 해당하는 퀴즈 엔티티 리스트 (태그 정보 포함)
     */
    @EntityGraph(attributePaths = {"tags"}) // 'tags' 연관 관계를 EAGER 로딩하도록 지정
    @Query("SELECT q FROM Quiz q WHERE q.id IN :ids")
    List<Quiz> findAllByIdWithTags(@Param("ids") List<Long> ids);

    /**
     * 현재 시점(now) 기준으로 유효한(validUntil > now) 데일리 퀴즈(DAILY type)를 조회합니다.
     * (연관 관계 즉시 로딩은 지정되지 않음)
     *
     * @param now 현재 시각 (LocalDateTime)
     * @return 현재 유효한 데일리 퀴즈 Optional<Quiz> 객체
     */
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.validUntil > :now")
    Optional<Quiz> findCurrentDailyQuiz(@Param("now") LocalDateTime now);

    /**
     * 특정 시점(since) 이후에 생성된 데일리 퀴즈(DAILY type) 목록을 조회합니다.
     * 생성 시각(createdAt) 기준 내림차순(최신순)으로 정렬됩니다.
     *
     * @param since 조회 기준 시점 (이 시점 이후 생성된 데일리 퀴즈만 포함)
     * @return 해당 시점 이후 생성된 데일리 퀴즈 엔티티 리스트
     */
    @Query("SELECT q FROM Quiz q WHERE q.quizType = 'DAILY' AND q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quiz> findRecentDailyQuizzes(@Param("since") LocalDateTime since);

    /**
     * 주어진 태그 ID 목록 중 하나라도 포함하는 퀴즈의 총 개수(중복 제거)를 조회합니다.
     *
     * @param tagIds 조회할 태그 ID Set
     * @return 해당 태그들을 포함하는 퀴즈의 개수 (int)
     */
    @Query("SELECT COUNT(DISTINCT q) FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    int countByTagIds(@Param("tagIds") Set<Long> tagIds);

    /**
     * 주어진 태그 ID 목록 중 하나라도 포함하는 퀴즈들의 평균 난이도를 계산합니다.
     * 난이도 Enum 값을 특정 점수(BEGINNER=50, INTERMEDIATE=100, ADVANCED=150)로 매핑하여 평균을 계산합니다.
     *
     * @param tagIds 조회할 태그 ID Set
     * @return 계산된 평균 난이도 점수 (Double) 또는 null (해당 퀴즈가 없을 경우)
     */
    @Query("SELECT AVG(" +
            "  CASE q.difficultyLevel " + // 퀴즈 난이도에 따라 점수 부여
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.BEGINNER THEN 50 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.INTERMEDIATE THEN 100 " +
            "    WHEN com.quizplatform.core.domain.quiz.DifficultyLevel.ADVANCED THEN 150 " +
            "    ELSE 0 " + // 그 외 경우는 0점 처리
            "  END" +
            ") FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds") // 태그 ID 목록에 포함되는 퀴즈 대상
    Double calculateAverageDifficultyByTagIds(@Param("tagIds") Set<Long> tagIds);

    /**
     * 특정 사용자(creatorId)가 생성한 퀴즈 목록을 페이징 처리하여 조회합니다.
     *
     * @param creatorId 조회할 생성자의 ID
     * @param pageable  페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 해당 생성자가 만든 Quiz 엔티티 페이지 객체
     */
    @Query("SELECT q FROM Quiz q WHERE q.creator.id = :creatorId")
    Page<Quiz> findByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);

    /**
     * 데일리 퀴즈 후보를 찾습니다.
     * 최근에 데일리 퀴즈로 사용된 태그(recentTagIds)나 난이도(recentDifficulties)를 가진 퀴즈를 제외하고,
     * 공개된(isPublic=true) 일반(REGULAR) 퀴즈 중에서 후보 목록을 반환합니다.
     *
     * @param recentTagIds       최근 데일리 퀴즈에서 사용된 태그 ID Set
     * @param recentDifficulties 최근 데일리 퀴즈에서 사용된 난이도 Set
     * @return 조건에 맞는 데일리 퀴즈 후보 리스트
     */
    @Query("SELECT q FROM Quiz q " +
            "LEFT JOIN q.tags t " + // 태그 조인이 필요할 수 있음 (t.id 사용 시)
            "WHERE q.quizType != 'DAILY' " + // 데일리 퀴즈 타입 제외
            "AND q.isPublic = true " + // 공개된 퀴즈만
            "AND t.id NOT IN :recentTagIds " + // 최근 사용된 태그 제외
            "AND q.difficultyLevel NOT IN :recentDifficulties") // 최근 사용된 난이도 제외
    List<Quiz> findQuizCandidatesForDaily(
            @Param("recentTagIds") Set<Long> recentTagIds,
            @Param("recentDifficulties") Set<DifficultyLevel> recentDifficulties
    );

    /**
     * 특정 태그(Tag)를 포함하는 퀴즈 목록을 제목(title) 오름차순으로 페이징 처리하여 조회합니다.
     * 태그 정보를 즉시 로딩(fetch join)합니다.
     *
     * @param tag      조회할 대상 태그 객체
     * @param pageable 페이징 정보
     * @return 해당 태그를 포함하는 Quiz 엔티ティ 페이지 객체 (태그 정보 포함)
     */
    @Query("SELECT DISTINCT q FROM Quiz q " + // 태그가 여러 개일 경우 퀴즈 중복 방지
            "JOIN FETCH q.tags t " + // 태그 정보 즉시 로딩 (파라미터 tag와 비교 위해 필요)
            "WHERE :tag MEMBER OF q.tags") // 퀴즈의 tags 컬렉션에 파라미터 tag가 포함되어 있는지 확인
    Page<Quiz> findByTags(@Param("tag") Tag tag, Pageable pageable);

    // ===== 데일리 퀴즈 서비스 추가 메서드 =====

    /**
     * 특정 퀴즈 타입(quizType) 중에서 유효 기간(validUntil)이 주어진 시작(start)과 종료(end) 시각 사이에 있는 퀴즈를 조회합니다.
     * 주로 특정 날짜에 유효한 데일리 퀴즈를 찾을 때 사용될 수 있습니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @param start    유효 기간 시작 시각 (이 시각 이후에 만료되어야 함)
     * @param end      유효 기간 종료 시각 (이 시각 이전에 만료되어야 함)
     * @return 조건에 맞는 Optional<Quiz> 객체
     */
    Optional<Quiz> findByQuizTypeAndValidUntilBetween(QuizType quizType, LocalDateTime start, LocalDateTime end);

    /**
     * 특정 퀴즈 타입(quizType) 중에서 유효 기간(validUntil)이 주어진 시각(dateTime) 이후인 퀴즈 목록을 조회합니다.
     * 아직 유효한 데일리 퀴즈 목록 등을 조회할 때 사용될 수 있습니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @param dateTime 비교 기준 시각
     * @return 유효 기간이 dateTime 이후인 Quiz 엔티티 리스트
     */
    List<Quiz> findByQuizTypeAndValidUntilAfter(QuizType quizType, LocalDateTime dateTime);

    /**
     * 특정 퀴즈 타입(quizType) 중에서 공개된(isPublic = true) 퀴즈 목록을 조회합니다.
     *
     * @param quizType 조회할 퀴즈 타입
     * @return 공개된 Quiz 엔티티 리스트
     */
    List<Quiz> findByQuizTypeAndIsPublicTrue(QuizType quizType);

    /**
     * 데일리 퀴즈로 선정될 수 있는 후보 퀴즈 목록을 조회합니다.
     * 조건: 일반(REGULAR) 타입, 공개(isPublic=true) 상태, 그리고 특정 시점(since) 이후에
     * 데일리 퀴즈(DAILY type)로 사용된 적 없는 퀴즈.
     * 생성 시각(createdAt) 기준 내림차순(최신순)으로 정렬됩니다.
     *
     * @param since 조회 기준 시점 (이 시점 이후 데일리 퀴즈로 사용된 퀴즈는 제외)
     * @return 데일리 퀴즈 후보 Quiz 엔티티 리스트
     */
    @Query("SELECT q FROM Quiz q " +
            "WHERE q.quizType = com.quizplatform.core.domain.quiz.QuizType.REGULAR " + // 일반 퀴즈 타입
            "AND q.isPublic = true " + // 공개된 퀴즈
            "AND q.id NOT IN (" + // 서브쿼리: 최근 데일리 퀴즈로 사용된 퀴즈 ID 제외
            "   SELECT dq.id FROM Quiz dq " +
            "   WHERE dq.quizType = com.quizplatform.core.domain.quiz.QuizType.DAILY " + // 데일리 퀴즈 타입
            "   AND dq.createdAt > :since" + // 특정 시점 이후 생성(선정)된 데일리 퀴즈
            ") " +
            "ORDER BY q.createdAt DESC") // 최신 생성된 퀴즈 우선
    List<Quiz> findEligibleQuizzesForDaily(@Param("since") LocalDateTime since);
}