package com.quizplatform.core.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 클래스
 * 
 * <p>애플리케이션의 비즈니스 규칙 위반 시 발생하는 예외를 표현합니다.
 * 각 예외는 특정 {@link ErrorCode}를 가지며, 이를 통해 클라이언트에게
 * 적절한 오류 메시지와 상태 코드를 전달할 수 있습니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 예외와 관련된 오류 코드
     */
    private final ErrorCode errorCode;

    /**
     * 기본 생성자
     * 
     * <p>오류 코드에 정의된 기본 메시지를 사용하여 예외를 생성합니다.</p>
     * 
     * @param errorCode 예외에 해당하는 오류 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 사용자 정의 메시지를 포함한 생성자
     * 
     * <p>오류 코드와 함께 사용자 정의 메시지를 사용하여 예외를 생성합니다.</p>
     * 
     * @param errorCode 예외에 해당하는 오류 코드
     * @param message 예외에 대한 상세 메시지
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}