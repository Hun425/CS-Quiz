package com.quizplatform.core.controller.auth;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.user.TestTokenRequest;
import com.quizplatform.core.dto.user.TestTokenResponse;
import com.quizplatform.core.dto.user.UserProfileDto;
import com.quizplatform.core.service.TestAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-auth")
@RequiredArgsConstructor
@Tag(name = "테스트 인증 API", description = "개발 및 테스트용 JWT 토큰 발급 및 테스트 API")
public class TestAuthController {

    private final TestAuthService testAuthService;

    @Operation(
            summary = "테스트 JWT 토큰 발급", 
            description = "사용자 이름을 입력하면 해당 사용자로 로그인된 JWT 토큰을 발급합니다. 개발 및 테스트 환경에서만 사용하세요.\n\n" +
                    "발급받은 토큰은 Swagger UI 우측 상단의 'Authorize' 버튼을 클릭하여 입력합니다.\n" +
                    "토큰값만 입력하세요(Bearer 접두사 없이). Swagger UI가 자동으로 Bearer 접두사를 추가합니다.\n\n" +
                    "참고: 토큰은 서버 설정에 따라 일정 시간(기본 1시간) 후 만료됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = TestTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/token")
    public ResponseEntity<CommonApiResponse<TestTokenResponse>> generateTestToken(
            @Parameter(description = "테스트 토큰 발급 요청 정보", required = true)
            @RequestBody TestTokenRequest request) {
        TestTokenResponse tokenResponse = testAuthService.generateTestToken(request.getUsername());
        return ResponseEntity.ok(CommonApiResponse.success(tokenResponse));
    }
    
    @Operation(
            summary = "인증 토큰 검증", 
            description = "현재 인증된 토큰의 유효성을 확인합니다. 이 API가 성공적으로 호출되면 토큰이 유효하고 인증이 정상적으로 작동하는 것입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 또는 잘못된 토큰")
    })
    @GetMapping("/verify")
    public ResponseEntity<CommonApiResponse<String>> verifyToken(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(CommonApiResponse.success(
                principal.getUsername() + " 사용자의 인증이 유효합니다. (User ID: " + principal.getId() + ")"
        ));
    }
    
    @Operation(
            summary = "인증 정보 확인", 
            description = "현재 인증된 사용자의 정보를 반환합니다. 이 API를 통해 토큰이 제대로 동작하는지 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<UserPrincipal>> getAuthenticatedUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(CommonApiResponse.success(principal));
    }
}