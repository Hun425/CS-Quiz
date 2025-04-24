package com.quizplatform.core.exception;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
