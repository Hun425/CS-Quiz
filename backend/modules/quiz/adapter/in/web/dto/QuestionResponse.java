package adapter.in.web.dto;

import domain.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 문제 응답 DTO 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Integer points;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer displayOrder;
    @Builder.Default
    private List<AnswerOptionResponse> answerOptions = new ArrayList<>();
    
    /**
     * 퀴즈 시도가 진행 중인 경우에는 정답 정보 제외
     */
    private String correctAnswerIds;

    /**
     * 답변 옵션 응답 DTO 클래스
     *
     * @author 채기훈
     * @since JDK 17.0.2 Eclipse Temurin
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerOptionResponse {
        private Long id;
        private String optionText;
        private Integer displayOrder;
    }
}
