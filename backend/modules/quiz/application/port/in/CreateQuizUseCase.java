package application.port.in;

import application.port.in.command.CreateQuizCommand;
import domain.model.Quiz;

/**
 * 퀴즈 및 데일리 퀴즈 생성 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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