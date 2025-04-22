package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.UpdateQuizCommand;
import com.quizplatform.quiz.domain.model.Quiz;

/**
 * 퀴즈 수정 유스케이스
 */
public interface UpdateQuizUseCase {
    /**
     * 퀴즈를 수정합니다.
     *
     * @param quizId 수정할 퀴즈 ID
     * @param command 퀴즈 수정 명령
     * @return 수정된 퀴즈
     */
    Quiz updateQuiz(Long quizId, UpdateQuizCommand command);
    
    /**
     * 퀴즈의 공개 여부를 설정합니다.
     *
     * @param quizId 퀴즈 ID
     * @param isPublic 공개 여부
     * @return 수정된 퀴즈
     */
    Quiz setQuizPublic(Long quizId, boolean isPublic);
}