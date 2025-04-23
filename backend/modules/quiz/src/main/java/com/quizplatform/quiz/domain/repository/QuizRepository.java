package com.quizplatform.quiz.domain.repository;

import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import com.quizplatform.quiz.domain.model.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
} 