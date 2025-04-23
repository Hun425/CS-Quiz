package com.quizplatform.modules.quiz.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 문제 시도 엔티티 클래스
 * 
 * <p>사용자가 특정 문제에 대해 제출한 답변과 관련 정보를 관리합니다.
 * 사용자의 답변, 정답 여부, 소요 시간 등의 정보를 저장합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "question_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 시도가 속한 퀴즈 시도
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id")
    private QuizAttempt quizAttempt;

    /**
     * 답변한 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    /**
     * 사용자 답변
     */
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    /**
     * 정답 여부
     */
    @Column(name = "is_correct")
    private boolean isCorrect;

    /**
     * 문제 답변에 소요된 시간 (초 단위)
     */
    @Column(name = "time_taken")
    private Integer timeTaken;

    /**
     * 시도 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 문제 시도 생성자
     * 
     * @param quizAttempt 이 시도가 속한 퀴즈 시도
     * @param question 답변한 문제
     * @param userAnswer 사용자 답변
     * @param isCorrect 정답 여부
     * @param timeTaken 소요 시간 (초)
     */
    @Builder
    public QuestionAttempt(QuizAttempt quizAttempt, Question question,
                           String userAnswer, boolean isCorrect, Integer timeTaken) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.timeTaken = timeTaken;
    }
}