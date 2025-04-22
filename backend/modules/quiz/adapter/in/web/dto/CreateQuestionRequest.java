package adapter.in.web.dto;

import domain.model.DifficultyLevel;
import domain.model.QuestionType; // Assuming QuestionType enum exists in domain.model
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set; // For correctAnswerIds

/**
 * 퀴즈 생성 시 개별 질문 요청 DTO
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {

    @NotBlank(message = "질문 내용은 필수입니다.")
    private String questionText;

    @NotNull(message = "질문 유형은 필수입니다.")
    private QuestionType questionType;

    @NotNull(message = "점수 배점은 필수입니다.")
    private Integer points;

    private String explanation;

    @NotNull(message = "질문 순서는 필수입니다.")
    private Integer displayOrder;

    // 추가된 필드
    private String codeSnippet;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimitSeconds;

    // 객관식/다중선택 유형에 필요할 수 있음
    private Set<Long> correctAnswerIds; // 혹은 String 타입 등 실제 구현에 맞게 조정 필요

    @NotEmpty(message = "최소 한 개 이상의 답변 옵션이 필요합니다.")
    @Size(min = 1, message = "최소 한 개 이상의 답변 옵션이 필요합니다.") // 객관식/다중선택 유형에 유효성 검사 추가 가능
    @Valid // 중첩 유효성 검사
    @Builder.Default
    private List<AnswerOptionRequest> answerOptions = new ArrayList<>();

}