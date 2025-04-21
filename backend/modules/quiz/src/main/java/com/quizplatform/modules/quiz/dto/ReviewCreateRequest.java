package com.quizplatform.modules.quiz.dto;


import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateRequest {
    private Long reviewerId;
    private Long quizId;
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
