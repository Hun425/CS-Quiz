package com.quizplatform.common.exception;

import com.quizplatform.common.exception.TokenException;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
} 