package com.quizplatform.core.dto.common;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 응답의 표준 형식을 정의하는 클래스
 * 
 * <p>모든 API 응답에 일관된 형식을 제공하여 성공, 실패, 데이터를 포함한 표준화된 응답 객체입니다.</p>
 * 
 * @param <T> 응답 데이터의 타입
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public class CommonApiResponse<T> {
    /**
     * 요청 처리 성공 여부
     */
    private final boolean success;
    
    /**
     * 응답 데이터
     */
    private final T data;
    
    /**
     * 응답 메시지
     */
    private final String message;
    
    /**
     * 응답 시간
     */
    private final LocalDateTime timestamp;
    
    /**
     * 응답 코드
     */
    private final String code;

    /**
     * 생성자
     * 
     * @param success 성공 여부
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @param code 응답 코드
     */
    private CommonApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공 응답 생성
     * 
     * @param <T> 응답 데이터 타입
     * @param data 응답 데이터
     * @return 데이터만 포함된 성공 응답
     */
    public static <T> CommonApiResponse<T> success(T data) {
        return new CommonApiResponse<>(true, data, null, "SUCCESS");
    }

    /**
     * 메시지가 포함된 성공 응답 생성
     * 
     * @param <T> 응답 데이터 타입
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @return 데이터와 메시지가 포함된 성공 응답
     */
    public static <T> CommonApiResponse<T> success(T data, String message) {
        return new CommonApiResponse<>(true, data, message, "SUCCESS");
    }

    /**
     * 오류 응답 생성
     * 
     * @param <T> 응답 데이터 타입
     * @param message 오류 메시지
     * @param code 오류 코드
     * @return 오류 메시지와 코드가 포함된 오류 응답
     */
    public static <T> CommonApiResponse<T> error(String message, String code) {
        return new CommonApiResponse<>(false, null, message, code);
    }
}
