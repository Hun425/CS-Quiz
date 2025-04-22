package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.FinishQuizAttemptCommand;
import com.quizplatform.quiz.domain.model.QuizAttempt;

/**
 * 퀴즈 시도 완료 유스케이스
 */
public interface FinishQuizAttemptUseCase {
    /**
     * 퀴즈 시도를 완료합니다.
     *
     * @param command 퀴즈 시도 완료 명령
     * @return 완료된 퀴즈 시도
     */
    QuizAttempt finishQuizAttempt(FinishQuizAttemptCommand command);
}