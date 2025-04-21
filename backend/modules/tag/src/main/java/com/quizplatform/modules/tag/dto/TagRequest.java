package com.quizplatform.modules.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 태그 생성/수정 요청 DTO
 * <p>
 * 태그 생성 및 수정 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {
    private String name;
    private String description;
    private String color;
}