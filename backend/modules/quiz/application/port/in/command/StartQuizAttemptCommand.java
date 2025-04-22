package application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 시도 시작을 위한 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class StartQuizAttemptCommand {
    private final Long quizId;
    private final Long userId;

    @Builder
    public StartQuizAttemptCommand(Long quizId, Long userId) {
        // 유효성 검사
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        this.quizId = quizId;
        this.userId = userId;
    }
}