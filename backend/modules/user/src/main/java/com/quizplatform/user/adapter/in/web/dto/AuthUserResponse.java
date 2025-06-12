package com.quizplatform.user.adapter.in.web.dto;

import com.quizplatform.user.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "인증된 사용자 정보")
@Builder
public record AuthUserResponse(
    @Schema(description = "사용자 ID")
    Long id,
    
    @Schema(description = "이메일")
    String email,
    
    @Schema(description = "표시 이름")
    String displayName,
    
    @Schema(description = "권한 목록")
    List<String> roles
) {
    
    /**
     * User 엔티티로부터 AuthUserResponse 생성
     */
    public static AuthUserResponse from(User user) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .roles(user.getRoles())
                .build();
    }
}