package com.quizplatform.modules.quiz.infrastructure.repository;

import com.quizplatform.modules.quiz.domain.entity.QuizReview;
import com.quizplatform.modules.quiz.domain.entity.QuizReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * QuizReviewComment 엔티티에 대한 데이터 접근을 처리하는 리포지토리 인터페이스입니다.
 * 퀴즈 리뷰에 달린 댓글 정보를 관리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface QuizReviewCommentRepository extends JpaRepository<QuizReviewComment, Long> {

    /**
     * 특정 퀴즈 리뷰(QuizReview)에 속하는 모든 댓글(QuizReviewComment) 목록을 조회합니다.
     *
     * @param parentReview 댓글이 속한 부모 퀴즈 리뷰 객체
     * @return 해당 퀴즈 리뷰에 포함된 QuizReviewComment 엔티티 리스트
     */
    List<QuizReviewComment> findByParentReview(QuizReview parentReview);
}