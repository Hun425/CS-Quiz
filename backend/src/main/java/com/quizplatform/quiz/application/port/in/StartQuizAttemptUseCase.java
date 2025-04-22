package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.StartQuizAttemptCommand;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizAttempt;

/**
 * 퀴즈 시도 시작 유스케이스
 */
public interface StartQuizAttemptUseCase {
    /**
     * 퀴즈 시도를 시작합니다.
     *
     * @param command 퀴즈 시도 시작 명령
     * @return 퀴즈 시도
     */
    QuizAttempt startQuizAttempt(StartQuizAttemptCommand command);
    
    /**
     * 플레이 가능한 퀴즈를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @return 플레이 가능한 퀴즈
     */
    Quiz getPlayableQuiz(Long quizId, Long userId);
}