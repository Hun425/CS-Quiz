package com.quizplatform.modules.battle.api.impl;

import com.quizplatform.modules.battle.api.BattleModuleApi;
import com.quizplatform.modules.battle.dto.BattleDetailResponse;
import com.quizplatform.modules.battle.dto.BattleRequest;
import com.quizplatform.modules.battle.dto.BattleResponse;
import com.quizplatform.modules.battle.dto.BattleResultResponse;
import com.quizplatform.modules.battle.service.BattleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배틀 모듈 API 구현체
 * <p>
 * BattleModuleApi 인터페이스를 구현하여 배틀 관련 API를 제공합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BattleModuleApiImpl implements BattleModuleApi {

    private final BattleService battleService;

    @Override
    public ResponseEntity<?> getBattleById(String battleId) {
        log.debug("Fetching battle by ID: {}", battleId);
        BattleDetailResponse battle = battleService.getBattleById(battleId);
        return ResponseEntity.ok(battle);
    }

    @Override
    public ResponseEntity<?> getActiveBattles(Pageable pageable) {
        log.debug("Fetching active battles");
        Page<BattleResponse> activeBattles = battleService.getActiveBattles(pageable);
        return ResponseEntity.ok(activeBattles);
    }

    @Override
    public ResponseEntity<?> createBattle(Object battleDto) {
        log.debug("Creating new battle");
        BattleRequest battleRequest = (BattleRequest) battleDto;
        BattleDetailResponse createdBattle = battleService.createBattle(battleRequest);
        return ResponseEntity.ok(createdBattle);
    }

    @Override
    public ResponseEntity<?> joinBattle(String battleId, Long userId) {
        log.debug("User {} joining battle: {}", userId, battleId);
        battleService.joinBattle(battleId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> leaveBattle(String battleId, Long userId) {
        log.debug("User {} leaving battle: {}", userId, battleId);
        battleService.leaveBattle(battleId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> startBattle(String battleId) {
        log.debug("Starting battle: {}", battleId);
        battleService.startBattle(battleId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> endBattle(String battleId) {
        log.debug("Ending battle: {}", battleId);
        battleService.endBattle(battleId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getBattleResults(String battleId) {
        log.debug("Fetching results for battle: {}", battleId);
        BattleResultResponse battleResults = battleService.getBattleResults(battleId);
        return ResponseEntity.ok(battleResults);
    }

    @Override
    public ResponseEntity<?> getUserBattleHistory(Long userId, Pageable pageable) {
        log.debug("Fetching battle history for user: {}", userId);
        Page<BattleResponse> userBattleHistory = battleService.getUserBattleHistory(userId, pageable);
        return ResponseEntity.ok(userBattleHistory);
    }

    @Override
    public ResponseEntity<?> getUserBattleStats(Long userId) {
        log.debug("Fetching battle stats for user: {}", userId);
        return ResponseEntity.ok(battleService.getUserBattleStats(userId));
    }
}