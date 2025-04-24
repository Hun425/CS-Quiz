package com.quizplatform.modules.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2Token {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}