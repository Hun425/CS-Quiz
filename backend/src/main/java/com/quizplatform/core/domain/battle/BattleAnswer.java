package com.quizplatform.core.domain.battle;

import com.quizplatform.modules.quiz.domain.entity.Question;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 배틀 답변 엔티티 클래스
 * 
 * <p>배틀 참가자가 문제에 제출한 답변과 관련 정보를 관리합니다.
 * 답변 내용, 정답 여부, 획득 점수, 소요 시간 등을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "battle_answers")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private BattleParticipant participant;

    /**
     * 답변한 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    /**
     * 제출한 답변 내용
     */
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    /**
     * 정답 여부
     */
    @Column(name = "is_correct")
    private boolean correct;

    /**
     * 획득한 기본 점수
     */
    @Column(name = "earned_points")
    private int earnedPoints;

    /**
     * 시간 보너스 점수
     */
    @Column(name = "time_bonus")
    private int timeBonus;

    /**
     * 답변에 소요된 시간 (초 단위)
     */
    @Column(name = "time_taken")
    private int timeTaken;

    /**
     * 타임아웃 발생 여부
     * 시간 내에 답변을 제출하지 못한 경우 true
     */
    @Column(name = "timeout_occurred")
    private boolean timeoutOccurred;

    /**
     * 연결 끊김 발생 여부
     * 참가자가 중간에 연결이 끊긴 경우 true
     */
    @Column(name = "disconnect_occurred")
    private boolean disconnectOccurred;

    /**
     * 답변 제출 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 배틀 답변 생성자
     * 
     * @param participant 답변을 제출한 참가자
     * @param question 답변한 문제
     * @param answer 제출한 답변 내용
     * @param timeTaken 소요 시간 (초 단위)
     */
    @Builder
    public BattleAnswer(BattleParticipant participant, Question question, String answer, int timeTaken) {
        this.participant = participant;
        this.question = question;
        this.answer = answer;
        this.timeTaken = timeTaken;
        this.timeBonus = 0;
        this.earnedPoints = 0;
        this.timeoutOccurred = false;
        this.disconnectOccurred = false;
    }

    /**
     * 정답 여부 설정
     * 
     * @param correct 정답 여부
     */
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /**
     * 획득 점수 설정
     * 
     * @param points 획득한 점수
     */
    public void setEarnedPoints(int points) {
        this.earnedPoints = points;
    }

    /**
     * 시간 보너스 설정
     * 
     * @param bonus 시간 보너스 점수
     */
    public void setTimeBonus(int bonus) {
        this.timeBonus = bonus;
    }

    /**
     * 타임아웃 발생 여부 설정
     * 
     * @param timeoutOccurred 타임아웃 발생 여부
     */
    public void setTimeoutOccurred(boolean timeoutOccurred) {
        this.timeoutOccurred = timeoutOccurred;
    }

    /**
     * 연결 끊김 발생 여부 설정
     * 
     * @param disconnectOccurred 연결 끊김 발생 여부
     */
    public void setDisconnectOccurred(boolean disconnectOccurred) {
        this.disconnectOccurred = disconnectOccurred;
    }

    /**
     * 총 획득 점수 계산
     * 
     * @return 기본 점수와 시간 보너스의 합
     */
    public int getTotalPoints() {
        return earnedPoints + timeBonus;
    }
}