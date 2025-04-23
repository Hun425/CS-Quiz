package com.quizplatform.battle.adapter.in.event;

import com.quizplatform.battle.adapter.out.quiz.QuizAnswerValidationAdapter;
import com.quizplatform.common.event.quiz.QuizAnswerValidationResultEvent;
import com.quizplatform.common.event.quiz.QuizScoreCalculationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Battle 모듈의 이벤트 리스너
 * 다른 모듈에서 발행한 이벤트를 구독하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BattleEventListener {

    private final QuizAnswerValidationAdapter quizAnswerValidationAdapter;

    /**
     * 퀴즈 답변 검증 결과 이벤트 처리
     * 
     * @param event 퀴즈 답변 검증 결과 이벤트
     */
    @KafkaListener(topics = "${app.kafka.topics.quiz-answer-validation-result:quiz-answer-validation-result}", 
                   groupId = "${app.kafka.group-id:battle-service}")
    public void handleQuizAnswerValidationResult(QuizAnswerValidationResultEvent event) {
        log.info("Received validation result for question: {}, isCorrect: {}", 
                 event.getQuestionId(), event.isCorrect());
        
        // 어댑터에 결과 전달
        quizAnswerValidationAdapter.receiveValidationResult(
            event.getRequestId(), 
            event.isCorrect()
        );
    }
    
    /**
     * 퀴즈 점수 계산 결과 이벤트 처리
     * 
     * @param event 퀴즈 점수 계산 결과 이벤트
     */
    @KafkaListener(topics = "${app.kafka.topics.quiz-score-calculation-result:quiz-score-calculation-result}", 
                   groupId = "${app.kafka.group-id:battle-service}")
    public void handleQuizScoreCalculationResult(QuizScoreCalculationResultEvent event) {
        log.info("Received score calculation result for question: {}, score: {}", 
                 event.getQuestionId(), event.getScore());
        
        // 어댑터에 결과 전달
        quizAnswerValidationAdapter.receiveScoreCalculationResult(
            event.getRequestId(), 
            event.getScore()
        );
    }
}
