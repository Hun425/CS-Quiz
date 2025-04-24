package com.quizplatform.battle.application.service;

import com.quizplatform.battle.application.dto.*;
import com.quizplatform.battle.domain.model.BattleAnswer;
import com.quizplatform.battle.domain.model.BattleRoom;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 기존 컨트롤러와 새로운 BattleService 인터페이스 사이의 어댑터
 * 레거시 코드 호환성을 위해 기존 메소드 시그니처를 유지하면서 새 서비스로 연결
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BattleServiceAdapter {
    
    private final BattleService battleService;
    
    /**
     * 새로운 배틀방을 생성합니다.
     */
    public BattleRoom createBattleRoom(Long quizId, int maxParticipants, Long creatorId, 
                                      String creatorUsername, String creatorProfileImage,
                                      int totalQuestions, Integer questionTimeLimitSeconds) {
        
        log.info("어댑터: 배틀방 생성 - quizId={}, creator={}", quizId, creatorId);
        
        // Builder 패턴을 사용하여 BattleRoom 생성
        BattleRoom battleRoom = BattleRoom.builder()
                .quizId(quizId)
                .maxParticipants(maxParticipants)
                .creatorId(creatorId)
                .totalQuestions(totalQuestions)
                .questionTimeLimitSeconds(questionTimeLimitSeconds)
                .build();
        
        // 방장을 첫 참가자로 추가
        battleRoom.addParticipant(creatorId, creatorUsername, creatorProfileImage);
        
        // 웹소켓 서비스에 방 등록
        BattleJoinRequest joinRequest = new BattleJoinRequest(
                battleRoom.getId(), creatorId, creatorId
        );
        battleService.joinBattle(joinRequest, null);
        
        return battleRoom;
    }
    
    /**
     * 특정 ID의 배틀방을 조회합니다.
     */
    public BattleRoom getBattleRoom(Long roomId) {
        log.info("어댑터: 배틀방 조회 - roomId={}", roomId);
        
        try {
            // 웹소켓 서비스에서 현재 방 정보 가져오기
            BattleJoinResponse roomInfo = battleService.getCurrentBattleParticipants(roomId);
            BattleProgressResponse progress = battleService.getBattleProgress(roomId);
            
            // 새로운 배틀룸 생성 (Builder에서 모든 속성 설정)
            BattleRoom battleRoom = BattleRoom.builder()
                    .quizId(0L) // 임시값
                    .maxParticipants(roomInfo.getMaxParticipants())
                    .creatorId(roomInfo.getCreatorId())
                    .totalQuestions(progress.getTotalQuestions())
                    .questionTimeLimitSeconds(30) // 기본값
                    .build();
            
            // 각 참가자 추가
            for (com.quizplatform.battle.application.dto.BattleParticipant dtoParticipant : roomInfo.getParticipants()) {
                battleRoom.addParticipant(
                    dtoParticipant.getUserId(), 
                    dtoParticipant.getUsername(), 
                    "" // 프로필 이미지는 DTO에 없으므로 빈 문자열 사용
                );
            }
            
            // 현재 상태 반영
            // 참고: 일부 속성은 builder에서 설정할 수 없고, 내부 메서드를 통해 설정해야 함
            if (roomInfo.getStatus() == BattleRoomStatus.IN_PROGRESS) {
                battleRoom.startBattle(); // 배틀 시작 상태 설정
                // currentQuestionIndex 설정은 startNextQuestion을 여러 번 호출해서 해야 함
                if (progress.getCurrentQuestionIndex() > 0) {
                    for (int i = 0; i < progress.getCurrentQuestionIndex(); i++) {
                        battleRoom.startNextQuestion();
                    }
                }
            } else if (roomInfo.getStatus() == BattleRoomStatus.FINISHED) {
                battleRoom.startBattle();
                battleRoom.finishBattle();
            }
            
            return battleRoom;
        } catch (Exception e) {
            log.error("배틀방 조회 실패: {}", e.getMessage(), e);
            // 기본 BattleRoom 생성
            return BattleRoom.builder()
                    .quizId(0L)
                    .maxParticipants(2)
                    .creatorId(0L)
                    .totalQuestions(0)
                    .questionTimeLimitSeconds(30)
                    .build();
        }
    }
    
    /**
     * 특정 상태의 배틀방 목록을 조회합니다.
     */
    public List<BattleRoom> getBattleRoomsByStatus(BattleRoomStatus status) {
        log.info("어댑터: 상태별 배틀방 조회 - status={}", status);
        
        // 웹소켓 서비스에서 상태별 배틀방 목록 조회
        List<BattleRoomResponse> responses = battleService.getBattleRoomsByStatus(status);
        
        // 배틀룸 도메인 모델로 변환
        return responses.stream().map(response -> {
            try {
                // Builder 패턴으로 BattleRoom 생성
                BattleRoom battleRoom = BattleRoom.builder()
                        .quizId(response.getQuizId() != null ? response.getQuizId() : 0L)
                        .maxParticipants(response.getMaxParticipants())
                        .creatorId(response.getCreatorId())
                        .totalQuestions(response.getTotalQuestions())
                        .questionTimeLimitSeconds(response.getQuestionTimeLimitSeconds())
                        .build();
                
                // 각 참가자 추가
                if (response.getParticipants() != null) {
                    for (com.quizplatform.battle.application.dto.BattleParticipant dtoParticipant : response.getParticipants()) {
                        battleRoom.addParticipant(
                            dtoParticipant.getUserId(), 
                            dtoParticipant.getUsername(), 
                            "" // 프로필 이미지는 DTO에 없으므로 빈 문자열 사용
                        );
                    }
                }
                
                // 상태 반영
                if (response.getStatus() == BattleRoomStatus.IN_PROGRESS) {
                    battleRoom.startBattle();
                } else if (response.getStatus() == BattleRoomStatus.FINISHED) {
                    battleRoom.startBattle();
                    battleRoom.finishBattle();
                }
                
                return battleRoom;
            } catch (Exception e) {
                log.error("배틀방 변환 실패: {}", e.getMessage(), e);
                return null;
            }
        })
        .filter(battleRoom -> battleRoom != null)
        .collect(Collectors.toList());
    }
    
    /**
     * 사용자가 특정 배틀방에 참가합니다.
     */
    public BattleRoom joinBattleRoom(Long roomId, Long userId, String username, String profileImage) {
        log.info("어댑터: 배틀방 참가 - roomId={}, userId={}", roomId, userId);
        
        try {
            // 웹소켓 서비스에 참가 요청
            BattleJoinRequest request = new BattleJoinRequest(roomId, userId, null);
            BattleJoinResponse response = battleService.joinBattle(request, null);
            
            // 기존 배틀룸 정보 가져오기 (ID 포함)
            return getBattleRoom(roomId);
        } catch (Exception e) {
            log.error("배틀방 참가 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
    
    /**
     * 참가자의 준비 상태를 토글합니다.
     */
    public BattleRoom toggleReady(Long roomId, Long userId) {
        log.info("어댑터: 준비 상태 토글 - roomId={}, userId={}", roomId, userId);
        
        try {
            // 현재 준비 상태 확인을 위해 방 정보 조회
            BattleJoinResponse roomInfo = battleService.getCurrentBattleParticipants(roomId);
            
            // 참가자 찾기
            com.quizplatform.battle.application.dto.BattleParticipant targetParticipant = null;
            for (com.quizplatform.battle.application.dto.BattleParticipant p : roomInfo.getParticipants()) {
                if (p.getUserId().equals(userId)) {
                    targetParticipant = p;
                    break;
                }
            }
            
            // 참가자가 없으면 오류
            if (targetParticipant == null) {
                throw new IllegalArgumentException("참가자를 찾을 수 없습니다: userId=" + userId);
            }
            
            // 현재 준비 상태의 반대로 설정
            boolean newReadyState = !targetParticipant.isReady();
            
            // 준비 상태 변경 요청
            BattleReadyRequest request = new BattleReadyRequest(roomId, userId, newReadyState);
            battleService.toggleReady(request);
            
            // 업데이트된 방 정보 반환
            return getBattleRoom(roomId);
        } catch (Exception e) {
            log.error("준비 상태 변경 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
    
    /**
     * 배틀방에서 참가자를 제거합니다.
     */
    public BattleRoom leaveBattleRoom(Long roomId, Long userId) {
        log.info("어댑터: 배틀방 나가기 - roomId={}, userId={}", roomId, userId);
        
        try {
            // 웹소켓 서비스에 나가기 요청
            BattleLeaveRequest request = new BattleLeaveRequest(roomId, userId);
            BattleLeaveResponse response = battleService.leaveBattle(request, null);
            
            // 방이 닫혔으면 null 반환
            if (response.isRoomClosed()) {
                return null;
            }
            
            // 업데이트된 방 정보 조회
            return getBattleRoom(roomId);
        } catch (Exception e) {
            log.error("배틀방 나가기 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
    
    /**
     * 배틀을 시작합니다.
     */
    public BattleRoom startBattle(Long roomId) {
        log.info("어댑터: 배틀 시작 - roomId={}", roomId);
        
        try {
            // 배틀 상태 업데이트
            battleService.updateRoomStatus(roomId, BattleRoomStatus.IN_PROGRESS);
            
            // 방 정보 가져오기
            BattleRoom battleRoom = getBattleRoom(roomId);
            
            return battleRoom;
        } catch (Exception e) {
            log.error("배틀 시작 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
    
    /**
     * 배틀에서 다음 문제로 진행합니다.
     */
    public BattleRoom startNextQuestion(Long roomId) {
        log.info("어댑터: 다음 문제 시작 - roomId={}", roomId);
        
        try {
            // 다음 문제 준비 
            BattleNextQuestionResponse response = battleService.prepareNextQuestion(roomId);
            
            // 게임이 종료되었으면 배틀 종료
            if (response.isGameOver()) {
                return finishBattle(roomId);
            }
            
            // 업데이트된 방 정보 가져오기
            BattleRoom battleRoom = getBattleRoom(roomId);
            
            return battleRoom;
        } catch (Exception e) {
            log.error("다음 문제 시작 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
    
    /**
     * 배틀 참가자의 문제 답변을 처리합니다.
     */
    public BattleAnswer processAnswer(Long roomId, Long userId, int questionIndex, 
                              String answer, boolean isCorrect, long answerTime) {
        log.info("어댑터: 답변 처리 - roomId={}, userId={}, questionIndex={}, isCorrect={}", 
                 roomId, userId, questionIndex, isCorrect);
        
        try {
            // BattleAnswerRequest 생성
            BattleAnswerRequest request = new BattleAnswerRequest(
                roomId, 
                userId, 
                (long)questionIndex,
                Integer.parseInt(answer), // 문자열 답변을 인덱스로 변환 
                answerTime
            );
            
            // 답변 처리
            BattleAnswerResponse response = battleService.processAnswer(request, null);
            
            // Builder 패턴으로 BattleAnswer 생성
            return BattleAnswer.builder()
                    .questionIndex(questionIndex)
                    .isCorrect(response.isCorrect())
                    .score(response.getPoints())
                    .answer(answer)
                    .answerTime(answerTime)
                    .submittedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("답변 처리 실패: {}", e.getMessage(), e);
            
            // 오류 발생 시 기본 BattleAnswer 생성
            return BattleAnswer.builder()
                    .questionIndex(questionIndex)
                    .isCorrect(isCorrect)
                    .answer(answer)
                    .answerTime(answerTime)
                    .score(0.0)
                    .submittedAt(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * 배틀을 종료하고 결과를 처리합니다.
     */
    public BattleRoom finishBattle(Long roomId) {
        log.info("어댑터: 배틀 종료 - roomId={}", roomId);
        
        try {
            // 배틀 종료 처리
            BattleResultResponse result = battleService.endBattle(roomId);
            
            // 방 상태 업데이트
            battleService.updateRoomStatus(roomId, BattleRoomStatus.FINISHED);
            
            // 배틀방 정보 조회
            BattleRoom battleRoom = getBattleRoom(roomId);
            
            return battleRoom;
        } catch (Exception e) {
            log.error("배틀 종료 실패: {}", e.getMessage(), e);
            return getBattleRoom(roomId);
        }
    }
} 