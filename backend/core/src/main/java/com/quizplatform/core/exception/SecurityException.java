package com.quizplatform.core.exception;

// com.quizplatform.core.exception 패키지에 위치
public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}

