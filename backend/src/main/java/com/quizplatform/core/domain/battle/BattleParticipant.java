package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
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
@Slf4j
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
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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

    /**
     * 현재 참가자가 특정 인덱스의 문제에 답변했는지 안전하게 확인합니다.
     * 이 메서드는 LazyInitializationException을 방지합니다.
     */
    @Transactional
    public boolean hasAnsweredCurrentQuestion(int questionIndex) {
        try {
            // 문제 번호 로그 (0-based 인덱스 -> 1-based 문제 번호)
            int questionNumber = questionIndex + 1;
            int totalQuestions = battleRoom.getQuiz().getQuestions().size();

            log.info("답변 여부 확인: userId={}, 문제번호={}/{}, 인덱스={}",
                    user.getId(), questionNumber, totalQuestions, questionIndex);

            // 마지막 문제인지 확인
            boolean isLastQuestion = questionIndex == totalQuestions - 1;

            // 중요: 안전하게 answers 컬렉션 크기 접근
            int answersCount = getAnswersCount();
            log.info("답변 수: userId={}, 답변수={}", user.getId(), answersCount);

            // 배틀이 완료되고 새로 시작한 경우 (첫 문제인데 답변이 이미 많은 경우)
            if (questionIndex == 0 && answersCount >= totalQuestions) {
                log.info("새 게임 시작 감지: userId={}, 기존 답변수={}, 전체문제수={}",
                        user.getId(), answersCount, totalQuestions);
                return false; // 새 게임이므로 답변 허용
            }

            // 마지막 문제에 대한 특별 처리
            if (isLastQuestion) {
                // 로직은 유지하되, 명시적으로 컬렉션 접근 없이 인덱스 기반으로 처리
                return questionIndex < answersCount;
            }

            // 일반적인 경우: 요구되는 답변 수 비교 (인덱스+1)과 비교
            boolean result = answersCount >= questionIndex + 1;
            log.info("답변 여부 결과: 인덱스={}, 답변수={}, 필요답변수={}, 결과={}",
                    questionIndex, answersCount, questionIndex + 1, result);

            return result;
        } catch (Exception e) {
            log.error("답변 확인 중 오류: {}", e.getMessage());
            // 기본값은 안전하게 false 반환 (아직 답변 안함)
            return false;
        }
    }

    /**
     * 안전하게 answers 컬렉션의 크기를 반환합니다.
     */
    private int getAnswersCount() {
        try {
            return answers.size();
        } catch (Exception e) {
            log.warn("answers 컬렉션 접근 오류: {}", e.getMessage());
            return 0;
        }
    }
}