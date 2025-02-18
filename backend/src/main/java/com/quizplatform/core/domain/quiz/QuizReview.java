package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 퀴즈 리뷰 도메인
@Entity
@Table(name = "quiz_reviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizReview {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private int rating; // 1-5 별점

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "parentReview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizReviewComment> comments = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    // 리뷰에 대한 댓글 추가
    public void addComment(User commenter, String content) {
        QuizReviewComment comment = new QuizReviewComment(this, commenter, content);
        comments.add(comment);
    }
}