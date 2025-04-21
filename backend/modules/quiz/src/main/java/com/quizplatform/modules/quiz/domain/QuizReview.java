package com.quizplatform.modules.quiz.domain;


import com.quizplatform.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 리뷰 엔티티 클래스
 * 
 * <p>사용자가 퀴즈에 대해 작성한 리뷰를 관리합니다.
 * 평점, 리뷰 내용 및 댓글을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "quiz_reviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReview {
    /**
     * 리뷰 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리뷰 대상 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    /**
     * 리뷰 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    /**
     * 평점 (1-5 별점)
     */
    private int rating;

    /**
     * 리뷰 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 리뷰에 달린 댓글 목록
     */
    @OneToMany(mappedBy = "parentReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizReviewComment> comments = new ArrayList<>();

    /**
     * 리뷰 작성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 리뷰에 댓글 추가
     * 
     * @param commenter 댓글 작성자
     * @param content 댓글 내용
     */
    public void addComment(User commenter, String content) {
        QuizReviewComment comment = new QuizReviewComment(this, commenter, content);
        comments.add(comment);
    }
}