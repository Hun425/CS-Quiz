package com.quizplatform.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 태그 생성 요청 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Builder
public record TagCreateRequest(
    
    /**
     * 태그 이름
     */
    @NotBlank(message = "태그 이름은 필수입니다")
    @Size(max = 50, message = "태그 이름은 50자를 초과할 수 없습니다")
    String name,
    
    /**
     * 태그 설명
     */
    @Size(max = 255, message = "태그 설명은 255자를 초과할 수 없습니다")
    String description,
    
    /**
     * 부모 태그 ID (루트 태그인 경우 null)
     */
    Long parentId
) {}