package com.quizplatform.battle.application.controller;

import com.quizplatform.battle.application.dto.*;
import com.quizplatform.battle.application.service.BattleService;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
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

    private final Map<Long, String> gameSessionMap = new ConcurrentHashMap<>();

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
        
        // 방 생성자 ID 확인 (프론트에서 전달)
        Long creatorId = request.getCreatorUserId();
        
        log.info("배틀 입장 요청: roomId={}, userId={}, creatorId={}, sessionId={}", 
                request.getRoomId(), request.getUserId(), creatorId, sessionId);

        try {
            // 방 생성자인 경우 join 요청을 처리하지 않음 (서버에서 이미 등록됨)
            if (creatorId != null && creatorId.equals(request.getUserId())) {
                log.info("방 생성자는 이미 자동 등록되어 join 요청 무시: roomId={}, userId={}", 
                        request.getRoomId(), request.getUserId());
                
                // 대신 현재 참가자 정보만 반환
                BattleJoinResponse response = battleService.getCurrentBattleParticipants(request.getRoomId());
                
                // 세션에 참가자 정보 연결 (생성자 세션 유지)
                battleService.linkSessionToParticipant(request.getRoomId(), request.getUserId(), sessionId);
                
                // 현재 참가자 정보 전송
                messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/battle/join",
                        response
                );
                
                return;
            }
            
            // 일반 참가자 - 대결방 입장 처리
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
                // 자동 시작 대신 5초 지연 후 시작하도록 수정
                log.info("모든 참가자 준비 완료. 5초 후 배틀 시작: roomId={}", request.getRoomId());
                
                // 대기 상태 메시지 전송
                messagingTemplate.convertAndSend(
                        "/topic/battle/" + request.getRoomId() + "/status",
                        new BattleRoomStatusChangeResponse(request.getRoomId(), BattleRoomStatus.READY)
                );
                
                // 5초 후 시작
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        startBattle(request.getRoomId());
                    } catch (Exception e) {
                        log.error("지연 배틀 시작 처리 중 오류 발생: roomId={}", request.getRoomId(), e);
                    }
                }).start();
            }
        } catch (Exception e) {
            log.error("배틀 입장 처리 중 오류 발생: roomId={}, userId={}", request.getRoomId(), request.getUserId(), e);
            
            // 오류 메시지를 클라이언트에게 전송
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/errors",
                    "배틀 입장 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }

    /**
     * 다음 문제로 진행
     */
    private void moveToNextQuestion(Long roomId) {
        log.info("다음 문제 준비: roomId={}", roomId);

        try {
            // 1. 현재 인덱스 로깅
            Integer currentIndex = battleService.getBattleProgress(roomId).getCurrentQuestionIndex();
            log.info("현재 문제 인덱스: {}", currentIndex);

            // 2. 다음 문제 준비
            BattleNextQuestionResponse response = battleService.prepareNextQuestion(roomId);
            log.info("다음 문제 준비 완료: roomId={}, isGameOver={}",
                    roomId, response.isGameOver());

            // 3. 게임 종료 확인
            if (response.isGameOver()) {
                log.info("게임 종료 감지 (isGameOver=true): roomId={}", roomId);

                // 게임 종료 상태 메시지 전송
                messagingTemplate.convertAndSend(
                        "/topic/battle/" + roomId + "/status",
                        new BattleRoomStatusChangeResponse(roomId, BattleRoomStatus.FINISHED)
                );

                // 게임이 종료되었으면 endBattle 호출
                endBattle(roomId);
                return;
            }

            // 4. 웹소켓을 통해 다음 문제 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/question",
                    response
            );
            log.info("다음 문제 메시지 전송 완료: roomId={}", roomId);
        } catch (Exception e) {
            log.error("다음 문제 준비 중 오류 발생: roomId={}", roomId, e);
        }
    }

    /**
     * 답변 제출 처리
     * 클라이언트: /app/battle/answer로 메시지 전송
     */
    @MessageMapping("/battle/answer")
    public void submitAnswer(BattleAnswerRequest request, @Header("simpSessionId") String sessionId) {
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

            // 모든 참가자가 답변했는지 확인 - 로그 추가
            boolean allAnswered = battleService.allParticipantsAnswered(request.getRoomId());
            log.info("모든 참가자 답변 여부: roomId={}, 결과={}", request.getRoomId(), allAnswered);

            if (allAnswered) {
                log.info("모든 참가자가 답변 완료. 다음 문제로 이동 시도: roomId={}", request.getRoomId());

                // 다음 문제로 이동 시도 시 약간의 지연 추가 (선택사항)
                Thread.sleep(1000);

                // 다음 문제로 이동
                moveToNextQuestion(request.getRoomId());
            } else {
                // 중요: 추가된 부분 - 답변이 완료되지 않았는데 문제가 넘어가지 않는 상황 디버깅
                log.info("아직 모든 참가자가 답변하지 않았습니다. 다음 문제로 넘어가지 않습니다.");

                // 안전하게 참가자 정보만 로깅 (지연 로딩 컬렉션 접근 없이)
                try {
                    progress = battleService.getBattleProgress(request.getRoomId());
                    progress.getParticipantProgress().forEach((id, p) -> {
                        log.info("참가자 진행 상황: userId={}, 점수={}, 정답수={}, 현재답변여부={}",
                                p.getUserId(), p.getCurrentScore(), p.getCorrectAnswers(),
                                p.isHasAnsweredCurrent());
                    });
                } catch (Exception e) {
                    log.warn("참가자 상세 정보 로깅 중 오류: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("답변 제출 처리 중 오류 발생: roomId={}, questionId={}",
                    request.getRoomId(), request.getQuestionId(), e);
        }
    }

    /**
     * 대결 시작
     */
    private synchronized void startBattle(Long roomId) {
        log.info("배틀 시작: roomId={}", roomId);
        
        try {
            // 시작 가능 여부 다시 한번 확인 (동시 요청 처리 대비)
            if (!battleService.isReadyToStart(roomId)) {
                log.info("배틀 시작 조건 미충족, 시작 취소: roomId={}", roomId);
                return;
            }
            
            // 이미 시작된 방인지 확인
            if (gameSessionMap.containsKey(roomId)) {
                log.info("이미 시작된 배틀입니다: roomId={}", roomId);
                return;
            }
            
            // 세션 생성 및 인덱스 초기화
            String gameSessionId = roomId + "_" + System.currentTimeMillis();
            gameSessionMap.put(roomId, gameSessionId);
            
            // 문제 인덱스 초기화
            roomQuestionIndexMap.put(roomId, -1);  // 다음에 0번 인덱스를 준비하기 위해 -1로 시작
            
            // 방 상태 업데이트 (진행 중)
            battleService.updateRoomStatus(roomId, BattleRoomStatus.IN_PROGRESS);
            
            // 진행 중 상태 메시지 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/status",
                    new BattleRoomStatusChangeResponse(roomId, BattleRoomStatus.IN_PROGRESS)
            );
            log.info("배틀 상태 업데이트 전송 완료: roomId={}, 상태={}", roomId, BattleRoomStatus.IN_PROGRESS);

            // 첫번째 문제 준비 및 전송
            moveToNextQuestion(roomId);
            
        } catch (Exception e) {
            log.error("배틀 시작 처리 중 오류 발생: roomId={}", roomId, e);
        }
    }

    /**
     * 대결 종료
     */
    private void endBattle(Long roomId) {
        log.info("배틀 종료 처리: roomId={}", roomId);
        
        try {
            // 게임 세션 정리
            gameSessionMap.remove(roomId);
            roomQuestionIndexMap.remove(roomId);
            lastQuestionProcessingMap.remove(roomId);
            
            // 배틀 종료 처리
            BattleResultResponse result = battleService.endBattle(roomId);
            
            log.info("배틀 결과 계산 완료: roomId={}, 참가자수={}", roomId, result.getParticipants().size());
            
            // 최종 결과 전송
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/result",
                    result
            );
            
            log.info("배틀 종료 메시지 전송 완료: roomId={}", roomId);
            
        } catch (Exception e) {
            log.error("배틀 종료 처리 중 오류 발생: roomId={}", roomId, e);
        }
    }

    /**
     * 대결방 나가기 처리
     * 클라이언트: /app/battle/leave로 메시지 전송
     */
    @MessageMapping("/battle/leave")
    public void leaveBattle(
            BattleLeaveRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        log.info("배틀 나가기 요청: roomId={}, userId={}, sessionId={}",
                request.getRoomId(), request.getUserId(), sessionId);
        
        try {
            // 배틀룸 나가기 처리
            BattleLeaveResponse response = battleService.leaveBattle(request, sessionId);
            
            // 나간 참가자에게 응답
            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/queue/battle/leave",
                    response
            );
            
            // 남은 참가자들에게 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/participants",
                    battleService.getCurrentBattleParticipants(request.getRoomId())
            );
            
            log.info("배틀 나가기 처리 완료: roomId={}, userId={}, 남은참가자수={}",
                    request.getRoomId(), request.getUserId(), response.getRemainingParticipants());
            
        } catch (Exception e) {
            log.error("배틀 나가기 처리 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
        }
    }

    /**
     * 준비 상태 토글
     * 클라이언트: /app/battle/ready로 메시지 전송
     */
    @MessageMapping("/battle/ready")
    public synchronized void toggleReady(
            BattleReadyRequest request,
            @Header("simpSessionId") String sessionId
    ) {
        log.info("준비 상태 토글 요청: roomId={}, userId={}, 준비상태={}",
                request.getRoomId(), request.getUserId(), request.isReady());
        
        try {
            // 준비 상태 토글 처리
            BattleReadyResponse response = battleService.toggleReady(request);
            
            // 대결방의 모든 참가자에게 준비 상태 변경 알림
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + request.getRoomId() + "/participants",
                    response
            );
            
            log.info("준비 상태 토글 처리 완료: roomId={}, userId={}, 준비상태={}",
                    request.getRoomId(), request.getUserId(), request.isReady());
            
            // 대결 시작 조건 확인
            if (battleService.isReadyToStart(request.getRoomId())) {
                log.info("모든 참가자 준비 완료. 5초 후 배틀 시작: roomId={}", request.getRoomId());
                
                // 대기 상태 메시지 전송
                messagingTemplate.convertAndSend(
                        "/topic/battle/" + request.getRoomId() + "/status",
                        new BattleRoomStatusChangeResponse(request.getRoomId(), BattleRoomStatus.READY)
                );
                
                // 5초 후 시작
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        startBattle(request.getRoomId());
                    } catch (Exception e) {
                        log.error("지연 배틀 시작 처리 중 오류 발생: roomId={}", request.getRoomId(), e);
                    }
                }).start();
            }
        } catch (Exception e) {
            log.error("준비 상태 토글 처리 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
        }
    }
} 