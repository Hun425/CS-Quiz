package com.quizplatform.user.application.port.in.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 레벨 업데이트 정보 Command
 */
@Getter
public class UpdateUserLevelCommand {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private final Long userId;

    @NotNull(message = "새 레벨은 필수입니다.")
    @Min(value = 1, message = "레벨은 1 이상이어야 합니다.")
    private final Integer newLevel;

    // 레벨 변경 사유 등 추가 정보 필드 가능
    // private final String reason;

    @Builder
    public UpdateUserLevelCommand(Long userId, Integer newLevel) {
        this.userId = userId;
        this.newLevel = newLevel;
    }
} 