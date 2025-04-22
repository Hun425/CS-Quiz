package domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import domain.model.Question;

import java.time.LocalDateTime;

/**
 * 문제 시도 도메인 모델
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAttempt {
    private Long id;
    private Long quizAttemptId;
    private Long questionId;
    private transient Question question;
    private Long selectedOptionId;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private Integer timeTaken;
    private LocalDateTime submittedAt;

    /**
     * 특정 문제에 대한 시도 생성
     */
    public static QuestionAttempt createAttempt(
            Long quizAttemptId,
            Question question,
            String userAnswer,
            Integer timeTaken
    ) {
        boolean isCorrect = question.isCorrectAnswer(userAnswer);
        
        return QuestionAttempt.builder()
                .quizAttemptId(quizAttemptId)
                .questionId(question.getId())
                .selectedOptionId(question.getOptionId(userAnswer))
                .isCorrect(isCorrect)
                .timeTaken(timeTaken)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    public void setQuestion(Question question) {
        this.question = question;
        if (question != null) {
            this.questionId = question.getId();
        }
    }
}