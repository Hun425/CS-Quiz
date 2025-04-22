package adapter.in.web.dto;

import domain.model.DifficultyLevel;
import domain.model.QuizType;
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
import java.util.Set;

/**
 * 퀴즈 생성 요청 DTO 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuizRequest {

    @NotBlank(message = "제목은 필수 항목입니다")
    @Size(min = 3, max = 100, message = "제목은 3~100자 사이여야 합니다")
    private String title;

    @Size(max = 1000, message = "설명은 최대 1000자까지 입력 가능합니다")
    private String description;

    @NotNull(message = "난이도는 필수 항목입니다")
    private DifficultyLevel difficultyLevel;

    private Integer timeLimitSeconds;

    private Integer passingScore;

    @NotNull(message = "공개 여부는 필수 항목입니다")
    private Boolean isPublic;

    @NotNull(message = "퀴즈 유형은 필수 항목입니다")
    private QuizType quizType;

    @NotEmpty(message = "최소 한 개 이상의 문제가 필요합니다")
    @Size(min = 1, max = 50, message = "문제는 1~50개까지 등록 가능합니다")
    @Valid
    @Builder.Default
    private List<CreateQuestionRequest> questions = new ArrayList<>();

    private Set<Long> tagIds;
}
