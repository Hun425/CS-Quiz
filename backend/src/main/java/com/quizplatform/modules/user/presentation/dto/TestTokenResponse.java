package com.quizplatform.modules.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테스트 토큰 발급 응답")
public class TestTokenResponse {
    
    @Schema(description = "엑세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "엑세스 토큰 만료 시간 (밀리초)", example = "3600000")
    private long expiresIn;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이름", example = "testuser")
    private String username;
    
    @Schema(description = "Swagger UI 'Authorize' 버튼에 바로 복사할 수 있는 인증 헤더 값", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    public String getAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }
    
    @Schema(description = "토큰 발급 후 바로 사용할 수 있는 인증 테스트 링크")
    public String getVerifyTokenUrl() {
        return "/api/test-auth/verify";
    }
    
    @Schema(description = "사용 안내")
    public String getUsageInstructions() {
        return "1. 'Authorize' 버튼을 클릭하세요.\n" +
               "2. 토큰값을 복사해서 입력하세요 (Bearer 접두사 제외).\n" +
               "3. 이제 인증이 필요한 API를 테스트할 수 있습니다.\n" +
               "4. 인증이 성공했는지 확인하려면 " + getVerifyTokenUrl() + " 엔드포인트를 사용하세요.";
    }
}