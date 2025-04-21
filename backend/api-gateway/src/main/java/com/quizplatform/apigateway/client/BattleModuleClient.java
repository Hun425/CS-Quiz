package com.quizplatform.apigateway.client;

import com.quizplatform.core.client.ModuleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Battle 모듈 클라이언트
 * <p>
 * API Gateway에서 Battle 모듈과 통신하기 위한 클라이언트
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BattleModuleClient {

    @Qualifier("battleModuleClient")
    private final ModuleClient client;
    
    @Value("${module.services.battle}")
    private String battleServiceUrl;
    
    /**
     * 배틀 ID로 배틀 정보를 조회합니다.
     *
     * @param battleId 배틀 ID
     * @return 배틀 정보
     */
    public Object getBattleById(String battleId) {
        log.debug("Fetching battle with ID: {} from Battle module", battleId);
        return client.get("/api/v1/battles/" + battleId, Object.class);
    }
    
    /**
     * 현재 활성화된 배틀 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 활성화된 배틀 목록
     */
    public Object getActiveBattles(Pageable pageable) {
        log.debug("Fetching active battles from Battle module");
        
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("size", pageable.getPageSize());
        
        return client.get("/api/v1/battles/active", params, Object.class);
    }
    
    /**
     * 새로운 배틀을 생성합니다.
     *
     * @param battleRequest 배틀 생성 정보
     * @return 생성된 배틀 정보
     */
    public Object createBattle(Object battleRequest) {
        log.debug("Creating battle through Battle module");
        return client.post("/api/v1/battles", battleRequest, Object.class);
    }
    
    /**
     * 배틀 참가 요청을 처리합니다.
     *
     * @param battleId 배틀 ID
     * @param userId 사용자 ID
     * @return 처리 결과
     */
    public Object joinBattle(String battleId, Long userId) {
        log.debug("Joining battle: {} for user: {} through Battle module", battleId, userId);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return client.post("/api/v1/battles/" + battleId + "/join", params, Object.class);
    }
    
    /**
     * 사용자의 배틀 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 배틀 통계
     */
    public Object getUserBattleStats(Long userId) {
        log.debug("Fetching battle stats for user: {} from Battle module", userId);
        return client.get("/api/v1/battles/users/" + userId + "/stats", Object.class);
    }
}