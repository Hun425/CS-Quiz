package com.quizplatform.core.domain.battle;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "battle_participants")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_room_id")
    private BattleRoom battleRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BattleAnswer> answers = new ArrayList<>();

    @Column(name = "current_score")
    private int currentScore = 0;

    @Column(name = "is_ready")
    private boolean isReady = false;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public BattleParticipant(BattleRoom battleRoom, User user) {
        this.battleRoom = battleRoom;
        this.user = user;
        this.lastActivity = LocalDateTime.now();
    }

    // 답변 제출 메서드를 수정하여 새로운 시간 보너스 계산 로직 적용
    public BattleAnswer submitAnswer(Question question, String answer) {
        // 현재 진행 중인 문제가 맞는지 확인
        if (!battleRoom.getCurrentQuestion().equals(question)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 제한 시간 초과 여부 확인
        LocalDateTime questionStartTime = getQuestionStartTime();
        if (question.isTimeExpired(questionStartTime)) {
            throw new BusinessException(ErrorCode.QUIZ_TIME_EXPIRED);
        }

        // 답변 생성
        BattleAnswer battleAnswer = BattleAnswer.builder()
                .participant(this)
                .question(question)
                .answer(answer)
                .isCorrect(question.isCorrectAnswer(answer))
                .answerTime(LocalDateTime.now())
                .build();

        answers.add(battleAnswer);
        updateScore(battleAnswer, questionStartTime);
        this.lastActivity = LocalDateTime.now();

        return battleAnswer;
    }

    // 점수 업데이트 로직 수정
    private void updateScore(BattleAnswer answer, LocalDateTime questionStartTime) {
        if (answer.isCorrect()) {
            // 기본 점수
            int basePoints = answer.getQuestion().getPoints();

            // 남은 시간 계산
            int secondsRemaining = calculateRemainingSeconds(
                    questionStartTime,
                    answer.getAnswerTime(),
                    answer.getQuestion().getTimeLimitSeconds()
            );

            // 시간 보너스 계산
            int timeBonus = answer.getQuestion().calculateTimeBonus(secondsRemaining);

            // 총점 업데이트
            this.currentScore += (basePoints + timeBonus);
        }
    }

    // 현재 문제의 시작 시간 계산
    private LocalDateTime getQuestionStartTime() {
        int currentIndex = battleRoom.getCurrentQuestionIndex();
        Question currentQuestion = battleRoom.getCurrentQuestion();

        // 배틀 시작 시간 + 이전 문제들의 시간 제한 합계
        LocalDateTime startTime = battleRoom.getStartTime();
        List<Question> previousQuestions = battleRoom.getQuiz().getQuestions()
                .subList(0, currentIndex);

        int previousTimeTotal = previousQuestions.stream()
                .mapToInt(Question::getTimeLimitSeconds)
                .sum();

        return startTime.plusSeconds(previousTimeTotal);
    }

    // 남은 시간 계산 (초 단위)
    private int calculateRemainingSeconds(
            LocalDateTime questionStartTime,
            LocalDateTime answerTime,
            int timeLimitSeconds
    ) {
        LocalDateTime deadline = questionStartTime.plusSeconds(timeLimitSeconds);
        Duration remainingTime = Duration.between(answerTime, deadline);
        return (int) Math.max(0, remainingTime.getSeconds());
    }

    // 현재 문제 답변 여부 확인
    public boolean hasAnsweredCurrentQuestion(int questionIndex) {
        return answers.size() > questionIndex;
    }

    // 준비 상태 토글
    public void toggleReady() {
        this.isReady = !this.isReady;
        this.lastActivity = LocalDateTime.now();
    }

    // 활성 상태 확인 (5분 이내 활동이 있었는지)
    public boolean isActive() {
        return Duration.between(lastActivity, LocalDateTime.now()).toMinutes() < 5;
    }

    // 총 답변 시간 계산
    public Duration getTotalAnswerTime() {
        if (answers.isEmpty()) {
            return Duration.ZERO;
        }

        // 첫 답변부터 마지막 답변까지의 시간
        LocalDateTime firstAnswerTime = answers.get(0).getAnswerTime();
        LocalDateTime lastAnswerTime = answers.get(answers.size() - 1).getAnswerTime();

        return Duration.between(firstAnswerTime, lastAnswerTime);
    }

    // 평균 답변 시간 계산
    public Duration getAverageAnswerTime() {
        if (answers.isEmpty()) {
            return Duration.ZERO;
        }

        long totalSeconds = answers.stream()
                .mapToLong(answer ->
                        Duration.between(
                                getQuestionStartTimeForAnswer(answer),
                                answer.getAnswerTime()
                        ).getSeconds()
                )
                .sum();

        return Duration.ofSeconds(totalSeconds / answers.size());
    }

    // 특정 답변에 대한 문제 시작 시간 계산
    private LocalDateTime getQuestionStartTimeForAnswer(BattleAnswer answer) {
        int answerIndex = answers.indexOf(answer);
        return getQuestionStartTime();
    }

    // 모든 답변이 정답인지 확인
    public boolean hasAllCorrectAnswers() {
        // 아직 모든 문제를 풀지 않았다면 false 반환
        if (answers.size() < battleRoom.getQuiz().getQuestions().size()) {
            return false;
        }

        return answers.stream().allMatch(BattleAnswer::isCorrect);
    }

    // 보너스 점수 추가
    public void addBonusPoints(int points) {
        if (points < 0) {
            throw new BusinessException(ErrorCode.INVALID_BONUS_POINTS);
        }
        this.currentScore += points;
    }

    // 정답률 계산
    public double getCorrectAnswerRate() {
        if (answers.isEmpty()) {
            return 0.0;
        }
        return answers.stream()
                .filter(BattleAnswer::isCorrect)
                .count() * 100.0 / answers.size();
    }

    // 연속 정답 횟수 계산
    public int getCurrentStreak() {
        int streak = 0;
        for (int i = answers.size() - 1; i >= 0; i--) {
            if (answers.get(i).isCorrect()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // 특정 문제 인덱스의 답변 시간 가져오기
    public Duration getAnswerTimeForQuestion(int questionIndex) {
        if (questionIndex >= answers.size()) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }

        BattleAnswer answer = answers.get(questionIndex);
        LocalDateTime questionStart = battleRoom.getQuestionStartTimeForIndex(questionIndex);

        return Duration.between(questionStart, answer.getAnswerTime());
    }
}