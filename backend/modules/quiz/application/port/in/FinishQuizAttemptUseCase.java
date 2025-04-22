package application.port.in;

import application.port.in.command.FinishQuizAttemptCommand;
import domain.model.QuizAttempt;

/**
 * 퀴즈 시도 완료 처리를 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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