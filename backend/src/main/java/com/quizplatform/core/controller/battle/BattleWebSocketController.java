package com.quizplatform.core.controller.battle;

import com.quizplatform.core.dto.battle.*;
import com.quizplatform.core.service.battle.BattleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BattleWebSocketController {
    private final BattleService battleService;
    private final SimpMessagingTemplate messagingTemplate;

    // 답변 제출 중복 방지를 위한 Map (방ID와 문제 인덱스 추적)
    private final Map<Long, Integer> roomQuestionIndexMap = new ConcurrentHashMap<>();

    // 중복 문제 진행 방지를 위한 Map (방ID와 마지막 처리 타임스탬프)
    private final Map<Long, Long> lastQuestionProcessingMap = new ConcurrentHashMap<>();

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

        log.info("배틀 입장 요청: roomId={}, userId={}, sessionId={}", request.getRoomId(), request.getUserId(), sessionId);

        try {
            // 대결방 입장 처리
            BattleJoinResponse response = battleService.joinBattle(request, sessionId);

            // 대결방의 모든 참가자에게 새로운 참가자 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/participants",
                    response
            );

            log.info("배틀 입장 처리 완료: roomId={}, userId={}, 참가자 수={}",
                    request.getRoomId(), request.getUserId(), response.getParticipants().size());

            // 대결 시작 조건 확인
            if (battleService.isReadyToStart(request.getRoomId())) {
                startBattle(request.getRoomId());
            }
        } catch (Exception e) {
            log.error("배틀 입장 처리 중 오류 발생: roomId={}, userId={}", request.getRoomId(), request.getUserId(), e);
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
        log.info("답변 제출 요청: roomId={}, questionId={}, sessionId={}",
                request.getRoomId(), request.getQuestionId(), sessionId);

        try {
            // 답변 처리 및 결과 계산
            BattleAnswerResponse response = battleService.processAnswer(request, sessionId);

            // 개인 결과 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/battle/result",
                    response
            );

            log.info("개인 결과 전송 완료: roomId={}, questionId={}, 정답여부={}",
                    request.getRoomId(), request.getQuestionId(), response.isCorrect());

            // 전체 진행 상황 업데이트
            BattleProgressResponse progress = battleService.getBattleProgress(request.getRoomId());
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/progress",
                    progress
            );

            log.info("진행 상황 업데이트 전송 완료: roomId={}, 현재문제={}/{}",
                    request.getRoomId(), progress.getCurrentQuestionIndex() + 1, progress.getTotalQuestions());

            // 모든 참가자가 답변했는지 확인 - 중복 진행 방지 로직 추가
            if (battleService.allParticipantsAnswered(request.getRoomId())) {
                // 마지막 처리 시간 확인 (1초 내 중복 처리 방지)
                long currentTime = System.currentTimeMillis();
                long lastProcessingTime = lastQuestionProcessingMap.getOrDefault(request.getRoomId(), 0L);

                if (currentTime - lastProcessingTime > 1000) {
                    // 1초 이상 경과한 경우에만 다음 문제로 진행
                    lastQuestionProcessingMap.put(request.getRoomId(), currentTime);
                    moveToNextQuestion(request.getRoomId());
                } else {
                    log.info("중복 문제 진행 시도 방지: roomId={}, 마지막처리={}, 현재시간={}",
                            request.getRoomId(), lastProcessingTime, currentTime);
                }
            }
        } catch (Exception e) {
            log.error("답변 제출 처리 중 오류 발생: roomId={}, questionId={}",
                    request.getRoomId(), request.getQuestionId(), e);
        }
    }

    /**
     * 다음 문제로 진행
     */
    private void moveToNextQuestion(Long roomId) {
        log.info("다음 문제 준비: roomId={}", roomId);

        try {
        // 현재 인덱스 기록 (중복 방지용)
            Integer currentIndex = battleService.getBattleProgress(roomId).getCurrentQuestionIndex();
            Integer recordedIndex = roomQuestionIndexMap.get(roomId);

            if (recordedIndex != null && recordedIndex > currentIndex) {
                // 더 높은 인덱스를 기록한 경우(인덱스 역전)만 중복으로 간주
                log.warn("중복 문제 진행 감지(인덱스 역전): roomId={}, 기록인덱스={}, 현재인덱스={}",
                        roomId, recordedIndex, currentIndex);
                return;
            }

        // 현재 인덱스를 처리 중으로 표시
            roomQuestionIndexMap.put(roomId, currentIndex);

        // 다음 문제 준비
            BattleNextQuestionResponse response = battleService.prepareNextQuestion(roomId);

        // 다음 문제 인덱스로 업데이트
            if (!response.isGameOver()) {
                roomQuestionIndexMap.put(roomId, currentIndex + 1);
            } else {
                // 다음 문제 전송
                log.info("다음 문제 전송: roomId={}, questionId={}, 마지막문제={}",
                        roomId, response.getQuestionId(), response.isLastQuestion());
                messagingTemplate.convertAndSend(
                        "/topic/battle/" + roomId + "/question",
                        response
                );
            }
        } catch (Exception e) {
            log.error("다음 문제 준비 중 오류 발생: roomId={}", roomId, e);
        }
    }

    /**
     * 대결 시작
     */
    private void startBattle(Long roomId) {
        log.info("배틀 시작: roomId={}", roomId);

        try {
            // 문제 인덱스 추적 초기화
            roomQuestionIndexMap.put(roomId, 0);

            BattleStartResponse response = battleService.startBattle(roomId);

            // 대결 시작 알림 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/start",
                    response
            );

            log.info("배틀 시작 알림 전송 완료: roomId={}, 총문제수={}", roomId, response.getTotalQuestions());
        } catch (Exception e) {
            log.error("배틀 시작 처리 중 오류 발생: roomId={}", roomId, e);
        }
    }

    /**
     * 대결 종료
     */
    private void endBattle(Long roomId) {
        log.info("배틀 종료: roomId={}", roomId);

        try {
            BattleEndResponse response = battleService.endBattle(roomId);

            // 최종 결과 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/end",
                    response
            );

            // 추적 데이터 정리
            roomQuestionIndexMap.remove(roomId);
            lastQuestionProcessingMap.remove(roomId);

            log.info("배틀 종료 결과 전송 완료: roomId={}, 참가자수={}", roomId, response.getResults().size());
        } catch (Exception e) {
            log.error("배틀 종료 처리 중 오류 발생: roomId={}", roomId, e);
        }
    }

    @MessageMapping("/battle/leave")
    public void leaveBattle(
            BattleLeaveRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        log.info("배틀 나가기 요청: roomId={}, userId={}, sessionId={}",
                request.getRoomId(), request.getUserId(), sessionId);

        try {
            // 대결방 나가기 처리
            BattleLeaveResponse response = battleService.leaveBattle(request, sessionId);

            // 대결방의 모든 참가자에게 나가기 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/participants",
                    response
            );

            log.info("배틀 나가기 처리 완료: roomId={}, userId={}, 상태={}",
                    request.getRoomId(), request.getUserId(), response.getStatus());

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

                // 추적 데이터 정리
                roomQuestionIndexMap.remove(request.getRoomId());
                lastQuestionProcessingMap.remove(request.getRoomId());
            }
        } catch (Exception e) {
            log.error("배틀 나가기 처리 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
        }
    }

    /**
     * 준비 상태 토글 처리
     * 클라이언트: /app/battle/ready로 메시지 전송
     */
    @MessageMapping("/battle/ready")
    public void toggleReady(
            BattleReadyRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        log.info("준비 상태 토글 요청: roomId={}, userId={}, sessionId={}",
                request.getRoomId(), request.getUserId(), sessionId);

        try {
            // 준비 상태 토글 처리
            BattleReadyResponse response = battleService.toggleReadyState(request, sessionId);

            // 대결방의 모든 참가자에게 준비 상태 변경 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/participants",
                    response
            );

            log.info("준비 상태 토글 처리 완료: roomId={}, userId={}, 참가자수={}",
                    request.getRoomId(), request.getUserId(), response.getParticipants().size());

            // 대결 시작 조건 확인
            if (battleService.isReadyToStart(request.getRoomId())) {
                startBattle(request.getRoomId());
            }
        } catch (Exception e) {
            log.error("준비 상태 토글 처리 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
        }
    }
}