package com.quizplatform.quiz.application.dto;

import lombok.Builder;

/**
 * 태그 이동 요청 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Builder
public record TagMoveRequest(
    
    /**
     * 새 부모 태그 ID (루트로 이동시 null)
     */
    Long newParentId
) {}