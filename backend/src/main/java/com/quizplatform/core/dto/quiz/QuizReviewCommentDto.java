package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.QuizReviewComment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class QuizReviewCommentDto {
    private UUID id;
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
