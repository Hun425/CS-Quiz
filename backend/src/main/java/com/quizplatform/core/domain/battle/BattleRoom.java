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

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "winner_id")
    private BattleParticipant winner;  // 승자 필드 추가

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BattleRoomStatus status;

    @Column(name = "max_participants")
    private int maxParticipants;

    @Column(name = "current_question_index")
    private int currentQuestionIndex = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "current_question_start_time")
    private LocalDateTime currentQuestionStartTime;

    @OneToMany(mappedBy = "battleRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BattleParticipant> participants = new HashSet<>();

    @Column(name = "room_code")
    private String roomCode;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Builder
    public BattleRoom(Quiz quiz, int maxParticipants) {
        this.quiz = quiz;
        this.maxParticipants = Math.max(maxParticipants, MIN_PARTICIPANTS);
        this.status = BattleRoomStatus.WAITING;
        this.roomCode = generateRoomCode();
    }

    /**
     * 참가자의 답변 유효성 검증 개선
     */
    public boolean validateParticipantAnswer(BattleParticipant participant, Question question, int questionIndex) {
        // 참가자가 이 방의 참가자인지 확인
        if (!this.participants.contains(participant)) {
            return false;
        }

        // 게임이 진행 중인지 확인
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return false;
        }

        // 답변하려는 문제가 이전 문제인지 확인 (0번부터 currentQuestionIndex-1번까지가 가능)
        if (questionIndex < 0 || questionIndex >= currentQuestionIndex) {
            return false;
        }

        // 이미 답변했는지 확인
        if (participant.hasAnsweredCurrentQuestion(questionIndex)) {
            return false;
        }

        return true;
    }


    // 참가자 추가 메서드
    public BattleParticipant addParticipant(User user) {
        validateParticipantAddition(user);

        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(this)
                .user(user)
                .build();

        participants.add(participant);

        // 참가자가 다 모였고 모두 준비 상태라면 게임 시작
        if (isReadyToStart()) {
            startBattle();
        }

        return participant;
    }

    // 참가자 추가 유효성 검사
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

    // 배틀 시작 가능 여부 확인
    public boolean isReadyToStart() {
        return participants.size() >= MIN_PARTICIPANTS &&
                participants.size() <= maxParticipants &&
                participants.stream().allMatch(BattleParticipant::isReady);
    }

    // 배틀 시작
    public void startBattle() {
        if (status != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }

        if (!isReadyToStart()) {
            throw new BusinessException(ErrorCode.NOT_READY_TO_START);
        }

        this.status = BattleRoomStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.currentQuestionStartTime = this.startTime;
        startNextQuestion();
    }

    /**
     * 다음 문제로 진행
     * 중복 호출 방지 및 정확한 상태 검증 추가
     */
    public Question startNextQuestion() {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "배틀이 진행 중이 아닙니다.");
        }

        List<Question> questions = getQuestions();
        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈에 문제가 없습니다.");
        }

        // 문제 인덱스 유효성 검사 추가
        if (currentQuestionIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "잘못된 문제 순서입니다: " + currentQuestionIndex);
        }

        // 인덱스가 범위를 벗어나는지 검사
        if (currentQuestionIndex >= questions.size()) {
            // 이미 마지막 문제까지 진행된 경우 게임 종료 처리
            finishBattle();
            return null;
        }

        // 다음 문제 가져오기 (인덱스는 startNextQuestion 호출 시점에 증가)
        Question nextQuestion = questions.get(currentQuestionIndex);

        // 문제 시작 시간 기록
        this.currentQuestionStartTime = LocalDateTime.now();

        // 문제 인덱스 증가 (현재 문제를 처리한 후에 인덱스 증가)
        currentQuestionIndex++;

        return nextQuestion;
    }

    // 현재 문제의 남은 시간 계산
    public Duration getRemainingTimeForCurrentQuestion() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return Duration.ZERO;
        }

        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return Duration.ZERO;
        }

        LocalDateTime deadline = currentQuestionStartTime
                .plusSeconds(currentQuestion.getTimeLimitSeconds());
        return Duration.between(LocalDateTime.now(), deadline);
    }

    // 현재 문제의 제한 시간 초과 여부 확인
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
     * 현재 문제(아직 인덱스가 증가하지 않은)에 대한 답변 확인 로직 수정
     */
    public boolean allParticipantsAnswered() {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return false;
        }

        // 문제가 진행되지 않은 경우
        if (currentQuestionIndex < 0) {
            return false;
        }

        // 참가자 수가 0인 경우 체크
        if (participants.isEmpty()) {
            return false;
        }

        // 활성 참가자만 고려
        long activeParticipants = participants.stream()
                .filter(BattleParticipant::isActive)
                .count();

        if (activeParticipants == 0) {
            return false;
        }

        /// 수정된 부분: 각 참가자가 최소 currentQuestionIndex만큼 답변했는지 확인
        // 즉, 첫 번째 문제(인덱스 0)면 최소 1개, 두 번째 문제(인덱스 1)면 최소 1개의 답변이 필요
        boolean result = participants.stream()
                .filter(BattleParticipant::isActive)
                .allMatch(p -> p.getAnswers().size() >= currentQuestionIndex);

        log.info("모든 참가자 답변 여부: 결과={}, 현재문제인덱스={}, 참가자수={}, 답변상태={}",
                result, currentQuestionIndex, activeParticipants,
                participants.stream()
                        .filter(BattleParticipant::isActive)
                        .map(p -> p.getUser().getId() + ":" + p.getAnswers().size())
                        .collect(Collectors.joining(", ")));

        return result;
    }

    // 배틀 종료
    public void finishBattle() {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }

        this.status = BattleRoomStatus.FINISHED;
        this.endTime = LocalDateTime.now();

        // 승자 결정 및 보상 처리
        determineWinnerAndRewards();
    }

    // 승자 결정 및 보상 처리
    private void determineWinnerAndRewards() {
        BattleParticipant winner = participants.stream()
                .max(Comparator.comparingInt(BattleParticipant::getCurrentScore))
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PARTICIPANTS));

        // 승자에게 추가 보상 점수 부여
        int bonusPoints = calculateWinnerBonus(winner);
        winner.addBonusPoints(bonusPoints);
    }

    // 승자 보너스 점수 계산
    private int calculateWinnerBonus(BattleParticipant winner) {
        // 기본 승리 보너스
        int bonus = 50;

        // 완벽한 승리 보너스 (모든 문제 정답)
        if (winner.hasAllCorrectAnswers()) {
            bonus += 30;
        }

        // 빠른 답변 보너스
        Duration avgAnswerTime = winner.getAverageAnswerTime();
        if (avgAnswerTime.getSeconds() < 30) {
            bonus += 20;
        }

        return bonus;
    }

    // 현재 참가자 수가 제한에 도달했는지 확인
    public boolean isParticipantLimitReached() {
        return participants.size() >= maxParticipants;
    }

    // 특정 사용자가 이미 참가중인지 확인
    public boolean hasParticipant(User user) {
        return participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    // 방 코드 생성
    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 현재 진행 상황 조회
    public BattleProgress getProgress() {
        return new BattleProgressBuilder(this).build();
    }

    // 참가자별 점수 조회
    private Map<Long, Integer> getParticipantScores() {
        return participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        BattleParticipant::getCurrentScore
                ));
    }

    // 특정 인덱스의 문제 시작 시간 계산
    public LocalDateTime getQuestionStartTimeForIndex(int questionIndex) {
        if (questionIndex < 0 || questionIndex >= quiz.getQuestions().size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE);
        }

        LocalDateTime baseTime = startTime;
        // 해당 문제 이전까지의 모든 문제의 시간 제한을 합산
        for (int i = 0; i < questionIndex; i++) {
            Question question = quiz.getQuestions().get(i);
            baseTime = baseTime.plusSeconds(question.getTimeLimitSeconds());
        }

        return baseTime;
    }

    // 배틀 설정 유효성 검사
    public void validateBattleSettings() {
        if (quiz == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }

        if (maxParticipants < MIN_PARTICIPANTS) {
            throw new BusinessException(ErrorCode.INVALID_PARTICIPANT_COUNT,
                    String.format("최소 %d명의 참가자가 필요합니다.", MIN_PARTICIPANTS));
        }

        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈에 문제가 없습니다.");
        }
    }

        /**
         * 현재 진행 중인 문제를 반환합니다.
         * 이 메서드는 다음과 같은 경우들을 처리합니다:
         * 1. 배틀이 시작되지 않은 경우
         * 2. 모든 문제가 끝난 경우
         * 3. 정상적으로 진행 중인 경우
         *
         * @return 현재 문제 객체, 또는 해당되는 문제가 없는 경우 null
         * @throws BusinessException 배틀룸의 상태가 유효하지 않은 경우
         */
    /**
     * 현재 문제 검색 메서드 개선
     * 안전하게 현재 문제를 반환
     */
    public Question getCurrentQuestion() {
        // 배틀이 진행 중이 아닌 경우
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return null;
        }

        // 퀴즈가 없는 경우 체크
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }

        List<Question> questions = quiz.getQuestions();

        // 인덱스가 범위를 벗어나는지 확인
        if (currentQuestionIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "잘못된 문제 순서입니다: " + currentQuestionIndex);
        }

        // 모든 문제를 이미 다 풀었는지 확인
        if (currentQuestionIndex >= questions.size()) {
            return null;
        }

        return questions.get(currentQuestionIndex);
    }

        /**
         * 특정 인덱스의 문제를 반환합니다.
         * 이 메서드는 주로 이전 문제를 참조하거나 다음 문제를 미리 확인할 때 사용됩니다.
         *
         * @param index 조회할 문제의 인덱스
         * @return 해당 인덱스의 문제 객체
         * @throws BusinessException 유효하지 않은 인덱스인 경우
         */
        public Question getQuestionByIndex(int index) {
            if (quiz == null || quiz.getQuestions() == null) {
                throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
            }

            List<Question> questions = quiz.getQuestions();

            if (index < 0 || index >= questions.size()) {
                throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                        String.format("잘못된 문제 인덱스입니다: %d, 전체 문제 수: %d", index, questions.size()));
            }

            return questions.get(index);
        }

        /**
         * 다음 문제가 있는지 확인합니다.
         *
         * @return 다음 문제 존재 여부
         */
        public boolean hasNextQuestion() {
            return quiz != null &&
                    quiz.getQuestions() != null &&
                    currentQuestionIndex < quiz.getQuestions().size() - 1;
        }

        /**
         * 이전 문제가 있는지 확인합니다.
         *
         * @return 이전 문제 존재 여부
         */
        public boolean hasPreviousQuestion() {
            return currentQuestionIndex > 0;
        }

        /**
         * 현재 문제의 시간 제한을 반환합니다.
         *
         * @return 현재 문제의 시간 제한 (초 단위)
         * @throws BusinessException 현재 문제를 찾을 수 없는 경우
         */
        public int getCurrentQuestionTimeLimit() {
            Question currentQuestion = getCurrentQuestion();
            if (currentQuestion == null) {
                throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "현재 진행 중인 문제가 없습니다.");
            }
            return currentQuestion.getTimeLimitSeconds();
        }

    /**
     * 이미 풀었던 특정 인덱스의 문제 조회
     * 중요: currentQuestionIndex는 다음에 풀 문제의 인덱스를 가리킴
     */
    public Question getPreviousQuestion(int completedIndex) {
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }

        List<Question> questions = quiz.getQuestions();

        if (completedIndex < 0 || completedIndex >= questions.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                    String.format("잘못된 문제 인덱스입니다: %d, 전체 문제 수: %d", completedIndex, questions.size()));
        }

        return questions.get(completedIndex);
    }

    /**
     * 현재 문제가 마지막 문제인지 확인합니다.
     */
    public boolean isLastQuestion() {
        return currentQuestionIndex == quiz.getQuestions().size() - 1;
    }


    /**
     * 남은 시간(초)을 계산합니다.
     */
    public int getRemainingTimeSeconds() {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return 0;
        }

        Question currentQuestion = getCurrentQuestion();
        if (currentQuestion == null) {
            return 0;
        }

        long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                currentQuestionStartTime.plusSeconds(currentQuestion.getTimeLimitSeconds())
        ).getSeconds();

        return (int) Math.max(0, remainingSeconds);
    }

    /**
     * 모든 문제 목록을 반환합니다.
     */
    public List<Question> getQuestions() {
        return quiz.getQuestions();
    }


    /**
     * 총 소요 시간(초)을 계산합니다.
     */
    public int getTotalTimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        LocalDateTime endDateTime = endTime != null ? endTime : LocalDateTime.now();
        return (int) java.time.Duration.between(startTime, endDateTime).getSeconds();
    }

    /**
     * 부분 Setter 구현
     * @param status
     */
    public void setStatus(BattleRoomStatus status) {
        this.status = status;
    }

    public void setQuiz(Quiz quiz){
        this.quiz = quiz;
    }
}