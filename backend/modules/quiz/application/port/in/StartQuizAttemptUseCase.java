package application.port.in;

import application.port.in.command.StartQuizAttemptCommand;
import domain.model.Quiz;
import domain.model.QuizAttempt;

/**
 * 퀴즈 시도 시작 및 플레이 가능 여부 확인 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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