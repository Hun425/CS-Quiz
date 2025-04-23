package com.quizplatform.core.domain.battle;

import com.quizplatform.modules.quiz.domain.entity.Question;
import com.quizplatform.modules.user.domain.entity.User;
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
                @UniqueConstraint(columnNames = {"battle_room_id", "user_id"})
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_room_id")
    private BattleRoom battleRoom;

    /**
     * 참가자 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 참가자가 제출한 답변 목록
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 30)
    private List<BattleAnswer> answers = new ArrayList<>();

    /**
     * 현재 점수
     */
    @Column(name = "current_score")
    private int currentScore = 0;

    /**
     * 준비 상태
     */
    @Column(name = "is_ready")
    private boolean ready = false;

    /**
     * 활동 상태
     */
    @Column(name = "is_active")
    private boolean active = true;

    /**
     * 현재 연속 정답 횟수
     */
    @Column(name = "current_streak")
    private int currentStreak = 0;

    /**
     * 마지막 활동 시간
     */
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    /**
     * 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    // 최대 비활성 시간 (분)
    private static final int MAX_INACTIVE_MINUTES = 5;
    // 보너스 점수 상수들
    private static final int STREAK_BONUS_THRESHOLD_1 = 3;
    private static final int STREAK_BONUS_THRESHOLD_2 = 5;
    private static final int STREAK_BONUS_POINTS_1 = 3;
    private static final int STREAK_BONUS_POINTS_2 = 5;

    /**
     * 배틀 참가자 생성자
     * 
     * @param battleRoom 참가할 배틀 방
     * @param user 참가자 사용자
     */
    @Builder
    public BattleParticipant(BattleRoom battleRoom, User user) {
        this.battleRoom = battleRoom;
        this.user = user;
        this.lastActivity = LocalDateTime.now();
        validateParticipant(battleRoom, user);
    }

    /**
     * 현재 참가자가 특정 질문에 답변했는지 확인
     * 
     * <p>질문 ID를 기반으로 이미 답변했는지 여부를 체크합니다.</p>
     *
     * @param questionId 확인할 질문의 ID
     * @return 해당 질문에 답변했으면 true, 아니면 false
     */
    @Transactional
    public boolean hasAnsweredQuestion(Long questionId) {
        try {
            log.info("질문 ID로 답변 여부 확인: userId={}, questionId={}",
                    user.getId(), questionId);

            if (questionId == null) {
                return false;
            }

            // 안전하게 answers 컬렉션에 접근
            int answersCount = getAnswersCount();
            log.info("답변 수: userId={}, 답변수={}", user.getId(), answersCount);

            if (answersCount == 0) {
                return false;
            }

            // stream을 사용하여 해당 ID의 질문에 대한 답변이 있는지 확인
            boolean hasAnswered = answers.stream()
                    .anyMatch(answer -> answer.getQuestion().getId().equals(questionId));

            log.info("질문 ID 기반 답변 여부 결과: questionId={}, 결과={}",
                    questionId, hasAnswered);

            return hasAnswered;
        } catch (Exception e) {
            log.error("답변 확인 중 오류: {}", e.getMessage());
            // 기본값은 안전하게 false 반환 (아직 답변 안함)
            return false;
        }
    }

    /**
     * 현재 참가자가 특정 인덱스의 문제에 답변했는지 확인
     * 
     * <p>LazyInitializationException을 방지하기 위한 안전한 검사를 수행합니다.</p>
     *
     * @param questionIndex 확인할 문제의 인덱스
     * @return 해당 인덱스의 문제에 답변했으면 true, 아니면 false
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
     * 현재 진행 중인 문제에 답변했는지 확인
     * 
     * @return 현재 문제에 답변했으면 true, 아니면 false
     */
    public boolean hasAnsweredCurrentQuestion() {
        return answers.size() > battleRoom.getCurrentQuestionIndex();
    }

    /**
     * 참가자 유효성 검증
     * 
     * @param battleRoom 배틀 방
     * @param user 참가자
     * @throws BusinessException 유효하지 않은 참가자일 경우
     */
    private void validateParticipant(BattleRoom battleRoom, User user) {
        if (battleRoom == null) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND);
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 문제 답변 제출
     * 
     * @param question 답변할 문제
     * @param answer 제출할 답변
     * @param timeSpentSeconds 소요 시간(초)
     * @return 생성된 답변 객체
     */
    public BattleAnswer submitAnswer(Question question, String answer, int timeSpentSeconds) {
        validateAnswerSubmission(question, timeSpentSeconds);

        BattleAnswer battleAnswer = createBattleAnswer(question, answer, timeSpentSeconds);
        processAnswerResult(battleAnswer);
        updateActivityStatus();

        return battleAnswer;
    }

    /**
     * 답변 제출 유효성 검증
     * 
     * @param question 답변할 문제
     * @param timeSpentSeconds 소요 시간(초)
     * @throws BusinessException 유효하지 않은 제출일 경우
     */
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

    /**
     * 배틀 답변 객체 생성
     * 
     * @param question 답변할 문제
     * @param answer 제출할 답변
     * @param timeSpentSeconds 소요 시간(초)
     * @return 생성된 답변 객체
     */
    private BattleAnswer createBattleAnswer(Question question, String answer, int timeSpentSeconds) {
        return BattleAnswer.builder()
                .participant(this)
                .question(question)
                .answer(answer)
                .timeTaken(timeSpentSeconds)
                .build();
    }

    /**
     * 답변 결과 처리
     * 
     * @param battleAnswer 처리할 답변 객체
     */
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

    /**
     * 정답 처리
     * 
     * @param battleAnswer 정답 객체
     * @param question 답변한 문제
     */
    private void processCorrectAnswer(BattleAnswer battleAnswer, Question question) {
        int earnedPoints = calculateTotalPoints(question, battleAnswer.getTimeTaken());
        battleAnswer.setEarnedPoints(earnedPoints);
        currentScore += earnedPoints;
        currentStreak++;
    }

    /**
     * 오답 처리
     * 
     * @param battleAnswer 오답 객체
     */
    private void processIncorrectAnswer(BattleAnswer battleAnswer) {
        battleAnswer.setEarnedPoints(0);
        currentStreak = 0;
    }

    /**
     * 총 획득 점수 계산
     * 
     * @param question 답변한 문제
     * @param timeSpentSeconds 소요 시간(초)
     * @return 계산된 총 점수
     */
    private int calculateTotalPoints(Question question, int timeSpentSeconds) {
        int basePoints = question.getPoints();
        int timeBonus = calculateTimeBonus(timeSpentSeconds, question.getTimeLimitSeconds());
        int streakBonus = calculateStreakBonus();

        return basePoints + timeBonus + streakBonus;
    }

    /**
     * 시간 보너스 계산
     * 
     * @param timeSpentSeconds 소요 시간(초)
     * @param timeLimitSeconds 제한 시간(초)
     * @return 시간 보너스 점수
     */
    private int calculateTimeBonus(int timeSpentSeconds, int timeLimitSeconds) {
        double timeRatio = 1 - (timeSpentSeconds / (double) timeLimitSeconds);
        if (timeRatio >= 0.7) return 3;
        if (timeRatio >= 0.5) return 2;
        if (timeRatio >= 0.3) return 1;
        return 0;
    }

    /**
     * 연속 정답 보너스 계산
     * 
     * @return 연속 정답 보너스 점수
     */
    private int calculateStreakBonus() {
        if (currentStreak >= STREAK_BONUS_THRESHOLD_2) return STREAK_BONUS_POINTS_2;
        if (currentStreak >= STREAK_BONUS_THRESHOLD_1) return STREAK_BONUS_POINTS_1;
        return 0;
    }

    /**
     * 정답 개수 조회
     * 
     * @return 정답 개수
     */
    public int getCorrectAnswersCount() {
        return (int) answers.stream()
                .filter(BattleAnswer::isCorrect)
                .count();
    }

    /**
     * 준비 상태 토글
     * 
     * @throws BusinessException 준비 상태 변경이 불가능한 경우
     */
    public void toggleReady() {
        validateReadyToggle();
        this.ready = !this.ready;
        updateActivityStatus();
    }

    /**
     * 준비 상태 토글 유효성 검증
     * 
     * @throws BusinessException 준비 상태 변경이 불가능한 경우
     */
    private void validateReadyToggle() {
        if (!battleRoom.getStatus().name().equals(BattleRoomStatus.WAITING.name())) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED);
        }
        if (!isActive()) {
            throw new BusinessException(ErrorCode.PARTICIPANT_INACTIVE);
        }
    }

    /**
     * 활동 상태 확인
     * 
     * @return 활동 중이면 true, 아니면 false
     */
    public boolean isActive() {
        return active && !hasExceededInactiveTime();
    }

    /**
     * 최대 비활성 시간을 초과했는지 확인
     * 
     * @return 초과했으면 true, 아니면 false
     */
    private boolean hasExceededInactiveTime() {
        return Duration.between(lastActivity, LocalDateTime.now())
                .toMinutes() >= MAX_INACTIVE_MINUTES;
    }

    /**
     * 활동 상태 업데이트
     */
    private void updateActivityStatus() {
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * 정답률 계산
     * 
     * @return 정답률 (0-100%)
     */
    public double getAccuracy() {
        if (answers.isEmpty()) return 0.0;
        return (getCorrectAnswersCount() / (double) answers.size()) * 100;
    }

    /**
     * 평균 답변 시간 계산
     * 
     * @return 평균 답변 시간
     */
    public Duration getAverageAnswerTime() {
        if (answers.isEmpty()) return Duration.ZERO;

        long totalSeconds = answers.stream()
                .mapToInt(BattleAnswer::getTimeTaken)
                .sum();
        return Duration.ofSeconds(totalSeconds / answers.size());
    }

    /**
     * 보너스 점수 추가
     * 
     * @param bonusPoints 추가할 보너스 점수
     */
    public void addBonusPoints(int bonusPoints) {
        // 보너스 점수를 현재 점수에 더함
        this.currentScore += bonusPoints;
    }

    /**
     * 활동 상태 설정
     * 
     * @param active 활동 상태
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * 모든 문제를 정답으로 맞췄는지 확인
     * 
     * @return 모두 정답이면 true, 아니면 false
     */
    public boolean hasAllCorrectAnswers() {
        // 참가자의 답변이 존재하고 모두 정답이면 true 반환
        return !answers.isEmpty() && answers.stream().allMatch(BattleAnswer::isCorrect);
    }

    /**
     * 안전하게 answers 컬렉션의 크기를 반환
     * 
     * @return answers 컬렉션의 크기
     */
    private int getAnswersCount() {
        try {
            return answers.size();
        } catch (Exception e) {
            log.warn("answers 컬렉션 접근 오류: {}", e.getMessage());
            return 0;
        }
    }

    public void setCurrentStreak(int i) {
        this.currentStreak = i;
    }
}