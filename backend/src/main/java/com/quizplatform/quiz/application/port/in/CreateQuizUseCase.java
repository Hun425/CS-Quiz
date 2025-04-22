package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.CreateQuizCommand;
import com.quizplatform.quiz.domain.model.Quiz;

/**
 * 퀴즈 생성 유스케이스
 */
public interface CreateQuizUseCase {
    /**
     * 새로운 퀴즈를 생성합니다.
     *
     * @param command 퀴즈 생성 명령
     * @return 생성된 퀴즈
     */
    Quiz createQuiz(CreateQuizCommand command);
    
    /**
     * 데일리 퀴즈를 생성합니다.
     *
     * @return 생성된 데일리 퀴즈
     */
    Quiz createDailyQuiz();
}