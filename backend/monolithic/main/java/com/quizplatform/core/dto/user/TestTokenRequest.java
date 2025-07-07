package com.quizplatform.core.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테스트 토큰 발급 요청")
public class TestTokenRequest {
    
    @Schema(description = "토큰을 발급받을 사용자 이름", example = "testuser")
    private String username;
}