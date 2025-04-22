package application.port.in.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 퀴즈 문제에 대한 답변 제출 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class SubmitAnswerCommand {
    private final Long quizAttemptId;
    private final Long questionId;
    private final String userAnswer;
    private final Integer timeTaken;

    @Builder
    public SubmitAnswerCommand(
            Long quizAttemptId,
            Long questionId,
            String userAnswer,
            Integer timeTaken
    ) {
        // 유효성 검사
        if (quizAttemptId == null) {
            throw new IllegalArgumentException("Quiz attempt ID must not be null");
        }
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID must not be null");
        }
        if (userAnswer == null) {
            throw new IllegalArgumentException("User answer must not be null");
        }

        this.quizAttemptId = quizAttemptId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.timeTaken = timeTaken;
    }
}