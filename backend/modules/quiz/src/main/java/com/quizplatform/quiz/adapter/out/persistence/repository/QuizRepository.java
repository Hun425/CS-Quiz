package com.quizplatform.quiz.adapter.out.persistence.repository;

import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import com.quizplatform.quiz.domain.model.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 리포지토리 인터페이스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    /**
     * 카테고리로 퀴즈 목록 조회
     * 
     * @param category 카테고리
     * @return 퀴즈 목록
     */
    List<Quiz> findByCategory(String category);
    
    /**
     * 난이도로 퀴즈 목록 조회
     * 
     * @param difficulty 난이도
     * @return 퀴즈 목록
     */
    List<Quiz> findByDifficulty(int difficulty);
    
    /**
     * 생성자 ID로 퀴즈 목록 조회
     * 
     * @param creatorId 생성자 ID
     * @return 퀴즈 목록
     */
    List<Quiz> findByCreatorId(Long creatorId);
    
    /**
     * 활성화된 퀴즈 중 카테고리로 목록 조회
     * 
     * @param category 카테고리
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    List<Quiz> findByCategoryAndActive(String category, boolean active);

    /**
     * 공개된 활성화 퀴즈 페이지별 조회
     * 
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByPublishedAndActiveOrderByCreatedAtDesc(boolean published, boolean active, Pageable pageable);

    /**
     * 공개된 활성화 퀴즈 중 카테고리별 페이지 조회
     * 
     * @param category 카테고리
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByCategoryAndPublishedAndActiveOrderByCreatedAtDesc(
            String category, boolean published, boolean active, Pageable pageable);

    /**
     * 공개된 활성화 퀴즈 중 난이도별 페이지 조회
     * 
     * @param difficulty 난이도
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByDifficultyAndPublishedAndActiveOrderByCreatedAtDesc(
            int difficulty, boolean published, boolean active, Pageable pageable);

    /**
     * 생성자 ID별 퀴즈 페이지 조회
     * 
     * @param creatorId 생성자 ID
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    /**
     * 퀴즈 유형별 퀴즈 목록 조회
     * 
     * @param quizType 퀴즈 유형
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    List<Quiz> findByQuizTypeAndActive(QuizType quizType, boolean active);

    /**
     * 난이도 레벨별 활성화 퀴즈 조회
     * 
     * @param difficultyLevel 난이도 레벨
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    List<Quiz> findByDifficultyLevelAndActive(DifficultyLevel difficultyLevel, boolean active);

    /**
     * 유효 기간 내 활성화된 퀴즈 조회
     * 
     * @param now 현재 시간
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    List<Quiz> findByValidUntilGreaterThanAndActive(LocalDateTime now, boolean active);

    /**
     * 퀴즈 제목으로 검색 (키워드 포함)
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByTitleContainingAndPublishedAndActiveOrderByCreatedAtDesc(
            String keyword, boolean published, boolean active, Pageable pageable);

    /**
     * 퀴즈 태그 검색
     * 
     * @param tag 태그
     * @param published 공개 여부
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    @Query("SELECT q FROM Quiz q JOIN q.tags t WHERE t = :tag AND q.published = :published AND q.active = :active")
    List<Quiz> findByTagAndPublishedAndActive(String tag, boolean published, boolean active);

    /**
     * 특정 사용자가 가장 많이 풀어본 카테고리의 퀴즈 조회
     * 
     * @param category 카테고리
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param limit 조회 개수
     * @return 퀴즈 목록
     */
    @Query(value = "SELECT q.* FROM quizzes q WHERE q.category = :category AND q.published = :published AND q.active = :active ORDER BY q.created_at DESC LIMIT :limit", nativeQuery = true)
    List<Quiz> findRecentQuizzesByCategoryAndPublishedAndActive(String category, boolean published, boolean active, int limit);

    /**
     * 평균 점수가 높은 인기 퀴즈 조회
     * 
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByPublishedAndActiveOrderByAvgScoreDesc(boolean published, boolean active, Pageable pageable);

    /**
     * 시도 횟수가 많은 인기 퀴즈 조회
     * 
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이지 정보
     * @return 퀴즈 페이지
     */
    Page<Quiz> findByPublishedAndActiveOrderByAttemptCountDesc(boolean published, boolean active, Pageable pageable);
    
    // ===== 태그 관련 쿼리 메서드 =====
    
    /**
     * 특정 태그가 포함된 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.id = :tagId")
    List<Quiz> findByTagId(@Param("tagId") Long tagId);
    
    /**
     * 특정 태그가 포함된 활성화된 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @param published 공개 여부
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.id = :tagId AND q.published = :published AND q.active = :active")
    List<Quiz> findByTagIdAndPublishedAndActive(@Param("tagId") Long tagId, @Param("published") boolean published, @Param("active") boolean active);
    
    /**
     * 특정 태그 이름이 포함된 퀴즈 목록 조회
     * 
     * @param tagName 태그 이름
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.name = :tagName")
    List<Quiz> findByTagName(@Param("tagName") String tagName);
    
    /**
     * 여러 태그가 모두 포함된 퀴즈 목록 조회 (AND 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @param tagCount 태그 개수 (검증용)
     * @return 퀴즈 목록
     */
    @Query("SELECT q FROM Quiz q WHERE q.id IN (" +
           "SELECT qt.id FROM Quiz qt JOIN qt.tags t WHERE t.id IN :tagIds " +
           "GROUP BY qt.id HAVING COUNT(DISTINCT t.id) = :tagCount)")
    List<Quiz> findByAllTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount);
    
    /**
     * 여러 태그 중 하나라도 포함된 퀴즈 목록 조회 (OR 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds")
    List<Quiz> findByAnyTags(@Param("tagIds") List<Long> tagIds);
    
    /**
     * 태그 기반 고급 검색 (복합 조건)
     * 
     * @param includeTagIds 포함되어야 할 태그 ID들
     * @param excludeTagIds 제외되어야 할 태그 ID들
     * @param difficulty 난이도 (null이면 무시)
     * @param category 카테고리 (null이면 무시)
     * @param published 공개 여부
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN q.tags t WHERE " +
           "(:includeTagIds IS NULL OR " +
           " q.id IN (SELECT qt.id FROM Quiz qt JOIN qt.tags it WHERE it.id IN :includeTagIds " +
           "          GROUP BY qt.id HAVING COUNT(DISTINCT it.id) = :includeTagCount)) AND " +
           "(:excludeTagIds IS NULL OR " +
           " q.id NOT IN (SELECT qe.id FROM Quiz qe JOIN qe.tags et WHERE et.id IN :excludeTagIds)) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:category IS NULL OR q.category = :category) AND " +
           "q.published = :published AND q.active = :active")
    List<Quiz> findByTagCriteria(@Param("includeTagIds") List<Long> includeTagIds, @Param("includeTagCount") Long includeTagCount, 
                                 @Param("excludeTagIds") List<Long> excludeTagIds, @Param("difficulty") Integer difficulty, 
                                 @Param("category") String category, @Param("published") boolean published, @Param("active") boolean active);
    
    /**
     * 사용자의 취약 태그 기반 퀴즈 조회
     * 
     * @param tagIds 취약 태그 ID 목록
     * @param userId 사용자 ID (시도하지 않은 퀴즈 필터링용)
     * @param published 공개 여부
     * @param active 활성화 여부
     * @return 퀴즈 목록
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE " +
           "t.id IN :tagIds AND q.published = :published AND q.active = :active AND " +
           "q.id NOT IN (SELECT qa.quiz.id FROM QuizAttempt qa WHERE qa.userId = :userId AND qa.completed = true)")
    List<Quiz> findUnsolvedQuizzesWithTags(@Param("tagIds") List<Long> tagIds, @Param("userId") Long userId, 
                                           @Param("published") boolean published, @Param("active") boolean active);
    
    // ===== 페이징 지원 태그 관련 쿼리 메서드 =====
    
    /**
     * 특정 태그가 포함된 퀴즈 페이지 조회
     * 
     * @param tagId 태그 ID
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.id = :tagId AND q.published = :published AND q.active = :active ORDER BY q.createdAt DESC")
    Page<Quiz> findByTagIdAndPublishedAndActivePageable(@Param("tagId") Long tagId, @Param("published") boolean published, 
                                                        @Param("active") boolean active, Pageable pageable);
    
    /**
     * 여러 태그가 모두 포함된 퀴즈 페이지 조회 (AND 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @param tagCount 태그 개수
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Query("SELECT q FROM Quiz q WHERE q.published = :published AND q.active = :active AND q.id IN (" +
           "SELECT qt.id FROM Quiz qt JOIN qt.tags t WHERE t.id IN :tagIds " +
           "GROUP BY qt.id HAVING COUNT(DISTINCT t.id) = :tagCount) ORDER BY q.createdAt DESC")
    Page<Quiz> findByAllTagsPageable(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount, 
                                     @Param("published") boolean published, @Param("active") boolean active, Pageable pageable);
    
    /**
     * 여러 태그 중 하나라도 포함된 퀴즈 페이지 조회 (OR 조건)
     * 
     * @param tagIds 태그 ID 목록
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.tags t WHERE t.id IN :tagIds AND q.published = :published AND q.active = :active ORDER BY q.createdAt DESC")
    Page<Quiz> findByAnyTagsPageable(@Param("tagIds") List<Long> tagIds, @Param("published") boolean published, 
                                     @Param("active") boolean active, Pageable pageable);
    
    /**
     * 고급 검색 (키워드 + 태그 + 카테고리 + 난이도) 페이지 조회
     * 
     * @param keyword 검색 키워드
     * @param tagIds 태그 ID 목록
     * @param category 카테고리
     * @param difficulty 난이도
     * @param published 공개 여부
     * @param active 활성화 여부
     * @param pageable 페이징 정보
     * @return 퀴즈 페이지
     */
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN q.tags t WHERE " +
           "(:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:tagIds IS NULL OR t.id IN :tagIds) AND " +
           "(:category IS NULL OR q.category = :category) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "q.published = :published AND q.active = :active ORDER BY q.createdAt DESC")
    Page<Quiz> findByAdvancedCriteria(@Param("keyword") String keyword, @Param("tagIds") List<Long> tagIds, 
                                      @Param("category") String category, @Param("difficulty") Integer difficulty, 
                                      @Param("published") boolean published, @Param("active") boolean active, Pageable pageable);
} 