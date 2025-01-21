package com.quizplatform.modules.user.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String name;
    private String imageUrl;
}

