package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.progress.BattleProgress;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
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

/**
 * 배틀 방 엔티티 클래스
 * 
 * <p>사용자 간 퀴즈 대결을 진행하는 가상의 방을 관리합니다.
 * 참가자, 문제, 상태, 타이머 등 배틀 진행에 필요한 모든 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "battle_rooms")
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
    
    // 관리자 클래스들 (JPA에서 제외)
    @Transient
    private final BattleRoomStateManager stateManager = new BattleRoomStateManager();
    @Transient
    private final BattleQuestionManager questionManager = new BattleQuestionManager();
    @Transient
    private final BattleScoreCalculator scoreCalculator = new BattleScoreCalculator();

    /**
     * 배틀 승자
     */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "winner_id")
    private BattleParticipant winner;

    /**
     * 배틀 방 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배틀에 사용되는 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

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
     * 배틀 방 생성자
     * 
     * @param quiz 사용할 퀴즈
     * @param maxParticipants 최대 참가자 수
     * @param creatorId 방 생성자 ID
     */
    @Builder
    public BattleRoom(Quiz quiz, int maxParticipants, Long creatorId) {
        this.quiz = quiz;
        this.maxParticipants = Math.max(maxParticipants, MIN_PARTICIPANTS);
        this.status = BattleRoomStatus.WAITING;
        this.roomCode = generateRoomCode();
        this.creatorId = creatorId;
    }

    /**
     * 참가자의 답변 유효성 검증
     * 
     * @param participant 답변한 참가자
     * @param question 답변한 문제
     * @param questionIndex 문제 인덱스
     * @return 유효한 답변이면 true, 아니면 false
     */
    public boolean validateParticipantAnswer(BattleParticipant participant, Question question, int questionIndex) {
        return stateManager.validateParticipantAnswer(status, participants, participant, questionIndex, currentQuestionIndex);
    }

    /**
     * 참가자 추가
     * 
     * @param user 참가할 사용자
     * @return 생성된 배틀 참가자
     */
    public BattleParticipant addParticipant(User user) {
        stateManager.validateParticipantAddition(status, participants, maxParticipants, user);
        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(this)
                .user(user)
                .build();
        participants.add(participant);
        if (isReadyToStart()) {
            startBattle();
        }
        return participant;
    }


    /**
     * 배틀 시작 가능 여부 확인
     * 
     * @return 시작 가능하면 true, 아니면 false
     */
    public boolean isReadyToStart() {
        return stateManager.isReadyToStart(status, participants, maxParticipants);
    }

    /**
     * 배틀 시작
     * 
     * @throws BusinessException 시작 불가능한 상태일 경우
     */
    public void startBattle() {
        stateManager.validateBattleStart(status, participants, maxParticipants);
        
        // 참가자 점수 초기화
        stateManager.initializeParticipantScores(participants, this.getId());
        
        this.currentQuestionIndex = -1; // 인덱스 초기화
        this.status = BattleRoomStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.currentQuestionStartTime = this.startTime;
        Question firstQuestion = startNextQuestion();
        log.info("배틀 시작됨: roomId={}, 첫 문제 인덱스={}", this.getId(), currentQuestionIndex);
    }

    /**
     * 다음 문제 시작
     * 
     * @return 다음 문제, 없으면 null
     * @throws BusinessException 진행 중이 아닌 상태일 경우
     */
    public Question startNextQuestion() {
        stateManager.validateBattleInProgress(status);
        
        BattleQuestionManager.QuestionTransitionResult result = 
                questionManager.startNextQuestion(quiz, currentQuestionIndex, this.getId());
        
        if (result.isFinished()) {
            return null;
        }
        
        this.currentQuestionIndex = result.getNewQuestionIndex();
        this.currentQuestionStartTime = LocalDateTime.now();
        return result.getNextQuestion();
    }

    /**
     * 현재 문제의 남은 시간 계산
     * 
     * @return 남은 시간
     */
    public Duration getRemainingTimeForCurrentQuestion() {
        return questionManager.getRemainingTimeForCurrentQuestion(quiz, status, currentQuestionIndex, currentQuestionStartTime);
    }

    /**
     * 현재 문제의 제한 시간 초과 여부 확인
     * 
     * @return 시간 초과면 true, 아니면 false
     */
    public boolean isCurrentQuestionTimeExpired() {
        return questionManager.isCurrentQuestionTimeExpired(quiz, status, currentQuestionIndex, currentQuestionStartTime);
    }

    /**
     * 모든 참가자의 답변 완료 여부 확인
     * 
     * @return 모두 답변했으면 true, 아니면 false
     */
    public boolean allParticipantsAnswered() {
        List<Question> questions = questionManager.getQuestions(quiz);
        return stateManager.allParticipantsAnswered(status, participants, currentQuestionIndex, questions.size());
    }

    /**
     * 배틀 종료
     * 
     * @throws BusinessException 진행 중이 아닌 상태일 경우
     */
    public void finishBattle() {
        stateManager.validateBattleInProgress(status);
        this.status = BattleRoomStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        this.winner = scoreCalculator.determineWinnerAndCalculateRewards(participants);
    }



    /**
     * 현재 참가자 수가 제한에 도달했는지 확인
     * 
     * @return 제한 도달 시 true, 아니면 false
     */
    public boolean isParticipantLimitReached() {
        return participants.size() >= maxParticipants;
    }

    /**
     * 특정 사용자가 이미 참가중인지 확인
     * 
     * @param user 확인할 사용자
     * @return 참가 중이면 true, 아니면 false
     */
    public boolean hasParticipant(User user) {
        return participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    /**
     * 방 코드 생성
     * 
     * @return 생성된 방 코드
     */
    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 현재 진행 상황 조회
     * 
     * @return 배틀 진행 상황 객체
     */
    public BattleProgress getProgress() {
        return new BattleProgressBuilder(this).build();
    }

    /**
     * 참가자별 점수 조회
     * 
     * @return 사용자 ID를 키로, 점수를 값으로 하는 맵
     */
    public Map<Long, Integer> getParticipantScores() {
        return scoreCalculator.getParticipantScores(participants);
    }

    /**
     * 특정 인덱스의 문제 시작 시간 계산
     * 
     * @param questionIndex 문제 인덱스
     * @return 시작 시간
     * @throws BusinessException 유효하지 않은 인덱스일 경우
     */
    public LocalDateTime getQuestionStartTimeForIndex(int questionIndex) {
        return questionManager.getQuestionStartTimeForIndex(quiz, questionIndex, startTime);
    }

    /**
     * 배틀 설정 유효성 검사
     * 
     * @throws BusinessException 설정이 유효하지 않을 경우
     */
    public void validateBattleSettings() {
        if (quiz == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
        if (maxParticipants < MIN_PARTICIPANTS) {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPANT_COUNT,
                    String.format("최소 %d명의 참가자가 필요합니다.", MIN_PARTICIPANTS));
        }
        if (quiz.getQuestions().isEmpty()) {  // Set이므로 isEmpty() 그대로 사용 가능
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈에 문제가 없습니다.");
        }
    }

    /**
     * 현재 진행 중인 문제를 반환
     * 
     * @return 현재 문제, 없으면 null
     * @throws BusinessException 퀴즈가 설정되지 않았거나 인덱스가 유효하지 않을 경우
     */
    public Question getCurrentQuestion() {
        return questionManager.getCurrentQuestion(quiz, status, currentQuestionIndex);
    }

    /**
     * 특정 인덱스의 문제를 반환
     * 
     * @param index 문제 인덱스
     * @return 해당 인덱스의 문제
     * @throws BusinessException 인덱스가 범위를 벗어날 경우
     */
    public Question getQuestionByIndex(int index) {
        return questionManager.getQuestionByIndex(quiz, index);
    }

    /**
     * 이전 문제를 반환
     * 
     * @param completedIndex 완료된 문제 인덱스
     * @return 해당 인덱스의 문제
     * @throws BusinessException 인덱스가 범위를 벗어날 경우
     */
    public Question getPreviousQuestion(int completedIndex) {
        return questionManager.getQuestionByIndex(quiz, completedIndex);
    }

    /**
     * 현재 문제가 마지막 문제인지 확인
     * 
     * @return 마지막 문제면 true, 아니면 false
     */
    public boolean isLastQuestion() {
        return questionManager.isLastQuestion(quiz, currentQuestionIndex);
    }

    /**
     * 남은 시간(초)을 계산
     * 
     * @return 남은 시간(초)
     */
    public int getRemainingTimeSeconds() {
        return questionManager.getRemainingTimeSeconds(quiz, status, currentQuestionIndex, currentQuestionStartTime);
    }

    /**
     * 모든 문제 목록을 반환
     * 
     * @return 문제 리스트
     */
    public List<Question> getQuestions() {
        return questionManager.getQuestions(quiz);
    }

    /**
     * 총 소요 시간(초)을 계산
     * 
     * @return 소요 시간(초)
     */
    public int getTotalTimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        LocalDateTime endDateTime = endTime != null ? endTime : LocalDateTime.now();
        return (int) Duration.between(startTime, endDateTime).getSeconds();
    }

    /**
     * 상태 설정
     * 
     * @param status 설정할 상태
     */
    public void setStatus(BattleRoomStatus status) {
        this.status = status;
    }

    /**
     * 배틀 방 생성자 ID
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 퀴즈 설정
     * 
     * @param quiz 설정할 퀴즈
     */
    public void setQuiz(Quiz quiz){
        this.quiz = quiz;
    }

    /**
     * 현재 문제의 제한 시간 조회
     * 
     * @return 제한 시간(초)
     * @throws BusinessException 현재 진행 중인 문제가 없을 경우
     */
    public int getCurrentQuestionTimeLimit() {
        return questionManager.getCurrentQuestionTimeLimit(quiz, status, currentQuestionIndex);
    }
}