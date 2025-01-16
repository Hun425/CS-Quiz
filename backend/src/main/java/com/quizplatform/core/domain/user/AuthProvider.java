package com.quizplatform.core.domain.user;

import lombok.Getter;

@Getter
public enum AuthProvider {
    GOOGLE("GOOGLE"),
    GITHUB("GITHUB"),
    KAKAO("KAKAO");

    private final String providerName;

    AuthProvider(String providerName) {
        this.providerName = providerName;
    }
}