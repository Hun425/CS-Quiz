package com.quizplatform.modules.battle.domain.entity;

import com.quizplatform.modules.quiz.domain.entity.Question; // Keep dependency
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 배틀 답변 엔티티 클래스
 *
 * <p>배틀 참가자가 문제에 제출한 답변과 관련 정보를 관리합니다.
 * 답변 내용, 정답 여부, 획득 점수 등을 포함합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "battle_answers",
       indexes = {
           // 특정 참가자의 답변 조회 최적화
           @Index(name = "idx_battle_answer_participant", columnList = "participant_id"),
           // 특정 문제에 대한 답변 조회 최적화
           @Index(name = "idx_battle_answer_question", columnList = "question_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleAnswer {
    /**
     * 답변 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 답변을 제출한 참가자
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private BattleParticipant participant;

    /**
     * 답변한 문제
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 제출한 답변 내용
     */
    @Column(name = "submitted_answer", columnDefinition = "TEXT")
    private String submittedAnswer;

    /**
     * 정답 여부 (Service에서 설정)
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    /**
     * 이 답변으로 획득한 총 점수 (Service에서 설정)
     */
    @Column(name = "score_gained", nullable = false)
    private int scoreGained = 0;

    /**
     * 답변 제출 시간 (Auditing으로 자동 관리)
     */
    @CreatedDate
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    /**
     * 배틀 답변 생성자 (Service에서 사용)
     *
     * @param participant 답변을 제출한 참가자 (Not null)
     * @param question 답변한 문제 (Not null)
     * @param submittedAnswer 제출한 답변 내용
     * @param isCorrect 정답 여부 (Service에서 판단)
     * @param scoreGained 획득 점수 (Service에서 계산)
     * @param answeredAt 답변 시간 (Service에서 설정 또는 Auditing)
     */
    @Builder
    public BattleAnswer(BattleParticipant participant, Question question, String submittedAnswer,
                        boolean isCorrect, int scoreGained, LocalDateTime answeredAt) {
        if (participant == null || question == null) {
            throw new IllegalArgumentException("Participant and Question cannot be null");
        }
        this.participant = participant;
        this.question = question;
        this.submittedAnswer = submittedAnswer;
        this.isCorrect = isCorrect;
        this.scoreGained = scoreGained;
        this.answeredAt = (answeredAt != null) ? answeredAt : LocalDateTime.now();
    }

    // --- hashCode & equals --- //

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BattleAnswer other = (BattleAnswer) obj;
        return id != null && Objects.equals(id, other.id);
    }
} 