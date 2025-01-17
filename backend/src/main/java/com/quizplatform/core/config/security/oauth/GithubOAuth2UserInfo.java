package com.quizplatform.core.config.security.oauth;

import lombok.Getter;

import java.util.Map;

@Getter
class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("login");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}