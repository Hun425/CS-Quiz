package com.quizplatform.user.application.port.in.command;

import com.quizplatform.user.domain.model.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 등록 정보 Command
 */
@Getter
public class RegisterUserCommand {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 3, max = 20, message = "사용자 이름은 3자 이상 20자 이하이어야 합니다.")
    private final String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private final String password;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private final String email;
    
    private final String nickname;
    private final String profileImage;
    private final AuthProvider provider;
    private final String providerId;

    @Builder
    public RegisterUserCommand(
            String username,
            String password,
            String email,
            String nickname,
            String profileImage,
            AuthProvider provider,
            String providerId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        
        // 소셜 로그인의 경우 비밀번호 검증을 건너뛸 수 있음
        if (this.provider == AuthProvider.LOCAL && (password == null || password.length() < 8)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
    }
}