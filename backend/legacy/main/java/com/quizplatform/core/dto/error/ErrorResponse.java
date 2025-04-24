package com.quizplatform.core.dto.error;

import com.quizplatform.core.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;
    private final String detail;

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }

    // MethodArgumentNotValidException 전용 오버로드
    public static ErrorResponse of(ErrorCode errorCode, java.util.List<FieldError> fieldErrors) {
        // 예시: detail에 각 필드 에러 정보를 JSON 형태나 간단한 문자열로 넣을 수 있음
        String detail = fieldErrors.toString();
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;
    }
}
