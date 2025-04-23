package com.quizplatform.battle.application.controller;

import com.quizplatform.battle.application.service.BattleService;
import com.quizplatform.battle.domain.model.BattleAnswer;
import com.quizplatform.battle.domain.model.BattleRoom;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 배틀 관련 API 요청을 처리하는 컨트롤러 클래스
 */
@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
@Slf4j
public class BattleController {

    private final BattleService battleService;
    
    /**
     * 새 배틀방 생성 API
     * 
     * @param request 배틀방 생성 요청 데이터
     * @return 생성된 배틀방 정보
     */
    @PostMapping
    public ResponseEntity<?> createBattleRoom(@RequestBody Map<String, Object> request) {
        try {
            Long quizId = Long.valueOf(request.get("quizId").toString());
            int maxParticipants = Integer.parseInt(request.get("maxParticipants").toString());
            Long creatorId = Long.valueOf(request.get("creatorId").toString());
            String creatorUsername = (String) request.get("creatorUsername");
            String creatorProfileImage = (String) request.get("profileImage");
            int totalQuestions = Integer.parseInt(request.get("totalQuestions").toString());
            Integer questionTimeLimitSeconds = request.containsKey("questionTimeLimit") ? 
                    Integer.valueOf(request.get("questionTimeLimit").toString()) : 30;
            
            BattleRoom battleRoom = battleService.createBattleRoom(
                    quizId, maxParticipants, creatorId, creatorUsername, 
                    creatorProfileImage, totalQuestions, questionTimeLimitSeconds);
            
            return ResponseEntity.ok(battleRoom);
        } catch (Exception e) {
            log.error("배틀방 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 배틀방 상세 정보 조회 API
     * 
     * @param roomId 조회할 배틀방 ID
     * @return 배틀방 상세 정보
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getBattleRoom(@PathVariable Long roomId) {
        try {
            BattleRoom battleRoom = battleService.getBattleRoom(roomId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("배틀방 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 배틀방 목록 조회 API
     * 
     * @param status 조회할 배틀방 상태 (WAITING, IN_PROGRESS, FINISHED)
     * @return 배틀방 목록
     */
    @GetMapping
    public ResponseEntity<List<BattleRoom>> getBattleRooms(
            @RequestParam(required = false, defaultValue = "WAITING") String status) {
        try {
            BattleRoomStatus roomStatus = BattleRoomStatus.valueOf(status);
            List<BattleRoom> rooms = battleService.getBattleRoomsByStatus(roomStatus);
            return ResponseEntity.ok(rooms);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 상태 값: {}", status);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 배틀방 참가 API
     * 
     * @param roomId 참가할 배틀방 ID
     * @param request 참가 요청 데이터
     * @return 업데이트된 배틀방 정보
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinBattleRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String username = (String) request.get("username");
            String profileImage = (String) request.get("profileImage");
            
            BattleRoom battleRoom = battleService.joinBattleRoom(roomId, userId, username, profileImage);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("배틀방 참가 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 참가자 준비 상태 토글 API
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방 정보
     */
    @PostMapping("/{roomId}/ready/{userId}")
    public ResponseEntity<?> toggleReady(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        try {
            BattleRoom battleRoom = battleService.toggleReady(roomId, userId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("준비 상태 변경 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 배틀방 퇴장 API
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방 정보
     */
    @PostMapping("/{roomId}/leave/{userId}")
    public ResponseEntity<?> leaveBattleRoom(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        try {
            BattleRoom battleRoom = battleService.leaveBattleRoom(roomId, userId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("배틀방 퇴장 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 배틀 시작 API
     * 
     * @param roomId 배틀방 ID
     * @return 시작된 배틀방 정보
     */
    @PostMapping("/{roomId}/start")
    public ResponseEntity<?> startBattle(@PathVariable Long roomId) {
        try {
            BattleRoom battleRoom = battleService.startBattle(roomId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("배틀 시작 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 다음 문제 진행 API
     * 
     * @param roomId 배틀방 ID
     * @return 업데이트된 배틀방 정보
     */
    @PostMapping("/{roomId}/next-question")
    public ResponseEntity<?> startNextQuestion(@PathVariable Long roomId) {
        try {
            BattleRoom battleRoom = battleService.startNextQuestion(roomId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("다음 문제 진행 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 문제 답변 처리 API
     * 
     * @param roomId 배틀방 ID
     * @param request 답변 요청 데이터
     * @return 처리된 답변 정보
     */
    @PostMapping("/{roomId}/answer")
    public ResponseEntity<?> processAnswer(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            int questionIndex = Integer.parseInt(request.get("questionIndex").toString());
            String answer = (String) request.get("answer");
            boolean isCorrect = Boolean.parseBoolean(request.get("isCorrect").toString());
            long answerTime = Long.parseLong(request.get("answerTimeMs").toString());
            
            BattleAnswer battleAnswer = battleService.processAnswer(
                    roomId, userId, questionIndex, answer, isCorrect, answerTime);
            
            return ResponseEntity.ok(battleAnswer);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("답변 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 배틀 종료 API
     * 
     * @param roomId 배틀방 ID
     * @return 종료된 배틀방 정보
     */
    @PostMapping("/{roomId}/finish")
    public ResponseEntity<?> finishBattle(@PathVariable Long roomId) {
        try {
            BattleRoom battleRoom = battleService.finishBattle(roomId);
            return ResponseEntity.ok(battleRoom);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("배틀 종료 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 