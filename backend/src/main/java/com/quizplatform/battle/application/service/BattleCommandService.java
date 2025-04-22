package com.quizplatform.battle.application.service;

import com.quizplatform.battle.application.port.in.*;
import com.quizplatform.battle.application.port.in.command.*;
import com.quizplatform.battle.application.port.out.*;
import com.quizplatform.battle.domain.event.BattleCancelledEvent;
import com.quizplatform.battle.domain.event.BattleCompletedEvent;
import com.quizplatform.battle.domain.event.BattleCreatedEvent;
import com.quizplatform.battle.domain.event.BattleStartedEvent;
import com.quizplatform.battle.domain.model.Battle;
import com.quizplatform.battle.domain.model.BattleParticipant;
import com.quizplatform.battle.domain.model.BattleSummary;
import com.quizplatform.battle.domain.model.BattleStatus;
import com.quizplatform.battle.domain.model.ParticipantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 배틀 커맨드 관련 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class BattleCommandService implements 
        CreateBattleUseCase, 
        StartBattleUseCase, 
        SubmitBattleAnswerUseCase, 
        CompleteBattleUseCase, 
        CancelBattleUseCase {

    private final SaveBattlePort saveBattlePort;
    private final LoadBattlePort loadBattlePort;
    private final SaveBattleParticipantPort saveBattleParticipantPort;
    private final LoadBattleParticipantPort loadBattleParticipantPort;
    private final SaveBattleSummaryPort saveBattleSummaryPort;
    private final PublishBattleEventPort publishBattleEventPort;
    private final ValidateQuizAnswerPort validateQuizAnswerPort;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UUID createBattle(CreateBattleCommand command) {
        // 배틀 도메인 객체 생성
        Battle battle = Battle.create(
                command.getChallengerId(),
                command.getOpponentId(),
                command.getQuizId(),
                command.getTimeLimit()
        );
        
        // 배틀 저장
        Battle savedBattle = saveBattlePort.saveBattle(battle);
        
        // 도메인 이벤트 발행
        publishBattleEventPort.publishEvent(
                BattleCreatedEvent.of(
                        savedBattle.getId(),
                        savedBattle.getChallengerId(),
                        savedBattle.getOpponentId(),
                        savedBattle.getQuizId()
                )
        );
        
        return savedBattle.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Battle startBattle(StartBattleCommand command) {
        // 배틀 조회
        Battle battle = loadBattlePort.loadBattleById(command.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다: " + command.getBattleId()));
        
        // 배틀 시작 권한 확인
        if (!battle.getChallengerId().equals(command.getUserId()) && !battle.getOpponentId().equals(command.getUserId())) {
            throw new IllegalArgumentException("배틀에 참여할 권한이 없습니다");
        }
        
        // 배틀 시작
        Battle startedBattle = battle.start();
        Battle savedBattle = saveBattlePort.saveBattle(startedBattle);
        
        // 도메인 이벤트 발행
        publishBattleEventPort.publishEvent(
                BattleStartedEvent.of(
                        savedBattle.getId(),
                        savedBattle.getChallengerId(),
                        savedBattle.getOpponentId(),
                        savedBattle.getQuizId()
                )
        );
        
        return savedBattle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BattleParticipant submitAnswer(SubmitBattleAnswerCommand command) {
        // 배틀 조회
        Battle battle = loadBattlePort.loadBattleById(command.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다: " + command.getBattleId()));
        
        // 배틀 상태 확인
        if (battle.getStatus() != BattleStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 배틀에만 답변을 제출할 수 있습니다");
        }
        
        // 참가자 확인
        BattleParticipant participant = loadBattleParticipantPort
                .loadBattleParticipant(command.getBattleId(), command.getUserId())
                .orElseGet(() -> {
                    // 참가자가 없으면 새로 생성
                    int totalQuestions = 10; // 실제로는 퀴즈 정보에서 가져와야 함
                    BattleParticipant newParticipant = BattleParticipant.create(
                            command.getUserId(),
                            command.getBattleId(),
                            totalQuestions
                    );
                    
                    // 참가자 추가
                    battle.addParticipant(newParticipant);
                    saveBattlePort.saveBattle(battle);
                    
                    return saveBattleParticipantPort.saveBattleParticipant(newParticipant);
                });
        
        // 답변 유효성 검증
        boolean isCorrect = validateQuizAnswerPort.validateAnswer(
                command.getQuestionId(), 
                command.getSelectedOptionId()
        );
        
        // 점수 계산
        int earnedPoints = isCorrect ? 
                validateQuizAnswerPort.calculateScore(command.getQuestionId(), command.getTimeSpentInSeconds()) : 0;
        
        // 참가자 답변 추가
        BattleParticipant updatedParticipant = participant.addAnswer(isCorrect, earnedPoints);
        
        // 참가자 저장
        return saveBattleParticipantPort.saveBattleParticipant(updatedParticipant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BattleSummary completeBattle(CompleteBattleCommand command) {
        // 배틀 조회
        Battle battle = loadBattlePort.loadBattleById(command.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다: " + command.getBattleId()));
        
        // 참가자 조회
        BattleParticipant participant = loadBattleParticipantPort
                .loadBattleParticipant(command.getBattleId(), command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("배틀 참가자를 찾을 수 없습니다"));
        
        // 참가자 완료 처리
        BattleParticipant completedParticipant = participant.complete();
        saveBattleParticipantPort.saveBattleParticipant(completedParticipant);
        
        // 모든 참가자가 완료한 경우 배틀 완료 처리
        boolean allCompleted = true;
        for (BattleParticipant p : loadBattleParticipantPort.loadBattleParticipantsByBattleId(battle.getId())) {
            if (!p.getStatus().equals(ParticipantStatus.COMPLETED) && 
                !p.getStatus().equals(ParticipantStatus.FORFEITED)) {
                allCompleted = false;
                break;
            }
        }
        
        if (allCompleted) {
            // 배틀 완료
            Battle completedBattle = battle.complete();
            Battle savedBattle = saveBattlePort.saveBattle(completedBattle);
            
            // 승자 결정
            UUID winnerId = savedBattle.determineWinner();
            
            // 배틀 요약 생성
            BattleSummary summary = BattleSummary.create(savedBattle, winnerId);
            BattleSummary savedSummary = saveBattleSummaryPort.saveBattleSummary(summary);
            
            // 도메인 이벤트 발행
            publishBattleEventPort.publishEvent(
                    BattleCompletedEvent.of(
                            savedBattle.getId(),
                            savedSummary.getWinnerId(),
                            savedSummary.getLoserId(),
                            savedSummary.getWinnerScore(),
                            savedSummary.getLoserScore(),
                            savedSummary.getDurationInSeconds()
                    )
            );
            
            return savedSummary;
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Battle cancelBattle(CancelBattleCommand command) {
        // 배틀 조회
        Battle battle = loadBattlePort.loadBattleById(command.getBattleId())
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다: " + command.getBattleId()));
        
        // 권한 확인
        if (!battle.getChallengerId().equals(command.getUserId()) && !battle.getOpponentId().equals(command.getUserId())) {
            throw new IllegalArgumentException("배틀을 취소할 권한이 없습니다");
        }
        
        // 배틀 취소
        Battle cancelledBattle = battle.cancel();
        Battle savedBattle = saveBattlePort.saveBattle(cancelledBattle);
        
        // 도메인 이벤트 발행
        publishBattleEventPort.publishEvent(
                BattleCancelledEvent.of(
                        savedBattle.getId(),
                        savedBattle.getChallengerId(),
                        savedBattle.getOpponentId()
                )
        );
        
        return savedBattle;
    }
}
