package adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 질문 생성 시 답변 옵션 요청 DTO
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOptionRequest {

    @NotBlank(message = "옵션 내용은 필수입니다.")
    private String optionText;

    @NotNull(message = "옵션 순서는 필수입니다.")
    private Integer displayOrder;
} 