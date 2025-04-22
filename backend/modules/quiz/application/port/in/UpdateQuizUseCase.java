package application.port.in;

import application.port.in.command.UpdateQuizCommand;
import domain.model.Quiz;

/**
 * 퀴즈 수정 및 공개 상태 변경 기능을 정의하는 유스케이스 인터페이스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public interface UpdateQuizUseCase {
    /**
     * 퀴즈를 수정합니다.
     *
     * @param command 퀴즈 수정 명령 (ID 포함)
     * @return 수정된 퀴즈
     */
    Quiz updateQuiz(UpdateQuizCommand command);
    
    /**
     * 퀴즈의 공개 여부를 설정합니다.
     *
     * @param quizId 퀴즈 ID
     * @param isPublic 공개 여부
     * @return 수정된 퀴즈
     */
    Quiz setQuizPublic(Long quizId, boolean isPublic);
}