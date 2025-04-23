package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.QuizReviewComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuizReviewCommentDto {
    private Long id;
    private String commenterName;
    private String content;
    private LocalDateTime createdAt;

    public static QuizReviewCommentDto from(QuizReviewComment comment) {
        return QuizReviewCommentDto.builder()
                .id(comment.getId())
                .commenterName(comment.getCommenter().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
