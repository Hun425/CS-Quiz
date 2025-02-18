package com.quizplatform.core.dto.quiz;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewCreateRequest {
    private UUID reviewerId;
    private UUID quizId;
    private int rating;
    private String content;

    // 유효성 검사 메서드
    public void validate() {
        if (rating < 1 || rating > 5) {
            throw new InvalidTokenException("별점은 1에서 5 사이여야 합니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidTokenException("리뷰 내용은 필수입니다.");
        }
        if (content.length() > 1000) {
            throw new InvalidTokenException("리뷰 내용은 1000자를 초과할 수 없습니다.");
        }
    }
}
