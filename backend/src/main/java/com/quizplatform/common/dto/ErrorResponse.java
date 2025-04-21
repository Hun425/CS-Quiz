package com.quizplatform.common.dto;

import com.quizplatform.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 클라이언트에게 반환되는 오류 응답 DTO
 * 
 * <p>일관된 오류 응답 형식을 제공하기 위한 클래스로,
 * 오류 상태, 코드, 메시지 및 세부 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Getter
@Builder
public class ErrorResponse {
    /**
     * HTTP 상태 코드
     */
    private final int status;
    
    /**
     * 비즈니스 오류 코드
     */
    private final String code;
    
    /**
     * 오류 메시지
     */
    private final String message;
    
    /**
     * 오류에 대한 상세 정보
     */
    private final String detail;

    /**
     * 오류 코드와 상세 정보로 ErrorResponse 객체 생성
     * 
     * @param errorCode 오류 코드 열거형
     * @param detail 상세 오류 정보
     * @return 오류 응답 객체
     */
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }

    /**
     * 입력값 검증 오류에 대한 ErrorResponse 객체 생성
     * 
     * <p>Bean Validation 등에서 발생한 필드별 오류 정보를 포함하는 응답 생성</p>
     * 
     * @param errorCode 오류 코드 열거형
     * @param fieldErrors 필드별 오류 목록
     * @return 오류 응답 객체
     */
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        String detail = fieldErrors.toString();
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }

    /**
     * 필드별 오류 정보를 담는 내부 정적 클래스
     */
    @Getter
    @Builder
    public static class FieldError {
        /**
         * 오류가 발생한 필드명
         */
        private final String field;
        
        /**
         * 오류가 발생한 값
         */
        private final String value;
        
        /**
         * 오류 발생 이유
         */
        private final String reason;
    }
}