package com.quizplatform.user.application.port.in.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 인증 정보 Command
 */
@Getter
public class AuthenticateCommand {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private final String password;

    @Builder
    public AuthenticateCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }
} 