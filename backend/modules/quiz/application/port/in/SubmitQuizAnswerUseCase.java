package application.port.in;

import application.port.in.command.SubmitAnswerCommand;
import domain.model.QuestionAttempt;

/**
 * 퀴즈 답변 제출 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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