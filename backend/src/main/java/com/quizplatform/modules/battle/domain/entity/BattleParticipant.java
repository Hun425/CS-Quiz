package com.quizplatform.modules.battle.domain.entity;

import com.quizplatform.modules.quiz.domain.entity.Question; // Keep dependency
import com.quizplatform.modules.user.domain.entity.User; // Keep dependency
import com.quizplatform.shared_kernel.exception.BusinessException; // TODO: Use shared_kernel exception
import com.quizplatform.shared_kernel.exception.ErrorCode;
import jakarta.persistence.*;
// import jakarta.transaction.Transactional; // Keep @Transactional on Service layer
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 배틀 참가자 엔티티 클래스
 *
 * <p>배틀에 참여한 사용자 정보와 점수, 답변, 상태 등을 관리합니다.
 * 사용자 식별, 답변 제출, 점수 계산, 활동 상태 추적 등의 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(
        name = "battle_participants",
        uniqueConstraints = {
                // 한 사용자는 같은 배틀방에 한 번만 참가 가능
                @UniqueConstraint(columnNames = {"battle_room_id", "user_id"})
        },
        indexes = {
                // 배틀방 ID로 참가자 조회 최적화
                @Index(name = "idx_battle_participant_room", columnList = "battle_room_id"),
                // 사용자 ID로 참가자 조회 최적화 (내 배틀 기록 등)
                @Index(name = "idx_battle_participant_user", columnList = "user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class BattleParticipant {
    /**
     * 참가자 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 참가 중인 배틀 방
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 참가자는 항상 방에 속해야 함
    @JoinColumn(name = "battle_room_id", nullable = false)
    private BattleRoom battleRoom;

    /**
     * 참가자 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 참가자는 항상 사용자와 연결되어야 함
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 참가자가 제출한 답변 목록
     * CascadeType.ALL: Participant 저장/삭제 시 Answer도 함께 처리
     * orphanRemoval=true: Participant의 answers 컬렉션에서 Answer 제거 시 DB에서도 삭제
     * FetchMode.SUBSELECT: 연관된 answers 조회 시 별도 쿼리로 조회 (N+1 방지)
     * BatchSize: SUBSELECT 쿼리 시 IN 절에 포함될 ID 개수 제한
     */
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 30)
    private List<BattleAnswer> answers = new ArrayList<>();

    /**
     * 현재 점수
     */
    @Column(name = "score", nullable = false)
    private int score = 0;

    /**
     * 준비 상태 (대기방에서 사용)
     */
    @Column(name = "is_ready", nullable = false)
    private boolean ready = false;

    /**
     * 패배 상태 (true: 패배/타임아웃/퇴장, false: 진행중)
     */
    @Column(name = "is_defeated", nullable = false)
    private boolean defeated = false;

    /**
     * 참가 시간 (Builder에서 설정)
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /**
     * 생성 시간 (Auditing으로 자동 관리)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 배틀 참가자 생성자
     *
     * @param battleRoom 참가할 배틀 방 (Not null)
     * @param user 참가자 사용자 (Not null)
     * @param joinedAt 참가 시간
     */
    @Builder
    public BattleParticipant(BattleRoom battleRoom, User user, LocalDateTime joinedAt) {
        validateParticipant(battleRoom, user);
        this.battleRoom = battleRoom;
        this.user = user;
        this.joinedAt = (joinedAt != null) ? joinedAt : LocalDateTime.now(); // Set join time
        this.defeated = false; // Default state
        this.ready = false; // Default state
        this.score = 0; // Default state
    }

    /**
     * 생성자 유효성 검증
     */
    private void validateParticipant(BattleRoom battleRoom, User user) {
        if (battleRoom == null) {
             log.error("Attempted to create BattleParticipant with null BattleRoom.");
            throw new IllegalArgumentException("BattleRoom cannot be null");
        }
        if (user == null) {
             log.error("Attempted to create BattleParticipant with null User.");
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    /**
     * 특정 인덱스의 질문에 이미 답변했는지 확인합니다.
     * @param questionIndex 확인할 질문 인덱스
     * @return 답변했으면 true, 아니면 false
     */
    public boolean hasAnsweredQuestionByIndex(int questionIndex) {
         // Check if any answer in the list corresponds to the given question index (assuming Question has getSequence())
         return this.answers.stream()
                 .anyMatch(answer -> answer.getQuestion() != null && answer.getQuestion().getSequence() == questionIndex);
     }

    /**
     * 참가자의 준비 상태를 토글합니다.
     * @throws BusinessException 준비 상태를 변경할 수 없는 경우 (예: 이미 패배)
     */
    public void toggleReady() {
        // Validation (like checking if defeated) is handled in BattleService before calling this
        this.ready = !this.ready;
        log.debug("Participant {} ready state toggled to: {}", this.id, this.ready);
    }

    /**
     * 참가자를 패배 상태로 표시합니다.
     * (타임아웃, 퇴장 등)
     */
    public void markAsDefeated() {
        if (!this.defeated) {
            this.defeated = true;
            log.info("Participant {} marked as defeated.", this.id);
        } else {
             log.warn("Participant {} is already marked as defeated.", this.id);
         }
    }

    /**
     * 참가자의 점수를 증가시킵니다.
     * @param pointsToAdd 추가할 점수
     */
    public void addScore(int pointsToAdd) {
        if (pointsToAdd > 0) {
            this.score += pointsToAdd;
            log.debug("Added {} points to participant {}. New score: {}", pointsToAdd, this.id, this.score);
        } else if (pointsToAdd < 0) {
             log.warn("Attempted to add negative points ({}) to participant {}. Ignoring.", pointsToAdd, this.id);
         }
    }

    /**
     * 참가자의 답변 목록에 새로운 답변을 추가하고 연관관계를 설정합니다.
     * (BattleService에서 BattleAnswer 생성 후 호출)
     *
     * @param answer 추가할 BattleAnswer 엔티티 (Not null)
     */
    public void addAnswer(BattleAnswer answer) {
        if (answer != null && answer.getParticipant() == this) {
            this.answers.add(answer);
            log.debug("Added answer for question {} for participant {}", answer.getQuestion().getId(), this.id);
        } else if (answer != null) {
            log.error("Attempted to add an answer that does not belong to this participant. Answer's participant: {}, This participant: {}",
                     answer.getParticipant() != null ? answer.getParticipant().getId() : "null", this.id);
             // Consider throwing an exception here
        } else {
             log.error("Attempted to add a null answer to participant {}.", this.id);
         }
    }

    /**
     * 정답 개수를 반환합니다.
     * @return 정답 개수
     */
    public int getCorrectAnswerCount() {
        return (int) this.answers.stream().filter(BattleAnswer::isCorrect).count();
    }

    /**
     * 정답률을 계산하여 반환합니다 (0.0 ~ 1.0).
     * @return 정답률, 답변이 없으면 0.0
     */
    public double getAccuracy() {
        int totalAnswers = getAnswersCount();
        if (totalAnswers == 0) {
            return 0.0;
        }
        return (double) getCorrectAnswerCount() / totalAnswers;
    }

    /**
     * 평균 답변 시간을 계산하여 반환합니다.
     * @return 평균 답변 시간 Duration, 답변이 없으면 Duration.ZERO
     */
    public Duration getAverageAnswerTime() {
        if (this.answers.isEmpty()) {
            return Duration.ZERO;
        }
        // Assuming BattleAnswer has getTimeSpentSeconds() or equivalent
        // Need to add getTimeSpentSeconds to BattleAnswer if not present
        // OptionalLong avgNanos = this.answers.stream()
        //         .mapToLong(answer -> answer.getTimeSpentSeconds() * 1_000_000_000L) // Convert seconds to nanos
        //         .average()
        //         .stream().mapToLong(avg -> (long)avg).findFirst(); // Get average as long nanos

        // Currently BattleAnswer doesn't store timeSpent. Cannot calculate average.
        // Returning ZERO as placeholder.
        log.warn("Cannot calculate average answer time for participant {} as BattleAnswer does not store time spent.", this.id);
        return Duration.ZERO;
        // return avgNanos.isPresent() ? Duration.ofNanos(avgNanos.getAsLong()) : Duration.ZERO;
    }

    /**
     * 총 답변 개수를 반환합니다.
     * @return 총 답변 개수
     */
    private int getAnswersCount() {
        return this.answers.size();
    }

    /**
     * 이 엔티티의 해시 코드를 반환합니다.
     * @return 해시 코드 (ID 기반)
     */
    @Override
    public int hashCode() {
        return Objects.hash(id); // ID 기반 해시코드
    }

    /**
     * 주어진 객체와 이 엔티티가 같은지 비교합니다.
     * @param obj 비교할 객체
     * @return 같은 클래스이고 ID가 같으면 true, 아니면 false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BattleParticipant other = (BattleParticipant) obj;
        return id != null && id.equals(other.id); // ID가 null이 아니고 같으면 동일 엔티티
    }
} 