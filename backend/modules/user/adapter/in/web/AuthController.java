package com.quizplatform.user.adapter.in.web;

import com.quizplatform.user.application.port.in.AuthenticateUserUseCase;
import com.quizplatform.user.application.port.in.RegisterUserUseCase;
import com.quizplatform.user.application.port.in.command.AuthenticateCommand;
import com.quizplatform.user.application.port.in.command.RegisterUserCommand;
import com.quizplatform.user.application.port.in.dto.AuthenticationResult;
import com.quizplatform.user.domain.model.User; // 등록 결과로 User 모델 반환 예시
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    /**
     * 사용자 등록 API
     * @param command 등록 정보 (username, password, email)
     * @return 생성된 사용자 정보 (간략화 또는 ID 만 반환 가능)
     */
    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> registerUser(@Valid @RequestBody RegisterUserCommand command) {
        User registeredUser = registerUserUseCase.registerUser(command);
        // 실제 반환 타입은 요구사항에 따라 조정 (예: ID만 반환, 성공 메시지만 반환 등)
        UserRegistrationResponse response = new UserRegistrationResponse(registeredUser.getId(), registeredUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     * @param command 로그인 정보 (username, password)
     * @return 인증 결과 (사용자 정보 일부, Access Token, Refresh Token 등)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResult> login(@Valid @RequestBody AuthenticateCommand command) {
        AuthenticationResult result = authenticateUserUseCase.authenticate(command);
        return ResponseEntity.ok(result);
    }

    // --- Helper DTO for Response ---
    // 실제로는 별도 파일로 분리하는 것이 좋음
    @Getter
    private static class UserRegistrationResponse {
        private final Long userId;
        private final String username;

        public UserRegistrationResponse(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }
} 