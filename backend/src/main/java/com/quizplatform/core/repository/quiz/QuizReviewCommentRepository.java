package com.quizplatform.core.repository.quiz;

import com.quizplatform.core.domain.quiz.QuizReview;
import com.quizplatform.core.domain.quiz.QuizReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizReviewCommentRepository extends JpaRepository<QuizReviewComment, UUID> {
    List<QuizReviewComment> findByParentReview(QuizReview parentReview);
}
