package com.quizplatform.core.domain.user;

import lombok.Getter;

@Getter
public enum AuthProvider {
    GOOGLE("GOOGLE"),
    GITHUB("GITHUB"),
    KAKAO("KAKAO"),
    TEST("TEST");  // 테스트용 인증 제공자 추가

    private final String providerName;

    AuthProvider(String providerName) {
        this.providerName = providerName;
    }
}