package com.quizplatform.modules.quiz.application.event; // Or domain.event

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자가 퀴즈 시도를 완료했음을 알리는 이벤트 DTO
 */
public record QuizAttemptCompletedEvent(
        Long attemptId,
        Long userId,
        Long quizId,
        Integer score, // 획득 점수
        Integer timeTakenSeconds, // 소요 시간 (초)
        boolean isCompleted, // 완료 여부 (true)
        LocalDateTime completedAt, // 완료 시각
        List<QuestionAnswerInfo> answers // 각 문제 답변 정보 (선택 사항)
) {
    public QuizAttemptCompletedEvent(Long attemptId, Long userId, Long quizId, Integer score,
                                     Integer timeTakenSeconds, LocalDateTime completedAt, List<QuestionAnswerInfo> answers) {
        this(attemptId, userId, quizId, score, timeTakenSeconds, true, completedAt, answers);
    }

    // 각 문제 답변 정보를 담는 내부 레코드 (필요시 정의)
    public record QuestionAnswerInfo(
        Long questionId,
        String userAnswer,
        boolean isCorrect
    ) {}
} 