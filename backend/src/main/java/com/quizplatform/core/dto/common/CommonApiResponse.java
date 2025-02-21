package com.quizplatform.core.dto.common;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;
    private final String code;

    private CommonApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> CommonApiResponse<T> success(T data) {
        return new CommonApiResponse<>(true, data, null, "SUCCESS");
    }

    public static <T> CommonApiResponse<T> success(T data, String message) {
        return new CommonApiResponse<>(true, data, message, "SUCCESS");
    }

    public static <T> CommonApiResponse<T> error(String message, String code) {
        return new CommonApiResponse<>(false, null, message, code);
    }
}

