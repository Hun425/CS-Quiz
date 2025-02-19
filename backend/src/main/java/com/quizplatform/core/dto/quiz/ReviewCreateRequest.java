package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
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
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
