package com.quizplatform.core.controller.battle;

import com.quizplatform.core.dto.battle.*;
import com.quizplatform.core.service.battle.BattleService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class BattleWebSocketController {
    private final BattleService battleService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 대결방 입장 처리
     * 클라이언트: /app/battle/join으로 메시지 전송
     */
    @MessageMapping("/battle/join")
    public void joinBattle(
            BattleJoinRequest request,
            @Header("simpSessionId") String sessionId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 세션에 사용자 정보 저장
        headerAccessor.getSessionAttributes().put("userId", request.getUserId());
        headerAccessor.getSessionAttributes().put("roomId", request.getRoomId());

        // 대결방 입장 처리
        BattleJoinResponse response = battleService.joinBattle(request, sessionId);

        // 대결방의 모든 참가자에게 새로운 참가자 알림
        messagingTemplate.convertAndSend(
                "/topic/battle/" + request.getRoomId() + "/participants",
                response
        );

        // 대결 시작 조건 확인
        if (battleService.isReadyToStart(request.getRoomId())) {
            startBattle(request.getRoomId());
        }
    }

    /**
     * 답변 제출 처리
     * 클라이언트: /app/battle/answer로 메시지 전송
     */
    @MessageMapping("/battle/answer")
    public void submitAnswer(
            BattleAnswerRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        // 답변 처리 및 결과 계산
        BattleAnswerResponse response = battleService.processAnswer(request, sessionId);

        // 개인 결과 전송
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/battle/result",
                response
        );

        // 전체 진행 상황 업데이트
        BattleProgressResponse progress = battleService.getBattleProgress(request.getRoomId());
        messagingTemplate.convertAndSend(
                "/topic/battle/" + request.getRoomId() + "/progress",
                progress
        );

        // 모든 참가자가 답변했는지 확인
        if (battleService.allParticipantsAnswered(request.getRoomId())) {
            moveToNextQuestion(request.getRoomId());
        }
    }

    /**
     * 다음 문제로 진행
     */
    private void moveToNextQuestion(Long roomId) {
        BattleNextQuestionResponse response = battleService.prepareNextQuestion(roomId);

        if (response.isGameOver()) {
            // 게임 종료 처리
            endBattle(roomId);
        } else {
            // 다음 문제 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/question",
                    response
            );
        }
    }

    /**
     * 대결 시작
     */
    private void startBattle(Long roomId) {
        BattleStartResponse response = battleService.startBattle(roomId);

        // 대결 시작 알림 전송
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/start",
                response
        );
    }

    /**
     * 대결 종료
     */
    private void endBattle(Long roomId) {
        BattleEndResponse response = battleService.endBattle(roomId);

        // 최종 결과 전송
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/end",
                response
        );
    }

    @MessageMapping("/battle/leave")
    public void leaveBattle(
            BattleLeaveRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        // 대결방 나가기 처리
        BattleLeaveResponse response = battleService.leaveBattle(request, sessionId);

        // 대결방의 모든 참가자에게 나가기 알림
        messagingTemplate.convertAndSend(
                "/topic/battle/" + request.getRoomId() + "/participants",
                response
        );

        // 방 상태 확인
        if (!battleService.isValidBattleRoom(request.getRoomId())) {
            // 방이 유효하지 않으면 해당 방에 대한 상태 변경 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/status",
                    new BattleRoomStatusChangeResponse(
                            request.getRoomId(),
                            response.getStatus() // BattleStatus 사용
                    )
            );
        }
    }
}