package com.quizplatform.battle.application.service;

import com.quizplatform.battle.application.dto.*;
import com.quizplatform.battle.domain.model.BattleRoomStatus;

import java.util.List;

public interface BattleService {
    
    // 배틀 참가
    BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId);
    
    // 현재 배틀 참가자 정보 조회
    BattleJoinResponse getCurrentBattleParticipants(Long roomId);
    
    // 세션에 참가자 정보 연결
    void linkSessionToParticipant(Long roomId, Long userId, String sessionId);
    
    // 준비 상태 변경
    BattleReadyResponse toggleReady(BattleReadyRequest request);
    
    // 모든 참가자가 준비되었는지 확인
    boolean isReadyToStart(Long roomId);
    
    // 방 상태 업데이트
    void updateRoomStatus(Long roomId, BattleRoomStatus status);
    
    // 다음 문제 준비
    BattleNextQuestionResponse prepareNextQuestion(Long roomId);
    
    // 답변 처리
    BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId);
    
    // 배틀 진행 상황 조회
    BattleProgressResponse getBattleProgress(Long roomId);
    
    // 모든 참가자가 답변했는지 확인
    boolean allParticipantsAnswered(Long roomId);
    
    // 배틀 종료 및 결과 계산
    BattleResultResponse endBattle(Long roomId);
    
    // 배틀방 나가기
    BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId);
    
    // 특정 상태의 배틀방 목록 조회
    List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status);
}