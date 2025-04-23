package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.QuizReview;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// DTO classes
@Getter
@Builder
public class QuizReviewDto {
    private Long id;
    private Long quizId;
    private String reviewerName;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private List<QuizReviewCommentDto> comments;

    public static QuizReviewDto from(QuizReview review) {
        return QuizReviewDto.builder()
                .id(review.getId())
                .quizId(review.getQuiz().getId())
                .reviewerName(review.getReviewer().getUsername())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .comments(review.getComments().stream()
                        .map(QuizReviewCommentDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}