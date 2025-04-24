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

import java.util.List;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
@Tag(name = "Battle Controller", description = "퀴즈 대결 관련 API를 제공합니다.")
public class BattleController {

    private final BattleService battleService;

    /**
     * 새로운 대결방 생성
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

        BattleRoomResponse battleRoom = battleService.createBattleRoom(
                userPrincipal.getUser(),
                request.getQuizId(),
                request.getMaxParticipants()
        );

        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }

    /**
     * 대결방 조회
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
     * 활성화된 대결방 목록 조회
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
     * 대결방 참가
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
     * 대결방 준비 상태 토글
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
     * 대결방 나가기
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
     * 내 활성 대결방 조회
     */
    @Operation(summary = "내 활성 대결방 조회", description = "현재 사용자가 참가 중인 활성 대결방을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 대결방 정보가 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "활성 대결방이 없습니다.")
    })
    @GetMapping("/my-active")
    public ResponseEntity<CommonApiResponse<BattleRoomResponse>> getMyActiveBattleRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 사용자 인증 확인
        if (userPrincipal == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "인증이 필요합니다.");
        }

        BattleRoomResponse battleRoom = battleService.getActiveBattleRoomByUser(userPrincipal.getUser());
        return ResponseEntity.ok(CommonApiResponse.success(battleRoom));
    }
}