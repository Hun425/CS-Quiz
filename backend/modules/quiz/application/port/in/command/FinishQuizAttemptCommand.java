package application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 시도 완료를 위한 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class FinishQuizAttemptCommand {
    private final Long quizAttemptId;
    private final Long userId;

    @Builder
    public FinishQuizAttemptCommand(Long quizAttemptId, Long userId) {
        // 유효성 검사
        if (quizAttemptId == null) {
            throw new IllegalArgumentException("Quiz attempt ID must not be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        this.quizAttemptId = quizAttemptId;
        this.userId = userId;
    }
}