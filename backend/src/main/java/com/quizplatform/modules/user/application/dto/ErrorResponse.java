package com.quizplatform.modules.user.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 에러 응답을 위한 DTO
@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;
}