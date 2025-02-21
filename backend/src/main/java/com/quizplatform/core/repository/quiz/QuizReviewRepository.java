package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizReview;
import com.quizplatform.core.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Repository interfaces
public interface QuizReviewRepository extends JpaRepository<QuizReview, Long> {
    Page<QuizReview> findByQuiz(Quiz quiz, Pageable pageable);
    List<QuizReview> findByReviewer(User reviewer);

    @Query("SELECT AVG(r.rating) FROM QuizReview r WHERE r.quiz = :quiz")
    Double getAverageRating(@Param("quiz") Quiz quiz);

    @Query("SELECT r FROM QuizReview r " +
            "WHERE r.quiz = :quiz " +
            "ORDER BY r.createdAt DESC")
    List<QuizReview> findRecentReviews(@Param("quiz") Quiz quiz, Pageable pageable);
}