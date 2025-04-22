package com.quizplatform.common.exception;

public class TokenException extends SecurityException {
    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
} 