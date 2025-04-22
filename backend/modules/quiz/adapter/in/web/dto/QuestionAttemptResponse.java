package adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 문제 시도 응답 DTO 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAttemptResponse {

    private Long id;
    private QuestionResponse question;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private LocalDateTime answerTime;
}
