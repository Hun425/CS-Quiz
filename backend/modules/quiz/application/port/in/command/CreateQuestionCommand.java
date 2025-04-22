package application.port.in.command;

import domain.model.DifficultyLevel;
import domain.model.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 내 문제를 생성하기 위한 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class CreateQuestionCommand {
    private final QuestionType questionType;
    private final String questionText;
    private final String codeSnippet;
    private final List<OptionCommand> options;
    private final String correctAnswer;
    private final String explanation;
    private final DifficultyLevel difficultyLevel;
    private final Integer points;
    private final Integer timeLimitSeconds;

    @Builder
    public CreateQuestionCommand(
            QuestionType questionType,
            String questionText,
            String codeSnippet,
            List<OptionCommand> options,
            String correctAnswer,
            String explanation,
            DifficultyLevel difficultyLevel,
            Integer points,
            Integer timeLimitSeconds
    ) {
        // 유효성 검사
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("Question text must not be empty");
        }
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("Correct answer must not be empty");
        }

        this.questionType = questionType != null ? questionType : QuestionType.MULTIPLE_CHOICE;
        this.questionText = questionText;
        this.codeSnippet = codeSnippet;
        this.options = options != null ? options : new ArrayList<>();
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        this.points = points != null ? points : 1;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    /**
     * 문제 옵션 정보를 담는 내부 커맨드 객체
     *
     * @author 채기훈
     * @since JDK 17.0.2 Eclipse Temurin
     */
    @Getter
    public static class OptionCommand {
        private final String key;
        private final String value;

        @Builder
        public OptionCommand(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}