package com.quizplatform.quiz.domain.event;

import lombok.Getter;

import java.util.Set;

/**
 * 데일리 퀴즈 생성 이벤트
 */
@Getter
public class DailyQuizCreatedEvent extends DomainEvent {
    private final Long quizId;
    private final String title;
    private final Set<Long> tagIds;
    private final int questionCount;

    public DailyQuizCreatedEvent(Long quizId, String title, Set<Long> tagIds, int questionCount) {
        super("DAILY_QUIZ_CREATED");
        this.quizId = quizId;
        this.title = title;
        this.tagIds = tagIds;
        this.questionCount = questionCount;
    }
}