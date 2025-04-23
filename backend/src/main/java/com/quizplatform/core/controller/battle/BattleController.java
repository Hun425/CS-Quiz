package com.quizplatform.core.controller.battle;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.dto.battle.BattleRoomCreateRequest;
import com.quizplatform.core.dto.battle.BattleRoomResponse;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.service.battle.BattleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * 배틀(퀴즈 대결) 컨트롤러 클래스
 * 
 * <p>배틀방 생성, 조회, 참가, 상태 변경 등 배틀 관련 REST API를 제공합니다.
 * 배틀은 다수의 사용자가 실시간으로 퀴즈를 풀고 경쟁하는 기능입니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
@Tag(name = "Battle Controller", description = "퀴즈 대결 관련 API를 제공합니다.")
public class BattleController {

    /**
     * 배틀 서비스
     */
    private final BattleService battleService;

    /**
     * 배틀방 생성 API
     * 
     * <p>새로운 퀴즈 대결방을 생성합니다. 방 생성자는 자동으로 참가자가 됩니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param request 배틀방 생성 요청 데이터
     * @return 생성된 배틀방 정보
     * @throws BusinessException 인증되지 않은 사용자 또는 잘못된 요청일 경우
     */
    @Operation(summary = "대결방 생성", description = "새로운 퀴즈 대결방을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대결방이 성공적으로 생성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PostMapping
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> createBattleRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody BattleRoomCreateRequest request) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        // creatorId를 요청 객체에 설정
        BattleRoomCreateRequest updateRequest = BattleRoomCreateRequest.builder()
                .quizId(request.getQuizId())
                .maxParticipants(request.getMaxParticipants())
                .creatorId(userPrincipal.getUser().getId())
                .build();
                
        BattleRoomResponse battleRoom = battleService.createBattleRoom(
                userPrincipal.getUser(),
                updateRequest.getQuizId(),
                updateRequest.getMaxParticipants()
        );

        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 배틀방 조회 API
     * 
     * <p>특정 ID의 배틀방 정보를 조회합니다.</p>
     * 
     * @param roomId 조회할 배틀방 ID
     * @return 배틀방 정보
     */
    @Operation(summary = "대결방 조회", description = "특정 대결방의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대결방 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "대결방을 찾을 수 없습니다.")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> getBattleRoom(
            @Parameter(description = "대결방 ID") @PathVariable Long roomId) {

        BattleRoomResponse battleRoom = battleService.getBattleRoom(roomId);
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 활성화된 배틀방 목록 조회 API
     * 
     * <p>현재 대기 중인(WAITING) 상태의 배틀방 목록을 조회합니다.</p>
     * 
     * @return 활성화된 배틀방 목록
     */
    @Operation(summary = "활성화된 대결방 목록 조회", description = "현재 활성화된 대결방 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대결방 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping("/active")
    public ResponseEntity<CommonApiResponse<List<BattleRoomResponse>>> getActiveBattleRooms() {
        List<BattleRoomResponse> battleRooms = battleService.getBattleRoomsByStatus(com.quizplatform.core.domain.battle.BattleRoomStatus.WAITING);
        return ResponseEntity.ok(CommonApiResponse.success(battleRooms));
    }

    /**
     * 배틀방 참가 API
     * 
     * <p>특정 배틀방에 참가합니다. 배틀방은 WAITING 상태여야 합니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param roomId 참가할 배틀방 ID
     * @return 참가 후 배틀방 정보
     * @throws BusinessException 인증되지 않은 사용자 또는 방 참가 불가 상태일 경우
     */
    @Operation(summary = "대결방 참가", description = "특정 대결방에 참가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대결방에 성공적으로 참가했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "대결방을 찾을 수 없습니다.")
    })
    @PostMapping("/{roomId}/join")
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> joinBattleRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "대결방 ID") @PathVariable Long roomId) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        BattleRoomResponse battleRoom = battleService.joinBattleRoom(roomId, userPrincipal.getUser());
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 배틀 준비 상태 토글 API
     * 
     * <p>배틀방에서 사용자의 준비 상태를 전환합니다.
     * 모든 참가자가 준비 완료되면 배틀이 자동으로 시작됩니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param roomId 배틀방 ID
     * @return 상태 변경 후 배틀방 정보
     * @throws BusinessException 인증되지 않은 사용자 또는 상태 변경 불가 상태일 경우
     */
    @Operation(summary = "대결방 준비 상태 토글", description = "대결방에서 사용자의 준비 상태를 토글합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "준비 상태가 성공적으로 토글되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "대결방 또는 참가자를 찾을 수 없습니다.")
    })
    @PostMapping("/{roomId}/ready")
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> toggleReady(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "대결방 ID") @PathVariable Long roomId) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        BattleRoomResponse battleRoom = battleService.toggleReady(roomId, userPrincipal.getUser());
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 배틀방 나가기 API
     * 
     * <p>현재 참가 중인 배틀방을 나갑니다. 
     * 배틀이 이미 시작된 경우 패배 처리됩니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @param roomId 나갈 배틀방 ID
     * @return 퇴장 후 배틀방 정보
     * @throws BusinessException 인증되지 않은 사용자 또는 퇴장 불가 상태일 경우
     */
    @Operation(summary = "대결방 나가기", description = "현재 참가 중인 대결방을 나갑니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대결방에서 성공적으로 나갔습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "404", description = "대결방 또는 참가자를 찾을 수 없습니다.")
    })
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> leaveBattleRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "대결방 ID") @PathVariable Long roomId) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        BattleRoomResponse battleRoom = battleService.leaveBattleRoom(roomId, userPrincipal.getUser());
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 배틀방 참가자 목록 조회 API
     * 
     * <p>특정 배틀방의 현재 참가자 목록을 조회합니다. 
     * 웹소켓을 통해 받는 정보와 동일한 형식의 데이터를 제공합니다.</p>
     * 
     * @param roomId 조회할 배틀방 ID
     * @return 참가자 목록 정보
     * @throws BusinessException 방을 찾을 수 없거나 참가자가 없는 경우
     */
    @Operation(summary = "대결방 참가자 목록 조회", description = "특정 대결방의 현재 참가자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "대결방을 찾을 수 없습니다.")
    })
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<CommonApiResponse<com.quizplatform.core.dto.battle.BattleJoinResponse>> getBattleParticipants(
            @Parameter(description = "대결방 ID") @PathVariable Long roomId) {
            
        com.quizplatform.core.dto.battle.BattleJoinResponse participants = battleService.getCurrentBattleParticipants(roomId);
        return ResponseEntity.ok(CommonApiResponse.success(participants));
    }

    /**
     * 내 활성 배틀방 조회 API
     * 
     * <p>현재 사용자가 참가 중인 활성 배틀방을 조회합니다.
     * 한 사용자는 한 번에 하나의 배틀방에만 참가할 수 있습니다.</p>
     * 
     * @param userPrincipal 인증된 사용자 정보
     * @return 사용자의 활성 배틀방 정보 또는 빈 배열
     * @throws BusinessException 인증되지 않은 사용자일 경우
     */
    @Operation(summary = "내 활성 대결방 조회", description = "현재 사용자가 참가 중인 활성 대결방을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 대결방 정보가 성공적으로 조회되었습니다.")
    })
    @GetMapping("/my-active")
    public ResponseEntity<CommonApiResponse<Object>> getMyActiveBattleRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        BattleRoomResponse battleRoom = battleService.getActiveBattleRoomByUser(userPrincipal.getUser());
        
        // 활성 대결방이 없으면 빈 배열 반환
        if (battleRoom == null) {
            return ResponseEntity.ok(CommonApiResponse.success(new ArrayList<>()));
        }
        
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }
}