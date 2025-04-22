package com.quizplatform.user.application.port.in.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 프로필 업데이트 정보 Command
 */
@Getter
public class UpdateUserProfileCommand {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private final Long userId;

    // 필요에 따라 수정 가능한 필드만 포함
    // 예: 닉네임, 이메일 등. 비밀번호 변경은 별도 UseCase 고려

    @Size(max = 50, message = "닉네임은 50자 이하이어야 합니다.")
    private final String nickname; // 닉네임은 선택적으로 변경 가능하도록 @NotBlank 제외

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private final String email; // 이메일도 선택적 변경 가능

    // ... 기타 프로필 정보 (예: profileImageUrl)

    @Builder
    public UpdateUserProfileCommand(Long userId, String nickname, String email) {
        this.userId = userId;
        this.nickname = nickname; // null 허용 가능
        this.email = email;       // null 허용 가능
        // 생성자에서 어떤 필드를 업데이트할지 명시적으로 관리할 수도 있음
    }
} 