package com.quizplatform.core.exception;

import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리를 담당하는 핸들러 클래스
 * 
 * <p>애플리케이션에서 발생하는 다양한 예외를 일관된 형식으로 처리하고
 * 클라이언트에게 적절한 오류 응답을 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리 메서드
     *
     * <p>애플리케이션의 비즈니스 로직에서 발생한 예외를 처리합니다.</p>
     *
     * @param e 비즈니스 예외 객체
     * @return 적절한 HTTP 상태 코드와 오류 정보가 포함된 응답 객체
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<?> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus(); // ErrorCode에서 HTTP 상태 가져오기

        // 데이터를 찾을 수 없는 경우(404) 처리 방식 변경
        if (status == HttpStatus.NOT_FOUND) {
            log.info("Handling 404 Not Found: {}", e.getMessage());
            // HTTP 404 상태와 함께 CommonApiResponse.fail 사용
            return ResponseEntity
                    .status(status) // HttpStatus.NOT_FOUND
                    .body(CommonApiResponse.fail(errorCode, e.getMessage()));
        }

        // 그 외 비즈니스 예외는 CommonApiResponse.fail 또는 ErrorResponse 사용 (일관성 유지)
        // 여기서는 CommonApiResponse.fail을 사용하도록 통일
        log.warn("Handling BusinessException with status {}: {}", status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(CommonApiResponse.fail(errorCode, e.getMessage()));

        /* 이전 ErrorResponse 사용 방식 (선택 사항)
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, status);
         */
    }

    /**
     * 입력값 검증 예외 처리 메서드
     * 
     * <p>Spring의 validation 기능에 의해 발생한 입력값 검증 실패 예외를 처리합니다.</p>
     * 
     * @param e 메서드 인자 유효성 검증 예외 객체
     * @return 필드별 오류 정보가 포함된 응답 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .value(error.getRejectedValue() != null ? error.getRejectedValue().toString() : "")
                        .reason(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * 기타 모든 예외 처리 메서드
     * 
     * <p>명시적으로 처리되지 않은 모든 예외를 처리하는 마지막 방어선 역할을 합니다.</p>
     * 
     * @param e 발생한 예외 객체
     * @return 서버 오류 정보가 포함된 응답 객체
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}