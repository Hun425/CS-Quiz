package com.quizplatform.modules.battle.domain.entity;

import com.quizplatform.shared_kernel.exception.BusinessException;
import com.quizplatform.shared_kernel.exception.ErrorCode;
import com.quizplatform.modules.battle.domain.vo.BattleRoomStatus;
import com.quizplatform.modules.quiz.domain.entity.Question;
import com.quizplatform.modules.quiz.domain.entity.Quiz;
import com.quizplatform.modules.user.domain.entity.User;
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
    private static final int DEFAULT_MAX_PARTICIPANTS = 4;
    private static final int DEFAULT_TIME_LIMIT_PER_QUESTION = 30; // Default seconds per question


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
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /**
     * 배틀 방 상태 (WAITING, IN_PROGRESS, FINISHED 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BattleRoomStatus status;

    /**
     * 최대 참가자 수
     */
    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    /**
     * 현재 진행 중인 문제 인덱스 (0부터 시작, -1은 시작 전)
     */
    @Column(name = "current_question_index", nullable = false)
    private int currentQuestionIndex = -1;

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
     * 문제당 제한 시간 (초)
     */
    @Column(name = "time_limit_per_question", nullable = false)
    private int timeLimitPerQuestion = DEFAULT_TIME_LIMIT_PER_QUESTION;

    /**
     * 배틀 참가자 목록
     * CascadeType.ALL: BattleRoom 저장/삭제 시 Participant도 함께 처리
     * orphanRemoval=true: BattleRoom의 participants 컬렉션에서 Participant 제거 시 DB에서도 삭제
     */
    @OneToMany(mappedBy = "battleRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BattleParticipant> participants = new HashSet<>();

    /**
     * 배틀 방 고유 코드 (필요시 사용, 현재 미사용)
     */
    @Column(name = "room_code", unique = true)
    private String roomCode;

    /**
     * 방 생성자 ID
     */
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

     /**
     * 배틀 승자 (optional=true -> nullable)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id") // participant id
    private BattleParticipant winner;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 낙관적 락 버전
     */
    @Version
    private Long version;

    /**
     * 배틀 방 생성자
     *
     * @param quiz 사용할 퀴즈 (Not null)
     * @param maxParticipants 최대 참가자 수
     * @param creatorId 방 생성자 ID (Not null)
     */
    @Builder
    public BattleRoom(Quiz quiz, int maxParticipants, Long creatorId) {
        if (quiz == null || creatorId == null) {
             log.error("Cannot create BattleRoom with null quiz or creatorId. Quiz: {}, CreatorId: {}", quiz, creatorId);
            throw new IllegalArgumentException("Quiz and Creator ID cannot be null");
        }
        this.quiz = quiz;
        this.maxParticipants = Math.max(maxParticipants > 0 ? maxParticipants : DEFAULT_MAX_PARTICIPANTS, MIN_PARTICIPANTS); // Ensure maxParticipants is at least MIN_PARTICIPANTS and positive, default if invalid
        // 퀴즈에 설정된 시간 제한이 있으면 사용, 없으면 기본값 사용
        this.timeLimitPerQuestion = quiz.getTimeLimit() != null && quiz.getTimeLimit() > 0
                                     ? quiz.getTimeLimit()
                                     : DEFAULT_TIME_LIMIT_PER_QUESTION;
        this.status = BattleRoomStatus.WAITING; // Use imported enum
        // this.roomCode = generateRoomCode(); // 필요시 활성화
        this.creatorId = creatorId;
        this.currentQuestionIndex = -1; // 명시적 초기화
    }

    // --- 유효성 검사 메서드 --- //

    /**
     * 생성된 배틀방 설정의 유효성을 검사합니다.
     * @throws BusinessException 설정이 유효하지 않을 경우
     */
    public void validateBattleSettings() {
         if (this.quiz == null || this.quiz.getQuestions() == null || this.quiz.getQuestions().isEmpty()) {
            log.warn("Attempted to create battle room with invalid quiz (null or no questions): quizId={}", this.quiz != null ? this.quiz.getId() : "null");
            throw new BusinessException(ErrorCode.INVALID_QUIZ_DATA, "유효하지 않은 퀴즈 데이터입니다."); // TODO: Use shared_kernel exception
        }
        if (this.maxParticipants < MIN_PARTICIPANTS) {
            log.warn("Attempted to create battle room with invalid maxParticipants: {}", this.maxParticipants);
            // Corrected the logic in the builder, but keep validation as a safeguard
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "최소 참가 인원은 " + MIN_PARTICIPANTS + "명입니다."); // TODO: Use shared_kernel exception
        }
        // 추가적인 검증 로직 (예: 퀴즈 상태 확인 등)
    }

    /**
     * 새로운 참가자를 추가하기 전에 유효성을 검사합니다. (BattleService에서 호출)
     *
     * @param user 추가하려는 사용자 (또는 userId)
     * @throws BusinessException 추가할 수 없는 상태일 경우
     */
    public void validateCanJoin(User user) { // Keep User for now, service layer passes it
        if (this.status != BattleRoomStatus.WAITING) { // Use imported enum
            log.warn("Cannot add participant, battle room not in WAITING state: roomId={}, status={}", this.id, this.status);
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작되었거나 종료된 대결방입니다."); // TODO: Use shared_kernel exception
        }
        if (isParticipantLimitReached()) {
             log.warn("Cannot add participant, battle room is full: roomId={}, current={}, max={}", this.id, this.participants.size(), this.maxParticipants);
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL, "대결방 정원이 가득 찼습니다."); // TODO: Use shared_kernel exception
        }
        if (hasParticipant(user)) {
            log.warn("Cannot add participant, user already in room: roomId={}, userId={}", this.id, user.getId());
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING, "이미 참가 중인 사용자입니다."); // TODO: Use shared_kernel exception
        }
    }

    // --- 상태 확인 메서드 --- //

    /**
     * 현재 배틀방 정원이 찼는지 확인합니다.
     * @return 정원이 찼으면 true, 아니면 false
     */
    public boolean isParticipantLimitReached() {
        return this.participants.size() >= this.maxParticipants;
    }

    /**
     * 특정 사용자가 이미 방에 참가 중인지 확인합니다.
     * @param user 확인할 사용자
     * @return 참가 중이면 true, 아니면 false
     */
    public boolean hasParticipant(User user) {
        if (user == null) return false;
        return this.participants.stream().anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }

    /**
     * 현재 문제가 마지막 문제인지 확인합니다.
     * @return 마지막 문제이면 true, 아니면 false
     */
    public boolean isLastQuestion() {
        if (this.quiz == null || this.quiz.getQuestions() == null) {
            return true; // 퀴즈 또는 질문이 없으면 마지막 문제로 간주
        }
        return this.currentQuestionIndex >= this.quiz.getQuestions().size() - 1;
    }

    /**
     * 현재 문제에 대한 남은 시간을 계산합니다.
     * @return 남은 시간 Duration, 진행 중이 아니거나 시간이 만료되었으면 Duration.ZERO
     */
    public Duration getRemainingTimeForCurrentQuestion() {
        if (this.status != BattleRoomStatus.IN_PROGRESS || this.currentQuestionStartTime == null) {
            return Duration.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = this.currentQuestionStartTime.plusSeconds(this.timeLimitPerQuestion);
        if (now.isAfter(deadline)) {
            return Duration.ZERO;
        }
        return Duration.between(now, deadline);
    }

    /**
     * 현재 문제의 제한 시간이 만료되었는지 확인합니다.
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isCurrentQuestionTimeExpired() {
         if (this.status != BattleRoomStatus.IN_PROGRESS || this.currentQuestionStartTime == null) {
             return false; // 진행 중이 아니면 만료되지 않음
         }
         LocalDateTime deadline = this.currentQuestionStartTime.plusSeconds(this.timeLimitPerQuestion);
         return LocalDateTime.now().isAfter(deadline);
     }

    // --- 상태 변경 메서드 --- //

    /**
     * 대결을 시작합니다. 상태를 IN_PROGRESS로 변경하고 시작 시간을 기록합니다.
     * 첫 번째 문제 인덱스(0)를 설정하고 문제 시작 시간을 기록합니다.
     * @throws BusinessException 이미 시작되었거나 대기 상태가 아닐 때
     */
    public void startBattle() {
        if (this.status != BattleRoomStatus.WAITING) {
            log.warn("Cannot start battle, room not in WAITING state: roomId={}, status={}", this.id, this.status);
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED_OR_ENDED, "대결을 시작할 수 없는 상태입니다."); // TODO: Use shared_kernel exception
        }
        // isReadyToStart 검사는 BattleService에서 수행

        this.status = BattleRoomStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.currentQuestionIndex = 0; // 첫 번째 문제 인덱스
        this.currentQuestionStartTime = LocalDateTime.now(); // 첫 문제 시작 시간
        log.info("Battle started: roomId={}", this.id);
    }

    /**
     * 다음 문제로 이동합니다. 현재 문제 인덱스를 증가시키고 문제 시작 시간을 업데이트합니다.
     * @return 다음 문제가 있으면 true, 없으면 false (마지막 문제였음)
     * @throws BusinessException 진행 중 상태가 아닐 때
     */
    public boolean moveToNextQuestion() {
         if (this.status != BattleRoomStatus.IN_PROGRESS) {
             log.warn("Cannot move to next question, battle not in progress: roomId={}, status={}", this.id, this.status);
             // Service 레이어에서 상태 확인 후 호출하므로 이론상 발생 안함
             // throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "대결이 진행 중이 아닙니다.");
             return false;
         }
        if (isLastQuestion()) {
            log.debug("Already at the last question: roomId={}, index={}", this.id, this.currentQuestionIndex);
            return false; // 이미 마지막 문제
        }
        this.currentQuestionIndex++;
        this.currentQuestionStartTime = LocalDateTime.now();
        log.debug("Moved to next question: roomId={}, index={}", this.id, this.currentQuestionIndex);
        return true;
    }

    /**
     * 대결을 종료합니다. 상태를 ENDED로 변경하고 종료 시간을 기록합니다.
     * 승자를 설정합니다.
     * @param winner 대결의 승자 (BattleParticipant), 승자가 없으면 null
     */
    public void endBattle(BattleParticipant winner) {
         // 종료 상태가 아닌 경우에만 처리 (이미 종료된 경우 로그만 남김)
        if (this.status != BattleRoomStatus.ENDED) {
             this.status = BattleRoomStatus.ENDED;
             this.endTime = LocalDateTime.now();
             this.winner = winner; // 승자 설정
             log.info("Battle ended: roomId={}, winnerId={}", this.id, winner != null ? winner.getId() : "None");
         } else {
             log.warn("Attempted to end an already ended battle: roomId={}", this.id);
         }
    }


    // --- 퀴즈 및 질문 관련 접근자 --- //

    /**
     * 현재 진행 중인 문제를 반환합니다.
     * @return 현재 Question 객체, 진행 중이 아니거나 인덱스 오류 시 null
     */
    public Question getCurrentQuestion() {
        if (this.status != BattleRoomStatus.IN_PROGRESS || this.currentQuestionIndex < 0) {
            return null;
        }
        return getQuestionByIndex(this.currentQuestionIndex);
    }

    /**
     * 특정 인덱스의 문제를 반환합니다.
     * @param index 조회할 문제 인덱스
     * @return 해당 인덱스의 Question 객체, 퀴즈/질문 없거나 인덱스 오류 시 null
     */
    public Question getQuestionByIndex(int index) {
        if (this.quiz == null || this.quiz.getQuestions() == null || index < 0 || index >= this.quiz.getQuestions().size()) {
             log.warn("Invalid question index requested or quiz/questions missing: roomId={}, requestedIndex={}, totalQuestions={}",
                     this.id, index, this.quiz != null && this.quiz.getQuestions() != null ? this.quiz.getQuestions().size() : "N/A");
            return null;
        }
        // 질문 목록이 List이며 순서가 보장된다고 가정
        // 주의: Set<Question>이고 순서 보장이 안 되면 List로 변환 후 정렬 필요
         if (this.quiz.getQuestions() instanceof List) {
             return ((List<Question>) this.quiz.getQuestions()).get(index);
         } else {
             // Set인 경우 순서 보장이 안되므로 sequence 필드 등으로 찾아야 함 (현재 Question에 sequence 필드 가정)
             log.warn("Quiz questions are not a List, attempting to find by sequence: roomId={}, index={}", this.id, index);
             return this.quiz.getQuestions().stream()
                     .filter(q -> q.getSequence() == index) // Assuming Question has getSequence()
                     .findFirst()
                     .orElse(null);
         }
    }

    /**
     * 이 배틀에서 사용될 모든 질문의 리스트를 반환합니다.
     * @return Question 리스트, 퀴즈 또는 질문이 없으면 빈 리스트
     */
    public List<Question> getQuestions() {
        if (this.quiz == null || this.quiz.getQuestions() == null) {
            return Collections.emptyList();
        }
        // 순서 보장을 위해 List로 변환 (필요시 sequence 기준 정렬)
        if (this.quiz.getQuestions() instanceof List) {
             // 이미 List면 그대로 반환 (방어적 복사 고려)
             return new ArrayList<>(this.quiz.getQuestions());
         } else {
             // Set 등 다른 컬렉션이면 List로 변환하고 sequence 기준으로 정렬
             return this.quiz.getQuestions().stream()
                     .sorted(Comparator.comparingInt(Question::getSequence)) // Assuming Question has getSequence()
                     .collect(Collectors.toList());
         }
    }

    /**
     * 이 배틀의 총 예상 시간을 초 단위로 계산합니다.
     * (문제 수 * 문제당 제한 시간)
     * @return 총 예상 시간(초)
     */
    public int getTotalTimeSeconds() {
        if (this.quiz == null || this.quiz.getQuestions() == null) {
            return 0;
        }
        return this.quiz.getQuestions().size() * this.timeLimitPerQuestion;
    }

    /**
     * 고유한 방 코드를 생성합니다 (예시).
     * 실제 사용 시 더 견고한 방법 고려 (예: UUID, SecureRandom)
     * @return 생성된 방 코드 문자열
     */
    private String generateRoomCode() {
        // 간단한 예시: UUID의 일부 사용
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // --- Object 메서드 재정의 --- //

    @Override
    public int hashCode() {
        return Objects.hash(id); // ID 기반 해시코드
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BattleRoom other = (BattleRoom) obj;
        return id != null && id.equals(other.id); // ID가 null이 아니고 같으면 동일 엔티티
    }

} 