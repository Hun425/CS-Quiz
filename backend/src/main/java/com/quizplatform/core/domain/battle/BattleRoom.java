package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.progess.BattleProgress;
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
import java.util.stream.Collectors;

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
        if (!this.participants.contains(participant)) {
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
     * 참가자 추가
     * 
     * @param user 참가할 사용자
     * @return 생성된 배틀 참가자
     */
    public BattleParticipant addParticipant(User user) {
        validateParticipantAddition(user);
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
     * 참가자 추가 유효성 검사
     * 
     * @param user 참가 예정 사용자
     * @throws BusinessException 참가 불가능한 상태일 경우
     */
    private void validateParticipantAddition(User user) {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (isParticipantLimitReached()) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL);
        }
        if (hasParticipant(user)) {
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
     * 
     * @throws BusinessException 시작 불가능한 상태일 경우
     */
    public void startBattle() {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isReadyToStart()) {
            throw new BusinessException(ErrorCode.NOT_READY_TO_START);
        }
        
        // 명시적으로 모든 참가자의 점수와 상태를 초기화
        for (BattleParticipant participant : this.participants) {
            if (participant.isActive()) {
                int oldScore = participant.getCurrentScore();
                participant.resetScore();
                participant.resetStreak();
                log.info("배틀 시작 시 참가자 점수 초기화: roomId={}, userId={}, 이전점수={}, 현재점수={}",
                        this.getId(), participant.getUser().getId(), oldScore, participant.getCurrentScore());
            }
        }
        
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
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }
        List<Question> questions = getQuestions();
        log.info("startNextQuestion 호출 - 시작: roomId={}, 현재 인덱스={}, 문제 목록 크기={}",
                this.getId(), currentQuestionIndex, questions.size());
        if (currentQuestionIndex + 1 >= questions.size()) {
            log.info("더 이상 문제가 없습니다. 게임 종료: roomId={}", this.getId());
            return null;
        }
        currentQuestionIndex++;
        log.info("인덱스 증가: {} -> {}", currentQuestionIndex - 1, currentQuestionIndex);
        Question nextQuestion = questions.get(currentQuestionIndex);
        log.info("다음 문제 선택: roomId={}, 인덱스={}, ID={}",
                this.getId(), currentQuestionIndex, nextQuestion.getId());
        this.currentQuestionStartTime = LocalDateTime.now();
        return nextQuestion;
    }

    /**
     * 현재 문제의 남은 시간 계산
     * 
     * @return 남은 시간
     */
    public Duration getRemainingTimeForCurrentQuestion() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return Duration.ZERO;
        }
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return Duration.ZERO;
        }
        LocalDateTime deadline = currentQuestionStartTime.plusSeconds(currentQuestion.getTimeLimitSeconds());
        return Duration.between(LocalDateTime.now(), deadline);
    }

    /**
     * 현재 문제의 제한 시간 초과 여부 확인
     * 
     * @return 시간 초과면 true, 아니면 false
     */
    public boolean isCurrentQuestionTimeExpired() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return true;
        }
        Question currentQuestion = getCurrentQuestion();
        return currentQuestion != null &&
                currentQuestion.isTimeExpired(currentQuestionStartTime);
    }

    /**
     * 모든 참가자의 답변 완료 여부 확인
     * 
     * @return 모두 답변했으면 true, 아니면 false
     */
    public boolean allParticipantsAnswered() {
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
        boolean isLastQuestion = currentQuestionIndex >= getQuestions().size() - 1;
        if (isLastQuestion) {
            log.info("마지막 문제 참가자 답변 여부 확인: roomId={}, 현재인덱스={}", this.getId(), currentQuestionIndex);
            Long lastQuestionId = getQuestions().get(getQuestions().size() - 1).getId();
            boolean result = participants.stream()
                    .filter(BattleParticipant::isActive)
                    .allMatch(p -> p.getAnswers().stream()
                            .anyMatch(a -> a.getQuestion().getId().equals(lastQuestionId)));
            log.info("마지막 문제 참가자 답변 여부 결과: roomId={}, 결과={}", this.getId(), result);
            return result;
        }
        boolean result = participants.stream()
                .filter(BattleParticipant::isActive)
                .allMatch(p -> p.getAnswers().size() >= currentQuestionIndex + 1);
        log.info("모든 참가자 답변 여부: 결과={}, 현재문제인덱스={}, 참가자수={}, 답변상태={}",
                result, currentQuestionIndex, activeParticipants,
                participants.stream()
                        .filter(BattleParticipant::isActive)
                        .map(p -> p.getUser().getId() + ":" + p.getAnswers().size())
                        .collect(Collectors.joining(", ")));
        return result;
    }

    /**
     * 배틀 종료
     * 
     * @throws BusinessException 진행 중이 아닌 상태일 경우
     */
    public void finishBattle() {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }
        this.status = BattleRoomStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        determineWinnerAndRewards();
    }

    /**
     * 승자 결정 및 보상 처리
     * 
     * @throws BusinessException 참가자가 없을 경우
     */
    private void determineWinnerAndRewards() {
        BattleParticipant winner = participants.stream()
                .max(Comparator.comparingInt(BattleParticipant::getCurrentScore))
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PARTICIPANTS));
        int bonusPoints = calculateWinnerBonus(winner);
        winner.addBonusPoints(bonusPoints);
    }

    /**
     * 승자 보너스 점수 계산
     * 
     * @param winner 배틀 승자
     * @return 보너스 점수
     */
    private int calculateWinnerBonus(BattleParticipant winner) {
        int bonus = 50;
        if (winner.hasAllCorrectAnswers()) {
            bonus += 30;
        }
        Duration avgAnswerTime = winner.getAverageAnswerTime();
        if (avgAnswerTime.getSeconds() < 30) {
            bonus += 20;
        }
        return bonus;
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
    private Map<Long, Integer> getParticipantScores() {
        return participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        BattleParticipant::getCurrentScore
                ));
    }

    /**
     * 특정 인덱스의 문제 시작 시간 계산
     * 
     * @param questionIndex 문제 인덱스
     * @return 시작 시간
     * @throws BusinessException 유효하지 않은 인덱스일 경우
     */
    public LocalDateTime getQuestionStartTimeForIndex(int questionIndex) {
        List<Question> questionsList = getQuestions();
        if (questionIndex < 0 || questionIndex >= questionsList.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE);
        }
        LocalDateTime baseTime = startTime;
        for (int i = 0; i < questionIndex; i++) {
            Question question = questionsList.get(i);
            baseTime = baseTime.plusSeconds(question.getTimeLimitSeconds());
        }
        return baseTime;
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
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return null;
        }
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
        List<Question> questions = getQuestions();
        if (currentQuestionIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "잘못된 문제 순서입니다: " + currentQuestionIndex);
        }
        if (currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    /**
     * 특정 인덱스의 문제를 반환
     * 
     * @param index 문제 인덱스
     * @return 해당 인덱스의 문제
     * @throws BusinessException 인덱스가 범위를 벗어날 경우
     */
    public Question getQuestionByIndex(int index) {
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
        List<Question> questions = getQuestions();
        if (index < 0 || index >= questions.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                    String.format("잘못된 문제 인덱스입니다: %d, 전체 문제 수: %d", index, questions.size()));
        }
        return questions.get(index);
    }

    /**
     * 이전 문제를 반환
     * 
     * @param completedIndex 완료된 문제 인덱스
     * @return 해당 인덱스의 문제
     * @throws BusinessException 인덱스가 범위를 벗어날 경우
     */
    public Question getPreviousQuestion(int completedIndex) {
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
        List<Question> questions = getQuestions();
        if (completedIndex < 0 || completedIndex >= questions.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                    String.format("잘못된 문제 인덱스입니다: %d, 전체 문제 수: %d", completedIndex, questions.size()));
        }
        return questions.get(completedIndex);
    }

    /**
     * 현재 문제가 마지막 문제인지 확인
     * 
     * @return 마지막 문제면 true, 아니면 false
     */
    public boolean isLastQuestion() {
        return currentQuestionIndex == getQuestions().size() - 1;
    }

    /**
     * 남은 시간(초)을 계산
     * 
     * @return 남은 시간(초)
     */
    public int getRemainingTimeSeconds() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return 0;
        }
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return 0;
        }
        long remainingSeconds = Duration.between(
                LocalDateTime.now(),
                currentQuestionStartTime.plusSeconds(currentQuestion.getTimeLimitSeconds())
        ).getSeconds();
        return (int) Math.max(0, remainingSeconds);
    }

    /**
     * 모든 문제 목록을 반환
     * 
     * @return 문제 리스트
     */
    public List<Question> getQuestions() {
        return new ArrayList<>(quiz.getQuestions());
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
        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "현재 진행 중인 문제가 없습니다.");
        }
        return currentQuestion.getTimeLimitSeconds();
    }
}