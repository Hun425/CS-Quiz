package com.quizplatform.quiz.domain.event;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 퀴즈 생성 이벤트
 * 
 * <p>새로운 퀴즈가 생성되었을 때 발생하는 이벤트입니다.
 * 다른 모듈에서 퀴즈 생성 정보를 필요로 할 때 사용됩니다.</p>
 */
@Getter
public class QuizCreatedEvent implements QuizEvent {
    private final String eventId;
    private final long timestamp;
    private final Long quizId;
    private final Long creatorId;
    private final String title;
    private final QuizType quizType;
    private final DifficultyLevel difficultyLevel;
    private final int questionCount;
    private final List<String> tagNames;
    
    /**
     * 퀴즈 생성 이벤트 생성자
     * 
     * @param quiz 생성된 퀴즈 엔티티
     */
    public QuizCreatedEvent(Quiz quiz) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.quizId = quiz.getId();
        this.creatorId = quiz.getCreatorId();
        this.title = quiz.getTitle();
        this.quizType = quiz.getQuizType();
        this.difficultyLevel = quiz.getDifficultyLevel();
        this.questionCount = quiz.getQuestionCount();
        this.tagNames = quiz.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toList());
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "QUIZ_CREATED";
    }
} 