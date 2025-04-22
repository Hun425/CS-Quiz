package com.quizplatform.quiz.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 시도 도메인 모델
 */
@Getter
public class QuizAttempt {
    private final Long id;
    private final Long userId;
    private final Long quizId;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer score;
    private boolean isCompleted;
    private Integer timeTaken;
    private final QuizType quizType;
    private final List<QuestionAttempt> questionAttempts;
    private final LocalDateTime createdAt;

    @Builder
    public QuizAttempt(
            Long id,
            Long userId,
            Long quizId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer score,
            boolean isCompleted,
            Integer timeTaken,
            QuizType quizType,
            List<QuestionAttempt> questionAttempts,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.startTime = startTime != null ? startTime : LocalDateTime.now();
        this.endTime = endTime;
        this.score = score;
        this.isCompleted = isCompleted;
        this.timeTaken = timeTaken;
        this.quizType = quizType != null ? quizType : QuizType.REGULAR;
        this.questionAttempts = questionAttempts != null ? questionAttempts : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * 퀴즈 시도 완료 처리
     */
    public void complete(int totalPoints, int earnedPoints) {
        this.endTime = LocalDateTime.now();
        if (this.timeTaken == null) {
            this.timeTaken = calculateTimeTaken();
        }
        this.score = calculateScore(totalPoints, earnedPoints);
        this.isCompleted = true;
    }

    /**
     * 문제 답변 시도 추가
     */
    public void addQuestionAttempt(QuestionAttempt questionAttempt) {
        this.questionAttempts.add(questionAttempt);
    }

    /**
     * 총점 계산 메서드
     */
    private int calculateScore(int totalPoints, int earnedPoints) {
        return totalPoints == 0 ? 0 : (int) ((double) earnedPoints / totalPoints * 100);
    }

    /**
     * 총 소요 시간 계산 (초 단위)
     */
    private int calculateTimeTaken() {
        return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * 제한 시간 초과 여부 확인 (퀴즈 timeLimit 분 기준)
     */
    public boolean isTimeExpired(Integer timeLimit) {
        if (timeLimit == null) return false;
        LocalDateTime deadline = startTime.plusMinutes(timeLimit);
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 소요 시간 직접 설정
     */
    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
}