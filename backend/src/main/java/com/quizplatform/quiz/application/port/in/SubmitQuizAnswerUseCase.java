package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.SubmitAnswerCommand;
import com.quizplatform.quiz.domain.model.QuestionAttempt;

/**
 * 퀴즈 답변 제출 유스케이스
 */
public interface SubmitQuizAnswerUseCase {
    /**
     * 퀴즈 문제 답변을 제출합니다.
     *
     * @param command 답변 제출 명령
     * @return 문제 시도 결과
     */
    QuestionAttempt submitAnswer(SubmitAnswerCommand command);
}