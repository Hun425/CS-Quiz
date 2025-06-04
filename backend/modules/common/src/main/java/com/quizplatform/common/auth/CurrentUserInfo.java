package com.quizplatform.common.auth;

import java.util.List;

public record CurrentUserInfo(Long id, List<String> roles) {
}
