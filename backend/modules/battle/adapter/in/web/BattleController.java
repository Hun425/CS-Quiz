package com.quizplatform.battle.adapter.in.web;

import com.quizplatform.battle.adapter.in.web.dto.request.CreateBattleRequest;
import com.quizplatform.battle.adapter.in.web.dto.request.StartBattleRequest;
import com.quizplatform.battle.adapter.in.web.dto.request.SubmitBattleAnswerRequest;
import com.quizplatform.battle.adapter.in.web.dto.request.CompleteBattleRequest;
import com.quizplatform.battle.adapter.in.web.dto.request.CancelBattleRequest;
import com.quizplatform.battle.adapter.in.web.dto.response.BattleResponse;
import com.quizplatform.battle.adapter.in.web.dto.response.BattleParticipantResponse;
import com.quizplatform.battle.adapter.in.web.dto.response.BattleSummaryResponse;
import com.quizplatform.battle.application.port.in.*;
import com.quizplatform.battle.application.port.in.command.*;
import com.quizplatform.battle.domain.model.Battle;
import com.quizplatform.battle.domain.model.BattleParticipant;
import com.quizplatform.battle.domain.model.BattleSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 배틀 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/battles")
@RequiredArgsConstructor
public class BattleController {

    private final CreateBattleUseCase createBattleUseCase;
    private final StartBattleUseCase startBattleUseCase;
    private final SubmitBattleAnswerUseCase submitBattleAnswerUseCase;
    private final CompleteBattleUseCase completeBattleUseCase;
    private final CancelBattleUseCase cancelBattleUseCase;
    private final GetBattleUseCase getBattleUseCase;
    private final GetBattleSummaryUseCase getBattleSummaryUseCase;

    /**
     * 배틀 생성
     * 
     * @param request 배틀 생성 요청
     * @return 생성된 배틀 ID
     */
    @PostMapping
    public ResponseEntity<UUID> createBattle(@Valid @RequestBody CreateBattleRequest request) {
        CreateBattleCommand command = CreateBattleCommand.builder()
                .challengerId(request.getChallengerId())
                .opponentId(request.getOpponentId())
                .quizId(request.getQuizId())
                .timeLimit(request.getTimeLimit())
                .build();
        
        UUID battleId = createBattleUseCase.createBattle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(battleId);
    }

    /**
     * 배틀 시작
     * 
     * @param battleId 배틀 ID
     * @param request 배틀 시작 요청
     * @return 시작된 배틀 정보
     */
    @PostMapping("/{battleId}/start")
    public ResponseEntity<BattleResponse> startBattle(
            @PathVariable UUID battleId,
            @Valid @RequestBody StartBattleRequest request) {
        
        StartBattleCommand command = StartBattleCommand.builder()
                .battleId(battleId)
                .userId(request.getUserId())
                .build();
        
        Battle battle = startBattleUseCase.startBattle(command);
        return ResponseEntity.ok(mapToBattleResponse(battle));
    }

    /**
     * 배틀 답변 제출
     * 
     * @param battleId 배틀 ID
     * @param request 답변 제출 요청
     * @return 갱신된 배틀 참가자 정보
     */
    @PostMapping("/{battleId}/submit-answer")
    public ResponseEntity<BattleParticipantResponse> submitAnswer(
            @PathVariable UUID battleId,
            @Valid @RequestBody SubmitBattleAnswerRequest request) {
        
        SubmitBattleAnswerCommand command = SubmitBattleAnswerCommand.builder()
                .battleId(battleId)
                .userId(request.getUserId())
                .questionId(request.getQuestionId())
                .selectedOptionId(request.getSelectedOptionId())
                .timeSpentInSeconds(request.getTimeSpentInSeconds())
                .build();
        
        BattleParticipant participant = submitBattleAnswerUseCase.submitAnswer(command);
        return ResponseEntity.ok(mapToParticipantResponse(participant));
    }

    /**
     * 배틀 완료
     * 
     * @param battleId 배틀 ID
     * @param request 배틀 완료 요청
     * @return 배틀 요약 정보
     */
    @PostMapping("/{battleId}/complete")
    public ResponseEntity<BattleSummaryResponse> completeBattle(
            @PathVariable UUID battleId,
            @Valid @RequestBody CompleteBattleRequest request) {
        
        CompleteBattleCommand command = CompleteBattleCommand.builder()
                .battleId(battleId)
                .userId(request.getUserId())
                .build();
        
        BattleSummary summary = completeBattleUseCase.completeBattle(command);
        
        if (summary == null) {
            // 아직 모든 참가자가 완료하지 않았을 경우
            return ResponseEntity.accepted().build();
        }
        
        return ResponseEntity.ok(mapToSummaryResponse(summary));
    }

    /**
     * 배틀 취소
     * 
     * @param battleId 배틀 ID
     * @param request 배틀 취소 요청
     * @return 취소된 배틀 정보
     */
    @PostMapping("/{battleId}/cancel")
    public ResponseEntity<BattleResponse> cancelBattle(
            @PathVariable UUID battleId,
            @Valid @RequestBody CancelBattleRequest request) {
        
        CancelBattleCommand command = CancelBattleCommand.builder()
                .battleId(battleId)
                .userId(request.getUserId())
                .build();
        
        Battle battle = cancelBattleUseCase.cancelBattle(command);
        return ResponseEntity.ok(mapToBattleResponse(battle));
    }

    /**
     * 배틀 조회
     * 
     * @param battleId 배틀 ID
     * @return 조회된 배틀 정보
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponse> getBattle(@PathVariable UUID battleId) {
        Battle battle = getBattleUseCase.getBattleById(battleId);
        return ResponseEntity.ok(mapToBattleResponse(battle));
    }

    /**
     * 사용자 참여 배틀 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자가 참여한 배틀 목록
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<BattleResponse>> getBattlesByUserId(@PathVariable UUID userId) {
        List<Battle> battles = getBattleUseCase.getBattlesByUserId(userId);
        List<BattleResponse> responses = battles.stream()
                .map(this::mapToBattleResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 최근 배틀 목록 조회
     * 
     * @param limit 조회할 배틀 수
     * @return 최근 배틀 목록
     */
    @GetMapping("/recent")
    public ResponseEntity<List<BattleResponse>> getRecentBattles(
            @RequestParam(defaultValue = "10") int limit) {
        List<Battle> battles = getBattleUseCase.getRecentBattles(limit);
        List<BattleResponse> responses = battles.stream()
                .map(this::mapToBattleResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 배틀 요약 정보 조회
     * 
     * @param battleId 배틀 ID
     * @return 조회된 배틀 요약 정보
     */
    @GetMapping("/{battleId}/summary")
    public ResponseEntity<BattleSummaryResponse> getBattleSummary(@PathVariable UUID battleId) {
        BattleSummary summary = getBattleSummaryUseCase.getBattleSummaryById(battleId);
        return ResponseEntity.ok(mapToSummaryResponse(summary));
    }

    /**
     * 사용자 참여 배틀 요약 정보 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자가 참여한 배틀 요약 정보 목록
     */
    @GetMapping("/summaries/users/{userId}")
    public ResponseEntity<List<BattleSummaryResponse>> getBattleSummariesByUserId(@PathVariable UUID userId) {
        List<BattleSummary> summaries = getBattleSummaryUseCase.getBattleSummariesByUserId(userId);
        List<BattleSummaryResponse> responses = summaries.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 최근 배틀 요약 정보 목록 조회
     * 
     * @param limit 조회할 배틀 요약 정보 수
     * @return 최근 배틀 요약 정보 목록
     */
    @GetMapping("/summaries/recent")
    public ResponseEntity<List<BattleSummaryResponse>> getRecentBattleSummaries(
            @RequestParam(defaultValue = "10") int limit) {
        List<BattleSummary> summaries = getBattleSummaryUseCase.getRecentBattleSummaries(limit);
        List<BattleSummaryResponse> responses = summaries.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Battle 엔티티를 BattleResponse DTO로 변환
     * 
     * @param battle Battle 엔티티
     * @return BattleResponse DTO
     */
    private BattleResponse mapToBattleResponse(Battle battle) {
        return BattleResponse.builder()
                .id(battle.getId())
                .challengerId(battle.getChallengerId())
                .opponentId(battle.getOpponentId())
                .quizId(battle.getQuizId())
                .timeLimit(battle.getTimeLimit())
                .status(battle.getStatus().name())
                .createdAt(battle.getCreatedAt())
                .startTime(battle.getStartTime())
                .endTime(battle.getEndTime())
                .participants(battle.getParticipants().stream()
                        .map(this::mapToParticipantResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * BattleParticipant 엔티티를 BattleParticipantResponse DTO로 변환
     * 
     * @param participant BattleParticipant 엔티티
     * @return BattleParticipantResponse DTO
     */
    private BattleParticipantResponse mapToParticipantResponse(BattleParticipant participant) {
        return BattleParticipantResponse.builder()
                .id(participant.getId())
                .userId(participant.getUserId())
                .battleId(participant.getBattleId())
                .score(participant.getScore())
                .correctAnswers(participant.getCorrectAnswers())
                .totalQuestions(participant.getTotalQuestions())
                .joinTime(participant.getJoinTime())
                .completionTime(participant.getCompletionTime())
                .status(participant.getStatus().name())
                .build();
    }

    /**
     * BattleSummary 엔티티를 BattleSummaryResponse DTO로 변환
     * 
     * @param summary BattleSummary 엔티티
     * @return BattleSummaryResponse DTO
     */
    private BattleSummaryResponse mapToSummaryResponse(BattleSummary summary) {
        return BattleSummaryResponse.builder()
                .id(summary.getId())
                .battleId(summary.getBattleId())
                .winnerId(summary.getWinnerId())
                .loserId(summary.getLoserId())
                .winnerScore(summary.getWinnerScore())
                .loserScore(summary.getLoserScore())
                .challengerScore(summary.getChallengerScore())
                .opponentScore(summary.getOpponentScore())
                .durationInSeconds(summary.getDurationInSeconds())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
