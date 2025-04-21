package com.quizplatform.apigateway.client;

import com.quizplatform.core.client.ModuleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * User 모듈 클라이언트
 * <p>
 * API Gateway에서 User 모듈과 통신하기 위한 클라이언트
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserModuleClient {

    @Qualifier("userModuleClient")
    private final ModuleClient client;
    
    @Value("${module.services.user}")
    private String userServiceUrl;
    
    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public Object getUserById(Long userId) {
        log.debug("Fetching user with ID: {} from User module", userId);
        return client.get("/api/v1/users/" + userId, Object.class);
    }
    
    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 경우 사용자 정보를 포함한 응답
     */
    public Object validateToken(String token) {
        log.debug("Validating token from User module");
        return client.post("/api/v1/users/validate-token?token=" + token, null, Object.class);
    }
}