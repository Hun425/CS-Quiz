package com.quizplatform.modules.search.event.listener;

import com.quizplatform.modules.quiz.event.QuizCreatedEvent;
import com.quizplatform.modules.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Quiz 모듈에서 발생하는 QuizCreatedEvent를 처리하는 이벤트 리스너입니다.
 * 새로운 퀴즈가 생성될 때 검색 인덱스에 추가하는 작업을 수행합니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizCreatedEventListener {

    private final SearchIndexService searchIndexService;

    /**
     * QuizCreatedEvent 이벤트를 수신하여 처리합니다.
     * 생성된 퀴즈 정보를 검색 인덱스에 추가합니다.
     *
     * @param event 수신된 QuizCreatedEvent
     */
    @EventListener
    public void handleQuizCreatedEvent(QuizCreatedEvent event) {
        log.info("Quiz created event received: Quiz ID={}, Creator={}, Category={}", 
                 event.getQuizId(), event.getCreatorId(), event.getCategory());
        
        try {
            // 퀴즈 정보를 검색 인덱스에 추가
            searchIndexService.indexQuiz(
                event.getQuizId(),
                event.getCreatorId(),
                event.getCategory(),
                event.getDifficulty(),
                event.getTags()
            );
            
            log.info("Successfully indexed new quiz: {}", event.getQuizId());
        } catch (Exception e) {
            log.error("Error indexing new quiz", e);
        }
    }
}
