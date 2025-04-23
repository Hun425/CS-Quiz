package com.quizplatform.quiz.application.service;

import com.quizplatform.common.event.Topics;
import com.quizplatform.common.event.quiz.QuizAnswerValidationResultEvent;
import com.quizplatform.quiz.application.port.in.ValidateQuizAnswerUseCase;
import com.quizplatform.quiz.application.port.out.DomainEventPublisherPort;
import com.quizplatform.quiz.application.port.out.LoadQuestionPort;
import com.quizplatform.quiz.domain.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 퀴즈 답변 검증 서비스
 * 사용자가 제출한 답변이 정답인지 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateQuizAnswerService implements ValidateQuizAnswerUseCase {

    private final LoadQuestionPort loadQuestionPort;
    private final DomainEventPublisherPort eventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public void validateAnswer(String requestId, String userId, String questionId, String submittedAnswer) {
        log.info("Validating answer for question: {}, user: {}", questionId, userId);
        
        try {
            // 질문 정보 로드
            Question question = loadQuestionPort.loadQuestionById(questionId);
            
            if (question == null) {
                log.warn("Question not found: {}", questionId);
                publishValidationResult(requestId, userId, questionId, submittedAnswer, "", false);
                return;
            }
            
            // 답변 검증 (대소문자 무시, 공백 제거)
            String correctAnswer = question.getCorrectAnswer();
            String normalizedCorrectAnswer = correctAnswer.trim().toLowerCase();
            String normalizedSubmittedAnswer = submittedAnswer.trim().toLowerCase();
            
            boolean isCorrect = normalizedCorrectAnswer.equals(normalizedSubmittedAnswer);
            
            log.info("Answer validation result for question {}: {}", questionId, isCorrect);
            
            // 검증 결과 이벤트 발행
            publishValidationResult(requestId, userId, questionId, submittedAnswer, correctAnswer, isCorrect);
            
        } catch (Exception e) {
            log.error("Error validating answer: {}", e.getMessage(), e);
            publishValidationResult(requestId, userId, questionId, submittedAnswer, "", false);
        }
    }
    
    /**
     * 검증 결과 이벤트 발행
     */
    private void publishValidationResult(String requestId, String userId, String questionId, 
                                        String submittedAnswer, String correctAnswer, boolean isCorrect) {
        QuizAnswerValidationResultEvent event = new QuizAnswerValidationResultEvent(
            requestId, userId, questionId, submittedAnswer, correctAnswer, isCorrect);
        
        eventPublisher.publish(event);
        log.info("Published answer validation result for request: {}, result: {}", requestId, isCorrect);
    }
}
