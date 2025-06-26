package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 배틀 방 상태 관리 클래스
 * 
 * <p>배틀 방의 상태 변경과 관련된 모든 로직을 담당합니다.
 * 상태 검증, 상태 전환, 참가자 상태 관리 등을 처리합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
public class BattleRoomStateManager {

    private static final int MIN_PARTICIPANTS = 2;

    /**
     * 배틀 시작 가능 여부 확인
     * 
     * @param status 현재 배틀 방 상태
     * @param participants 참가자 목록
     * @param maxParticipants 최대 참가자 수
     * @return 시작 가능하면 true, 아니면 false
     */
    public boolean isReadyToStart(BattleRoomStatus status, Set<BattleParticipant> participants, int maxParticipants) {
        if (status != BattleRoomStatus.WAITING) {
            return false;
        }
        
        return participants.size() >= MIN_PARTICIPANTS &&
               participants.size() <= maxParticipants &&
               participants.stream().allMatch(BattleParticipant::isReady);
    }

    /**
     * 참가자 추가 유효성 검사
     * 
     * @param status 현재 배틀 방 상태
     * @param participants 현재 참가자 목록
     * @param maxParticipants 최대 참가자 수
     * @param user 참가 예정 사용자
     * @throws BusinessException 참가 불가능한 상태일 경우
     */
    public void validateParticipantAddition(BattleRoomStatus status, Set<BattleParticipant> participants, 
                                          int maxParticipants, User user) {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (isParticipantLimitReached(participants, maxParticipants)) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL);
        }
        if (hasParticipant(participants, user)) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING);
        }
    }

    /**
     * 배틀 시작 검증
     * 
     * @param status 현재 배틀 방 상태
     * @param participants 참가자 목록
     * @param maxParticipants 최대 참가자 수
     * @throws BusinessException 시작 불가능한 상태일 경우
     */
    public void validateBattleStart(BattleRoomStatus status, Set<BattleParticipant> participants, int maxParticipants) {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isReadyToStart(status, participants, maxParticipants)) {
            throw new BusinessException(ErrorCode.NOT_READY_TO_START);
        }
    }

    /**
     * 배틀 진행 중 상태 검증
     * 
     * @param status 현재 배틀 방 상태
     * @throws BusinessException 진행 중이 아닌 상태일 경우
     */
    public void validateBattleInProgress(BattleRoomStatus status) {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }
    }

    /**
     * 모든 참가자의 답변 완료 여부 확인
     * 
     * @param status 현재 배틀 방 상태
     * @param participants 참가자 목록
     * @param currentQuestionIndex 현재 문제 인덱스
     * @param totalQuestions 전체 문제 수
     * @return 모두 답변했으면 true, 아니면 false
     */
    public boolean allParticipantsAnswered(BattleRoomStatus status, Set<BattleParticipant> participants, 
                                         int currentQuestionIndex, int totalQuestions) {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return false;
        }
        if (currentQuestionIndex < 0) {
            return false;
        }
        if (participants.isEmpty()) {
            return false;
        }

        long activeParticipants = participants.stream()
                .filter(BattleParticipant::isActive)
                .count();
        
        if (activeParticipants == 0) {
            return false;
        }

        boolean isLastQuestion = currentQuestionIndex >= totalQuestions - 1;
        
        if (isLastQuestion) {
            log.info("마지막 문제 참가자 답변 여부 확인: 현재인덱스={}", currentQuestionIndex);
            boolean result = participants.stream()
                    .filter(BattleParticipant::isActive)
                    .allMatch(p -> p.getAnswers().size() >= totalQuestions);
            log.info("마지막 문제 참가자 답변 여부 결과: 결과={}", result);
            return result;
        }

        boolean result = participants.stream()
                .filter(BattleParticipant::isActive)
                .allMatch(p -> p.getAnswers().size() >= currentQuestionIndex + 1);
        
        log.info("모든 참가자 답변 여부: 결과={}, 현재문제인덱스={}, 참가자수={}", 
                result, currentQuestionIndex, activeParticipants);
        
        return result;
    }

    /**
     * 참가자 준비 상태 토글 검증
     * 
     * @param status 현재 배틀 방 상태
     * @param isActive 참가자 활성 상태
     * @throws BusinessException 준비 상태 변경이 불가능한 경우
     */
    public void validateReadyToggle(BattleRoomStatus status, boolean isActive) {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isActive) {
            throw new BusinessException(ErrorCode.PARTICIPANT_INACTIVE);
        }
    }

    /**
     * 참가자 답변 유효성 검증
     * 
     * @param status 현재 배틀 방 상태
     * @param participants 참가자 목록
     * @param participant 답변한 참가자
     * @param questionIndex 문제 인덱스
     * @param currentQuestionIndex 현재 진행 중인 문제 인덱스
     * @return 유효한 답변이면 true, 아니면 false
     */
    public boolean validateParticipantAnswer(BattleRoomStatus status, Set<BattleParticipant> participants,
                                           BattleParticipant participant, int questionIndex, int currentQuestionIndex) {
        if (!participants.contains(participant)) {
            return false;
        }
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return false;
        }
        if (questionIndex < 0 || questionIndex >= currentQuestionIndex) {
            return false;
        }
        if (participant.hasAnsweredCurrentQuestion(questionIndex)) {
            return false;
        }
        return true;
    }

    /**
     * 참가자 점수 초기화
     * 
     * @param participants 참가자 목록
     * @param roomId 배틀 방 ID (로깅용)
     */
    public void initializeParticipantScores(Set<BattleParticipant> participants, Long roomId) {
        for (BattleParticipant participant : participants) {
            if (participant.isActive()) {
                int oldScore = participant.getCurrentScore();
                participant.resetScore();
                participant.resetStreak();
                log.info("배틀 시작 시 참가자 점수 초기화: roomId={}, userId={}, 이전점수={}, 현재점수={}",
                        roomId, participant.getUser().getId(), oldScore, participant.getCurrentScore());
            }
        }
    }

    /**
     * 현재 참가자 수가 제한에 도달했는지 확인
     * 
     * @param participants 참가자 목록
     * @param maxParticipants 최대 참가자 수
     * @return 제한 도달 시 true, 아니면 false
     */
    private boolean isParticipantLimitReached(Set<BattleParticipant> participants, int maxParticipants) {
        return participants.size() >= maxParticipants;
    }

    /**
     * 특정 사용자가 이미 참가중인지 확인
     * 
     * @param participants 참가자 목록
     * @param user 확인할 사용자
     * @return 참가 중이면 true, 아니면 false
     */
    private boolean hasParticipant(Set<BattleParticipant> participants, User user) {
        return participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }
}