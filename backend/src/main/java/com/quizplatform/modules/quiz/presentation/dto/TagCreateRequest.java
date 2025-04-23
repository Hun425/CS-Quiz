package com.quizplatform.modules.quiz.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@Schema(description = "태그 생성 요청")
public class TagCreateRequest {

    @NotBlank(message = "태그 이름은 필수입니다.")
    @Size(min = 1, max = 50, message = "태그 이름은 1~50자 사이여야 합니다.")
    @Schema(description = "태그 이름", example = "자바스크립트")
    private String name;

    @Size(max = 500, message = "태그 설명은 500자 이하여야 합니다.")
    @Schema(description = "태그 설명", example = "자바스크립트 프로그래밍 언어 관련 퀴즈")
    private String description;

    @Schema(description = "부모 태그 ID (계층 구조)", example = "1")
    private Long parentId;

    @Schema(description = "동의어 목록", example = "[\"JS\", \"JavaScript\"]")
    private Set<String> synonyms;
}