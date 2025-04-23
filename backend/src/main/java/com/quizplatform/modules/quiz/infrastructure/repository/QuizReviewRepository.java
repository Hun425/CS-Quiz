package com.quizplatform.modules.quiz.infrastructure.repository;

import com.quizplatform.modules.quiz.domain.entity.Quiz;
import com.quizplatform.modules.quiz.domain.entity.QuizReview;
import com.quizplatform.modules.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * QuizReview 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 사용자의 퀴즈 리뷰 및 평점 정보를 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuizReviewRepository extends JpaRepository<QuizReview, Long> {

    /**
     * 특정 퀴즈(Quiz)에 대한 리뷰(QuizReview) 목록을 페이징 처리하여 조회합니다.
     *
     * @param quiz     리뷰를 조회할 대상 퀴즈 객체
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 해당 퀴즈에 대한 QuizReview 엔티티 페이지 객체
     */
    Page<QuizReview> findByQuiz(Quiz quiz, Pageable pageable);

    /**
     * 특정 사용자(User)가 작성한 모든 퀴즈 리뷰 목록을 조회합니다.
     *
     * @param reviewer 리뷰를 작성한 사용자 객체
     * @return 해당 사용자가 작성한 QuizReview 엔티티 리스트
     */
    List<QuizReview> findByReviewer(User reviewer);

    /**
     * 특정 퀴즈에 대한 모든 리뷰들의 평균 평점(rating)을 계산하여 반환합니다.
     * 리뷰가 없는 경우 null을 반환할 수 있습니다.
     *
     * @param quiz 평균 평점을 계산할 대상 퀴즈 객체
     * @return 계산된 평균 평점 (Double) 또는 null
     */
    @Query("SELECT AVG(r.rating) FROM QuizReview r WHERE r.quiz = :quiz")
    Double getAverageRating(@Param("quiz") Quiz quiz);

    /**
     * 특정 퀴즈에 대한 최근 리뷰 목록을 조회합니다.
     * 생성 시각(createdAt) 기준 내림차순(최신순)으로 정렬됩니다.
     *
     * @param quiz     리뷰를 조회할 대상 퀴즈 객체
     * @param pageable 페이징 정보 (조회할 개수 제한 등)
     * @return 최신순으로 정렬된 QuizReview 엔티티 리스트 (해당 페이지)
     */
    @Query("SELECT r FROM QuizReview r " +
            "WHERE r.quiz = :quiz " +
            "ORDER BY r.createdAt DESC")
    List<QuizReview> findRecentReviews(@Param("quiz") Quiz quiz, Pageable pageable);
}