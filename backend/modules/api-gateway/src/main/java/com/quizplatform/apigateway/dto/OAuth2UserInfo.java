package com.quizplatform.apigateway.dto;

import lombok.Builder;

@Builder
public record OAuth2UserInfo(
    String provider,
    String providerId,
    String email,
    String name,
    String profileImage
) {}