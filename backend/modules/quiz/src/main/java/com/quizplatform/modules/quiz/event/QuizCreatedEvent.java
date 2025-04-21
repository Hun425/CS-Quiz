package com.quizplatform.modules.quiz.event;

import com.quizplatform.core.event.BaseDomainEvent;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * 새로운 퀴즈가 생성되었을 때 발생하는 도메인 이벤트입니다.
 * 생성된 퀴즈의 정보를 포함하며, 다른 모듈에서 이 이벤트를 구독하여 처리할 수 있습니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Getter
public class QuizCreatedEvent extends BaseDomainEvent {
    
    /** 생성된 퀴즈 ID */
    private final UUID quizId;
    
    /** 퀴즈 생성자 ID */
    private final UUID creatorId;
    
    /** 퀴즈 카테고리 */
    private final String category;
    
    /** 퀴즈 난이도 */
    private final String difficulty;
    
    /** 퀴즈 태그 목록 */
    private final List<String> tags;
    
    /**
     * QuizCreatedEvent 생성자
     *
     * @param source 이벤트 소스 객체
     * @param quizId 생성된 퀴즈의 ID
     * @param creatorId 퀴즈 생성자의 ID
     * @param category 퀴즈 카테고리
     * @param difficulty 퀴즈 난이도
     * @param tags 퀴즈와 관련된 태그 목록
     */
    public QuizCreatedEvent(Object source, UUID quizId, UUID creatorId, String category, String difficulty, List<String> tags) {
        super(source);
        this.quizId = quizId;
        this.creatorId = creatorId;
        this.category = category;
        this.difficulty = difficulty;
        this.tags = tags;
    }
}
