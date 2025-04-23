package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 퀴즈 리뷰 댓글 엔티티 클래스
 * 
 * <p>퀴즈 리뷰에 달린 댓글 정보를 관리합니다.
 * 댓글 작성자, 내용, 작성 시간 등의 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "quiz_review_comments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReviewComment {
    /**
     * 댓글 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 댓글이 달린 부모 리뷰
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_review_id")
    private QuizReview parentReview;

    /**
     * 댓글 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id")
    private User commenter;

    /**
     * 댓글 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 댓글 작성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 댓글 생성자
     * 
     * @param parentReview 댓글이 달린 부모 리뷰
     * @param commenter 댓글 작성자
     * @param content 댓글 내용
     */
    public QuizReviewComment(QuizReview parentReview, User commenter, String content) {
        this.parentReview = parentReview;
        this.commenter = commenter;
        this.content = content;
    }
}