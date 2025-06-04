package com.quizplatform.battle.domain.model;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 배틀 방 엔티티 클래스
 * 
 * <p>사용자 간 퀴즈 대결을 진행하는 가상의 방을 관리합니다.
 * 참가자, 문제, 상태, 타이머 등 배틀 진행에 필요한 모든 정보를 포함합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "battle_rooms", schema = "battle_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class BattleRoom {
    // 기본 상수 정의
    private static final int MIN_PARTICIPANTS = 2;
    private static final int DEFAULT_TIME_LIMIT_MINUTES = 30;
    private static final int READY_TIMEOUT_SECONDS = 30;
    private static final int QUESTION_TRANSITION_SECONDS = 5;

    /**
     * 배틀 방 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 퀴즈 ID (Quiz 모듈 참조)
     */
    @Column(name = "quiz_id")
    private Long quizId;

    /**
     * 배틀 방 상태 (WAITING, IN_PROGRESS, FINISHED 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BattleRoomStatus status;

    /**
     * 최대 참가자 수
     */
    @Column(name = "max_participants")
    private int maxParticipants;

    /**
     * 현재 진행 중인 문제 인덱스
     */
    @Column(name = "current_question_index")
    private int currentQuestionIndex = 0;

    /**
     * 배틀 시작 시간
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 배틀 종료 시간
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 현재 문제 시작 시간
     */
    @Column(name = "current_question_start_time")
    private LocalDateTime currentQuestionStartTime;

    /**
     * 배틀 참가자 목록
     */
    @OneToMany(mappedBy = "battleRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BattleParticipant> participants = new HashSet<>();

    /**
     * 배틀 방 고유 코드
     */
    @Column(name = "room_code")
    private String roomCode;

    /**
     * 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 낙관적 락 버전
     */
    @Version
    private Long version;

    /**
     * 방 생성자 ID (User 모듈 참조)
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 배틀 승자 
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private BattleParticipant winner;

    /**
     * 총 문제 수
     */
    @Column(name = "total_questions")
    private int totalQuestions;

    /**
     * 문제별 시간 제한 (초)
     */
    @Column(name = "question_time_limit_seconds")
    private Integer questionTimeLimitSeconds;

    /**
     * 배틀 방 생성자
     * 
     * @param quizId 사용할 퀴즈 ID
     * @param maxParticipants 최대 참가자 수
     * @param creatorId 방 생성자 ID
     * @param totalQuestions 문제 총 수
     * @param questionTimeLimitSeconds 문제 당 시간 제한 (초)
     */
    @Builder
    public BattleRoom(Long quizId, int maxParticipants, Long creatorId, 
                     int totalQuestions, Integer questionTimeLimitSeconds) {
        this.quizId = quizId;
        this.maxParticipants = Math.max(maxParticipants, MIN_PARTICIPANTS);
        this.status = BattleRoomStatus.WAITING;
        this.roomCode = generateRoomCode();
        this.creatorId = creatorId;
        this.totalQuestions = totalQuestions;
        this.questionTimeLimitSeconds = questionTimeLimitSeconds;
    }

    /**
     * 참가자 추가
     * 
     * @param userId 참가할 사용자 ID
     * @param username 사용자명
     * @param profileImage 프로필 이미지
     * @return 생성된 배틀 참가자
     */
    public BattleParticipant addParticipant(Long userId, String username, String profileImage) {
        validateParticipantAddition(userId);
        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(this)
                .userId(userId)
                .username(username)
                .profileImage(profileImage)
                .build();
        participants.add(participant);
        return participant;
    }

    /**
     * 참가자 추가 유효성 검사
     * 
     * @param userId 참가 예정 사용자 ID
     */
    private void validateParticipantAddition(Long userId) {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (isParticipantLimitReached()) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL);
        }
        if (hasParticipant(userId)) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING);
        }
    }

    /**
     * 배틀 시작 가능 여부 확인
     * 
     * @return 시작 가능하면 true, 아니면 false
     */
    public boolean isReadyToStart() {
        return participants.size() >= MIN_PARTICIPANTS &&
                participants.size() <= maxParticipants &&
                participants.stream().allMatch(BattleParticipant::isReady);
    }

    /**
     * 배틀 시작
     */
    public void startBattle() {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isReadyToStart()) {
            throw new BusinessException(ErrorCode.NOT_READY_TO_START);
        }
        this.currentQuestionIndex = -1; // 인덱스 초기화 (첫 번째 문제로 진행할 준비)
        this.status = BattleRoomStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.currentQuestionStartTime = this.startTime;
        
        // 모든 참가자 배틀 시작 상태로 설정
        participants.forEach(BattleParticipant::startBattle);
        
        // 첫 번째 문제로 진행
        startNextQuestion();
        
        log.info("배틀 시작됨: roomId={}, 첫 문제 인덱스={}", this.getId(), currentQuestionIndex);
    }

    /**
     * 다음 문제 시작
     */
    public void startNextQuestion() {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }
        
        // 다음 문제 인덱스로 진행
        currentQuestionIndex++;
        currentQuestionStartTime = LocalDateTime.now();
        
        // 모든 문제를 다 풀었으면 종료
        if (currentQuestionIndex >= totalQuestions) {
            finishBattle();
        }
    }

    /**
     * 현재 문제에 남은 시간 계산
     * 
     * @return 남은 시간
     */
    public Duration getRemainingTimeForCurrentQuestion() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return Duration.ZERO;
        }
        
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(currentQuestionStartTime, now);
        Duration timeLimit = Duration.ofSeconds(questionTimeLimitSeconds);
        
        return timeLimit.minus(elapsed).isNegative() ? Duration.ZERO : timeLimit.minus(elapsed);
    }

    /**
     * 현재 문제 시간 만료 여부 확인
     * 
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isCurrentQuestionTimeExpired() {
        return getRemainingTimeForCurrentQuestion().equals(Duration.ZERO);
    }

    /**
     * 모든 참가자가 현재 문제에 답변했는지 확인
     * 
     * @return 모두 답변했으면 true, 아니면 false
     */
    public boolean allParticipantsAnswered() {
        if (participants.isEmpty()) {
            return false;
        }
        
        return participants.stream()
                .filter(p -> !p.hasForfeited())
                .allMatch(p -> p.hasAnsweredCurrentQuestion(currentQuestionIndex));
    }

    /**
     * 참가자 제한 도달 여부 확인
     * 
     * @return 제한에 도달했으면 true, 아니면 false
     */
    public boolean isParticipantLimitReached() {
        return participants.size() >= maxParticipants;
    }

    /**
     * 특정 사용자의 참가 여부 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 참가 중이면 true, 아니면 false
     */
    public boolean hasParticipant(Long userId) {
        return participants.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
    }

    /**
     * 고유 방 코드 생성
     * 
     * @return 생성된 방 코드
     */
    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 참가자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 참가자 정보
     */
    public Optional<BattleParticipant> getParticipant(Long userId) {
        return participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 배틀 종료
     */
    public void finishBattle() {
        if (status == BattleRoomStatus.FINISHED) {
            return;
        }
        
        this.status = BattleRoomStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        
        // 모든 참가자의 배틀 종료 처리
        participants.forEach(BattleParticipant::finishBattle);
        
        // 순위 계산 및 승자 결정
        calculateRanksAndDetermineWinner();
        
        log.info("배틀 종료됨: roomId={}, 승자={}", this.getId(), 
                this.winner != null ? this.winner.getUsername() : "없음");
    }

    /**
     * 순위 계산 및 승자 결정
     */
    private void calculateRanksAndDetermineWinner() {
        List<BattleParticipant> rankedParticipants = participants.stream()
                .sorted(Comparator.comparing(BattleParticipant::getScore).reversed())
                .collect(Collectors.toList());
        
        // 순위 설정
        int rank = 1;
        double previousScore = -1;
        for (BattleParticipant participant : rankedParticipants) {
            // 동점자는 같은 순위 부여
            if (previousScore == participant.getScore()) {
                // 이전 참가자와 같은 순위
            } else {
                // 새로운 순위
                rank = rankedParticipants.indexOf(participant) + 1;
            }
            
            participant.setRank(rank);
            previousScore = participant.getScore();
        }
        
        // 승자 설정 (1등)
        if (!rankedParticipants.isEmpty()) {
            this.winner = rankedParticipants.get(0);
        }
    }

    /**
     * 배틀 취소
     */
    public void cancelBattle() {
        this.status = BattleRoomStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 배틀 상태 설정
     * 
     * @param status 새 상태
     */
    public void setStatus(BattleRoomStatus status) {
        this.status = status;
    }
} 