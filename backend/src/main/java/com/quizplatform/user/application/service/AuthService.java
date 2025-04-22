package com.quizplatform.user.application.service;

import com.quizplatform.user.application.port.in.AuthenticateUserUseCase;
import com.quizplatform.user.application.port.in.command.AuthenticateCommand;
import com.quizplatform.user.application.port.in.dto.AuthenticationResult;
import com.quizplatform.user.application.port.out.LoadUserPort;
// import com.quizplatform.user.application.port.out.TokenProviderPort; // 토큰 생성/관리 Port (별도 정의 필요)
import com.quizplatform.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthenticateUserUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordEncoder passwordEncoder;
    // private final TokenProviderPort tokenProviderPort; // 토큰 제공자 주입
    // 임시 토큰 값 사용 (실제 구현에서는 TokenProviderPort 사용)
    private static final String DUMMY_ACCESS_TOKEN = "dummy-access-token";
    private static final String DUMMY_REFRESH_TOKEN = "dummy-refresh-token";


    @Override
    @Transactional // 로그인 시 lastLogin 업데이트 등을 위해 트랜잭션 사용 가능
    public AuthenticationResult authenticate(AuthenticateCommand command) {
        // 사용자 이름으로 사용자 조회
        User user = loadUserPort.findByUsername(command.getUsername())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + command.getUsername()));

        // 비밀번호 확인
        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            // 실제 구현에서는 구체적인 인증 실패 예외 (예: BadCredentialsException) 사용 권장
            throw new IllegalArgumentException("Invalid password");
        }

        // 계정 활성화 상태 확인 등 추가 검증 가능
        if (!user.isActive()) {
             throw new IllegalStateException("User account is inactive");
        }

        // 토큰 생성 (실제로는 TokenProviderPort 사용)
        // String accessToken = tokenProviderPort.generateAccessToken(user);
        // String refreshToken = tokenProviderPort.generateRefreshToken(user);
        String accessToken = DUMMY_ACCESS_TOKEN + "_for_" + user.getUsername();
        String refreshToken = DUMMY_REFRESH_TOKEN + "_for_" + user.getUsername();
        
        // 마지막 로그인 시간 업데이트 (필요시)
        user.updateLastLogin();
        // saveUserPort.updateUser(user); // User 모델이 상태 변경을 기록하고 UoW 패턴 등으로 처리한다면 명시적 저장은 불필요할 수 있음

        // 인증 결과 반환
        return AuthenticationResult.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
} 