package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 시도 엔티티 클래스
 * 
 * <p>사용자가 특정 퀴즈를 풀기 시작할 때 생성되며, 시작 시간, 종료 시간, 점수, 소요 시간 등
 * 퀴즈 응시와 관련된 정보를 관리합니다. 문제별 시도 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "quiz_attempts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 퀴즈를 시도한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 시도한 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    /**
     * 퀴즈 시작 시간
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 퀴즈 종료 시간
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 획득 점수 (0-100)
     */
    private Integer score;

    /**
     * 퀴즈 완료 여부
     */
    @Column(name = "is_completed")
    private boolean isCompleted = false;

    /**
     * 총 소요 시간 (초 단위)
     */
    @Column(name = "time_taken")
    private Integer timeTaken;

    /**
     * 퀴즈 유형 (REGULAR, DAILY 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type")
    private QuizType quizType = QuizType.REGULAR;

    /**
     * 문제별 시도 목록
     */
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();

    /**
     * 시도 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 퀴즈 시도 생성자
     * 
     * @param user 시도하는 사용자
     * @param quiz 시도할 퀴즈
     */
    @Builder
    public QuizAttempt(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 퀴즈 시도 완료 처리
     * 
     * <p>종료 시간 기록, 소요 시간 계산, 점수 계산 및 완료 상태로 변경합니다.</p>
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.timeTaken == null) {
            this.timeTaken = calculateTimeTaken();
        }
        this.score = calculateScore();
        this.isCompleted = true;
    }

    /**
     * 문제 답변 시도 추가
     * 
     * <p>개별 문제에 대한 사용자 답변을 기록하고, 정답 여부를 판정합니다.</p>
     * 
     * @param question 답변할 문제
     * @param userAnswer 사용자 답변
     * @return 생성된 문제 시도 객체
     */
    public QuestionAttempt addQuestionAttempt(Question question, String userAnswer) {
        QuestionAttempt questionAttempt = QuestionAttempt.builder()
                .quizAttempt(this)
                .question(question)
                .userAnswer(userAnswer)
                .isCorrect(question.isCorrectAnswer(userAnswer))
                .timeTaken(calculateTimeTakenSinceLastAttempt())
                .build();

        this.questionAttempts.add(questionAttempt);
        return questionAttempt;
    }

    /**
     * 총점 계산 메서드
     * 
     * <p>맞은 문제의 배점 합계를 기준으로 100점 만점으로 환산합니다.</p>
     * 
     * @return 계산된 점수 (0-100)
     */
    private int calculateScore() {
        int totalPoints = quiz.getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();

        int earnedPoints = questionAttempts.stream()
                .filter(QuestionAttempt::isCorrect)
                .mapToInt(attempt -> attempt.getQuestion().getPoints())
                .sum();

        return totalPoints == 0 ? 0 : (int) ((double) earnedPoints / totalPoints * 100);
    }

    /**
     * 총 소요 시간 계산 (초 단위)
     * 
     * <p>시작 시간부터 종료 시간까지의 경과 시간을 초 단위로 계산합니다.</p>
     * 
     * @return 초 단위 소요 시간
     */
    private int calculateTimeTaken() {
        return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * 직전 시도 이후 경과 시간 계산 (초 단위)
     * 
     * <p>이전 문제 시도 시간(또는 퀴즈 시작 시간)부터 현재까지 경과 시간을 계산합니다.</p>
     * 
     * @return 초 단위 경과 시간
     */
    private int calculateTimeTakenSinceLastAttempt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAttemptTime;

        if (questionAttempts.isEmpty()) {
            lastAttemptTime = startTime;
        } else {
            QuestionAttempt lastAttempt = questionAttempts.get(questionAttempts.size() - 1);
            // createdAt이 null인 경우 startTime으로 대체
            lastAttemptTime = lastAttempt.getCreatedAt() != null ?
                    lastAttempt.getCreatedAt() :
                    startTime;
        }

        // 안전 장치: lastAttemptTime이 여전히 null인 경우
        if (lastAttemptTime == null) {
            return 0; // 유효한 시간이 없는 경우 기본값 0 반환
        }

        return (int) java.time.Duration.between(lastAttemptTime, now).getSeconds();
    }

    /**
     * 제한 시간 초과 여부 확인
     * 
     * <p>퀴즈의 제한 시간이 지났는지 확인합니다.</p>
     * 
     * @return 시간 초과면 true, 아니면 false
     */
    public boolean isTimeExpired() {
        if (quiz.getTimeLimit() == null) return false;
        LocalDateTime deadline = startTime.plusMinutes(quiz.getTimeLimit());
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 소요 시간 직접 설정
     * 
     * @param timeTaken 초 단위 소요 시간
     */
    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
}