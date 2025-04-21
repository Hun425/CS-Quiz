package com.quizplatform.modules.battle.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 배틀 모듈 API 인터페이스
 * <p>
 * 배틀 모듈이 다른 모듈에 제공하는 API를 정의합니다.
 * </p>
 */
@RequestMapping("/api/v1/battles")
public interface BattleModuleApi {

    /**
     * 배틀 ID로 배틀 정보를 조회합니다.
     *
     * @param battleId 배틀 ID
     * @return 배틀 정보
     */
    @GetMapping("/{battleId}")
    ResponseEntity<?> getBattleById(@PathVariable String battleId);

    /**
     * 현재 활성화된 배틀 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 활성화된 배틀 목록
     */
    @GetMapping("/active")
    ResponseEntity<?> getActiveBattles(Pageable pageable);

    /**
     * 새로운 배틀을 생성합니다.
     *
     * @param battleDto 배틀 생성 정보
     * @return 생성된 배틀 정보
     */
    @PostMapping
    ResponseEntity<?> createBattle(@RequestBody Object battleDto);

    /**
     * 배틀 참가 요청을 처리합니다.
     *
     * @param battleId 배틀 ID
     * @param userId 사용자 ID
     * @return 참가 성공 여부
     */
    @PostMapping("/{battleId}/join")
    ResponseEntity<?> joinBattle(@PathVariable String battleId, @RequestParam Long userId);

    /**
     * 배틀 퇴장 요청을 처리합니다.
     *
     * @param battleId 배틀 ID
     * @param userId 사용자 ID
     * @return 퇴장 성공 여부
     */
    @PostMapping("/{battleId}/leave")
    ResponseEntity<?> leaveBattle(@PathVariable String battleId, @RequestParam Long userId);

    /**
     * 배틀 시작 요청을 처리합니다.
     *
     * @param battleId 배틀 ID
     * @return 시작 성공 여부
     */
    @PostMapping("/{battleId}/start")
    ResponseEntity<?> startBattle(@PathVariable String battleId);

    /**
     * 배틀 종료 요청을 처리합니다.
     *
     * @param battleId 배틀 ID
     * @return 종료 성공 여부
     */
    @PostMapping("/{battleId}/end")
    ResponseEntity<?> endBattle(@PathVariable String battleId);

    /**
     * 배틀 결과를 조회합니다.
     *
     * @param battleId 배틀 ID
     * @return 배틀 결과
     */
    @GetMapping("/{battleId}/results")
    ResponseEntity<?> getBattleResults(@PathVariable String battleId);

    /**
     * 사용자의 배틀 참여 기록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 사용자의 배틀 참여 기록
     */
    @GetMapping("/users/{userId}/history")
    ResponseEntity<?> getUserBattleHistory(@PathVariable Long userId, Pageable pageable);

    /**
     * 사용자의 배틀 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 배틀 통계
     */
    @GetMapping("/users/{userId}/stats")
    ResponseEntity<?> getUserBattleStats(@PathVariable Long userId);
}