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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "battle_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"battle_room_id", "user_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_room_id")
    private BattleRoom battleRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 30)
    private List<BattleAnswer> answers = new ArrayList<>();

    @Column(name = "current_score")
    private int currentScore = 0;

    @Column(name = "is_ready")
    private boolean ready = false;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @CreatedDate
    private LocalDateTime createdAt;

    // 최대 비활성 시간 (분)
    private static final int MAX_INACTIVE_MINUTES = 5;
    // 보너스 점수 상수들
    private static final int STREAK_BONUS_THRESHOLD_1 = 3;
    private static final int STREAK_BONUS_THRESHOLD_2 = 5;
    private static final int STREAK_BONUS_POINTS_1 = 3;
    private static final int STREAK_BONUS_POINTS_2 = 5;

    @Builder
    public BattleParticipant(BattleRoom battleRoom, User user) {
        this.battleRoom = battleRoom;
        this.user = user;
        this.lastActivity = LocalDateTime.now();
        validateParticipant(battleRoom, user);
    }

    private void validateParticipant(BattleRoom battleRoom, User user) {
        if (battleRoom == null) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND);
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public BattleAnswer submitAnswer(Question question, String answer, int timeSpentSeconds) {
        validateAnswerSubmission(question, timeSpentSeconds);

        BattleAnswer battleAnswer = createBattleAnswer(question, answer, timeSpentSeconds);
        processAnswerResult(battleAnswer);
        updateActivityStatus();

        return battleAnswer;
    }

    private void validateAnswerSubmission(Question question, int timeSpentSeconds) {
        if (hasAnsweredCurrentQuestion()) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_SUBMITTED);
        }
        if (!isActive()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (timeSpentSeconds > question.getTimeLimitSeconds()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BattleAnswer createBattleAnswer(Question question, String answer, int timeSpentSeconds) {
        return BattleAnswer.builder()
                .participant(this)
                .question(question)
                .answer(answer)
                .timeTaken(timeSpentSeconds)
                .build();
    }

    private void processAnswerResult(BattleAnswer battleAnswer) {
        Question question = battleAnswer.getQuestion();
        boolean isCorrect = question.isCorrectAnswer(battleAnswer.getAnswer());
        battleAnswer.setCorrect(isCorrect);

        if (isCorrect) {
            processCorrectAnswer(battleAnswer, question);
        } else {
            processIncorrectAnswer(battleAnswer);
        }

        answers.add(battleAnswer);
    }

    private void processCorrectAnswer(BattleAnswer battleAnswer, Question question) {
        int earnedPoints = calculateTotalPoints(question, battleAnswer.getTimeTaken());
        battleAnswer.setEarnedPoints(earnedPoints);
        currentScore += earnedPoints;
        currentStreak++;
    }

    private void processIncorrectAnswer(BattleAnswer battleAnswer) {
        battleAnswer.setEarnedPoints(0);
        currentStreak = 0;
    }

    private int calculateTotalPoints(Question question, int timeSpentSeconds) {
        int basePoints = question.getPoints();
        int timeBonus = calculateTimeBonus(timeSpentSeconds, question.getTimeLimitSeconds());
        int streakBonus = calculateStreakBonus();

        return basePoints + timeBonus + streakBonus;
    }

    private int calculateTimeBonus(int timeSpentSeconds, int timeLimitSeconds) {
        double timeRatio = 1 - (timeSpentSeconds / (double) timeLimitSeconds);
        if (timeRatio >= 0.7) return 3;
        if (timeRatio >= 0.5) return 2;
        if (timeRatio >= 0.3) return 1;
        return 0;
    }

    private int calculateStreakBonus() {
        if (currentStreak >= STREAK_BONUS_THRESHOLD_2) return STREAK_BONUS_POINTS_2;
        if (currentStreak >= STREAK_BONUS_THRESHOLD_1) return STREAK_BONUS_POINTS_1;
        return 0;
    }

    public boolean hasAnsweredCurrentQuestion() {
        return answers.size() > battleRoom.getCurrentQuestionIndex();
    }

    public boolean hasAnsweredCurrentQuestion(int questionIndex) {
        // 전달받은 questionIndex보다 많은 답변이 제출되었는지 확인합니다.
        return answers.size() > questionIndex;
    }

    public int getCorrectAnswersCount() {
        return (int) answers.stream()
                .filter(BattleAnswer::isCorrect)
                .count();
    }

    public void toggleReady() {
        validateReadyToggle();
        this.ready = !this.ready;
        updateActivityStatus();
    }

    private void validateReadyToggle() {
        if (!battleRoom.getStatus().name().equals(BattleRoomStatus.WAITING.name())) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isActive()) {
            throw new BusinessException(ErrorCode.PARTICIPANT_INACTIVE);
        }
    }

    public boolean isActive() {
        return active && !hasExceededInactiveTime();
    }

    private boolean hasExceededInactiveTime() {
        return Duration.between(lastActivity, LocalDateTime.now())
                .toMinutes() >= MAX_INACTIVE_MINUTES;
    }

    private void updateActivityStatus() {
        this.lastActivity = LocalDateTime.now();
    }

    public double getAccuracy() {
        if (answers.isEmpty()) return 0.0;
        return (getCorrectAnswersCount() / (double) answers.size()) * 100;
    }

    public Duration getAverageAnswerTime() {
        if (answers.isEmpty()) return Duration.ZERO;

        long totalSeconds = answers.stream()
                .mapToInt(BattleAnswer::getTimeTaken)
                .sum();
        return Duration.ofSeconds(totalSeconds / answers.size());
    }

    public void addBonusPoints(int bonusPoints) {
        // 보너스 점수를 현재 점수에 더함
        this.currentScore += bonusPoints;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasAllCorrectAnswers() {
        // 참가자의 답변이 존재하고 모두 정답이면 true 반환
        return !answers.isEmpty() && answers.stream().allMatch(BattleAnswer::isCorrect);
    }
}