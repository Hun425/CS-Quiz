package com.quizplatform.quiz.adapter.in.event;

import com.quizplatform.common.event.Topics;
import com.quizplatform.common.event.quiz.QuizAnswerValidationRequestEvent;
import com.quizplatform.common.event.quiz.QuizScoreCalculationRequestEvent;
import com.quizplatform.quiz.application.port.in.ValidateQuizAnswerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Quiz 모듈의 이벤트 리스너
 * 다른 모듈에서 발행한 이벤트를 구독하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizEventListener {

    private final ValidateQuizAnswerUseCase validateQuizAnswerUseCase;

    /**
     * 퀴즈 답변 검증 요청 이벤트 처리
     * 
     * @param event 퀴즈 답변 검증 요청 이벤트
     */
    @KafkaListener(topics = "${app.kafka.topics.quiz-answer-validation-request:" 
                   + Topics.QUIZ_ANSWER_VALIDATION_REQUEST + "}", 
                   groupId = "${app.kafka.group-id:quiz-service}")
    public void handleQuizAnswerValidationRequest(QuizAnswerValidationRequestEvent event) {
        log.info("Received answer validation request: {} for question: {}", 
                 event.getRequestId(), event.getQuestionId());
        
        // 유스케이스 호출하여 답변 검증 처리
        validateQuizAnswerUseCase.validateAnswer(
            event.getRequestId(),
            event.getUserId(), 
            event.getQuestionId(), 
            event.getSubmittedAnswer()
        );
    }
    
    /**
     * 퀴즈 점수 계산 요청 이벤트 처리
     * 
     * @param event 퀴즈 점수 계산 요청 이벤트
     */
    @KafkaListener(topics = "${app.kafka.topics.quiz-score-calculation-request:" 
                   + Topics.QUIZ_SCORE_CALCULATION_REQUEST + "}", 
                   groupId = "${app.kafka.group-id:quiz-service}")
    public void handleQuizScoreCalculationRequest(QuizScoreCalculationRequestEvent event) {
        log.info("Received score calculation request: {} for question: {}", 
                 event.getRequestId(), event.getQuestionId());
        
        // TODO: 점수 계산 유스케이스 호출 구현
        // 현재는 구현되지 않음
    }
}
