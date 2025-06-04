package com.quizplatform.battle.adapter.in.web;

import com.quizplatform.battle.application.service.BattleServiceAdapter;
import com.quizplatform.battle.domain.model.BattleAnswer;
import com.quizplatform.battle.domain.model.BattleRoom;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.common.auth.CurrentUser;
import com.quizplatform.common.auth.CurrentUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 배틀 관련 API 요청을 처리하는 컨트롤러 클래스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Battle Controller", description = "퀴즈 대결 관련 API를 제공합니다")
@SecurityRequirement(name = "bearerAuth")
public class BattleController {

    private final BattleServiceAdapter battleService;
    
    /**
     * 새 배틀방 생성 API
     * 
     * @param request 배틀방 생성 요청 데이터
     * @return 생성된 배틀방 정보
     */
    @Operation(summary = "배틀방 생성", description = "새로운 퀴즈 대결방을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀방이 성공적으로 생성되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다.")
    })
    @PostMapping
    public ResponseEntity<BattleRoom> createBattleRoom(
            @Parameter(description = "배틀방 생성 요청 데이터", required = true)
            @RequestBody Map<String, Object> request,
            @CurrentUser CurrentUserInfo currentUser) {
        Long quizId = Long.valueOf(request.get("quizId").toString());
        int maxParticipants = Integer.parseInt(request.get("maxParticipants").toString());
        Long creatorId = currentUser.id();
        String creatorUsername = (String) request.get("creatorUsername");
        String creatorProfileImage = (String) request.get("profileImage");
        int totalQuestions = Integer.parseInt(request.get("totalQuestions").toString());
        Integer questionTimeLimitSeconds = request.containsKey("questionTimeLimit") ?
                Integer.valueOf(request.get("questionTimeLimit").toString()) : 30;

        BattleRoom battleRoom = battleService.createBattleRoom(
                quizId, maxParticipants, creatorId, creatorUsername,
                creatorProfileImage, totalQuestions, questionTimeLimitSeconds);

        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 배틀방 상세 정보 조회 API
     * 
     * @param roomId 조회할 배틀방 ID
     * @return 배틀방 상세 정보
     */
    @Operation(summary = "배틀방 조회", description = "특정 배틀방의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀방 정보가 성공적으로 조회되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다.")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getBattleRoom(
            @Parameter(description = "조회할 배틀방의 ID", required = true)
            @PathVariable Long roomId) {
        BattleRoom battleRoom = battleService.getBattleRoom(roomId);
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 배틀방 목록 조회 API
     * 
     * @param status 조회할 배틀방 상태 (WAITING, IN_PROGRESS, FINISHED)
     * @return 배틀방 목록
     */
    @Operation(summary = "배틀방 목록 조회", description = "특정 상태의 배틀방 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀방 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 값입니다.")
    })
    @GetMapping
    public ResponseEntity<List<BattleRoom>> getBattleRooms(
            @Parameter(description = "조회할 배틀방 상태 (WAITING, IN_PROGRESS, FINISHED)", example = "WAITING")
            @RequestParam(required = false, defaultValue = "WAITING") String status) {
        BattleRoomStatus roomStatus;
        try {
            roomStatus = BattleRoomStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION);
        }
        List<BattleRoom> rooms = battleService.getBattleRoomsByStatus(roomStatus);
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * 배틀방 참가 API
     * 
     * @param roomId 참가할 배틀방 ID
     * @param request 참가 요청 데이터
     * @return 업데이트된 배틀방 정보
     */
    @Operation(summary = "배틀방 참가", description = "특정 배틀방에 참가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀방 참가가 성공적으로 처리되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "참가 요청 처리 중 오류가 발생했습니다.")
    })
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinBattleRoom(
            @Parameter(description = "참가할 배틀방의 ID", required = true)
            @PathVariable Long roomId,
            @Parameter(description = "참가 요청 데이터", required = true)
            @RequestBody Map<String, Object> request,
            @CurrentUser CurrentUserInfo currentUser) {
        Long userId = currentUser.id();
        String username = (String) request.get("username");
        String profileImage = (String) request.get("profileImage");

        BattleRoom battleRoom = battleService.joinBattleRoom(roomId, userId, username, profileImage);
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 참가자 준비 상태 토글 API
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방 정보
     */
    @Operation(summary = "준비 상태 토글", description = "배틀방 참가자의 준비 상태를 전환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "준비 상태가 성공적으로 변경되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방 또는 참가자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "준비 상태 변경 중 오류가 발생했습니다.")
    })
    @PostMapping("/{roomId}/ready")
    public ResponseEntity<?> toggleReady(
            @Parameter(description = "배틀방 ID", required = true)
            @PathVariable Long roomId,
            @CurrentUser CurrentUserInfo currentUser) {
        BattleRoom battleRoom = battleService.toggleReady(roomId, currentUser.id());
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 배틀방 퇴장 API
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방 정보
     */
    @Operation(summary = "배틀방 퇴장", description = "배틀방에서 퇴장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀방 퇴장이 성공적으로 처리되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방 또는 참가자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "퇴장 처리 중 오류가 발생했습니다.")
    })
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveBattleRoom(
            @Parameter(description = "배틀방 ID", required = true)
            @PathVariable Long roomId,
            @CurrentUser CurrentUserInfo currentUser) {
        BattleRoom battleRoom = battleService.leaveBattleRoom(roomId, currentUser.id());
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 배틀 시작 API
     * 
     * @param roomId 배틀방 ID
     * @return 시작된 배틀방 정보
     */
    @Operation(summary = "배틀 시작", description = "배틀을 시작합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀이 성공적으로 시작되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "배틀 시작 조건이 충족되지 않았습니다.")
    })
    @PostMapping("/{roomId}/start")
    public ResponseEntity<?> startBattle(
            @Parameter(description = "시작할 배틀방 ID", required = true)
            @PathVariable Long roomId) {
        BattleRoom battleRoom = battleService.startBattle(roomId);
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 다음 문제 진행 API
     * 
     * @param roomId 배틀방 ID
     * @return 업데이트된 배틀방 정보
     */
    @Operation(summary = "다음 문제 진행", description = "배틀에서 다음 문제로 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다음 문제로 성공적으로 진행되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "다음 문제 진행 조건이 충족되지 않았습니다.")
    })
    @PostMapping("/{roomId}/next-question")
    public ResponseEntity<?> startNextQuestion(
            @Parameter(description = "배틀방 ID", required = true)
            @PathVariable Long roomId) {
        BattleRoom battleRoom = battleService.startNextQuestion(roomId);
        return ResponseEntity.ok(battleRoom);
    }
    
    /**
     * 문제 답변 처리 API
     * 
     * @param roomId 배틀방 ID
     * @param request 답변 요청 데이터
     * @return 처리된 답변 정보
     */
    @Operation(summary = "문제 답변 처리", description = "사용자가 제출한 배틀 문제 답변을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변이 성공적으로 처리되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleAnswer.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "답변 처리 중 오류가 발생했습니다.")
    })
    @PostMapping("/{roomId}/answer")
    public ResponseEntity<?> processAnswer(
            @Parameter(description = "배틀방 ID", required = true)
            @PathVariable Long roomId,
            @Parameter(description = "답변 요청 데이터", required = true)
            @RequestBody Map<String, Object> request,
            @CurrentUser CurrentUserInfo currentUser) {
        Long userId = currentUser.id();
        int questionIndex = Integer.parseInt(request.get("questionIndex").toString());
        String answer = (String) request.get("answer");
        boolean isCorrect = Boolean.parseBoolean(request.get("isCorrect").toString());
        long answerTime = Long.parseLong(request.get("answerTimeMs").toString());

        Object result = battleService.processAnswer(
                roomId, userId, questionIndex, answer, isCorrect, answerTime);

        return ResponseEntity.ok(result);
    }
    
    /**
     * 배틀 종료 API
     * 
     * @param roomId 배틀방 ID
     * @return 종료된 배틀방 정보
     */
    @Operation(summary = "배틀 종료", description = "배틀을 종료하고 결과를 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배틀이 성공적으로 종료되었습니다.",
                    content = @Content(schema = @Schema(implementation = BattleRoom.class))),
            @ApiResponse(responseCode = "404", description = "배틀방을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "배틀 종료 처리 중 오류가 발생했습니다.")
    })
    @PostMapping("/{roomId}/finish")
    public ResponseEntity<?> finishBattle(
            @Parameter(description = "종료할 배틀방 ID", required = true)
            @PathVariable Long roomId) {
        BattleRoom battleRoom = battleService.finishBattle(roomId);
        return ResponseEntity.ok(battleRoom);
    }
}
