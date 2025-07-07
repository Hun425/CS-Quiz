package com.quizplatform.core.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 사용자 프로필 표준 응답 DTO
 * 
 * <p>프론트엔드와의 API 계약을 위한 표준화된 응답 구조입니다.
 * 내부 쿼리 방식이 변경되어도 이 응답 구조는 유지됩니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "사용자 프로필 응답")
public class UserProfileResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자명", example = "홍길동")
    private String username;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "사용자 레벨", example = "5")
    private Integer level;

    @Schema(description = "현재 경험치", example = "1250")
    private int experience;

    @Schema(description = "다음 레벨까지 필요 경험치", example = "1500")
    private int requiredExperience;

    @Schema(description = "총 포인트", example = "3450")
    private int totalPoints;

    @Schema(description = "가입 일시", example = "2024-01-15 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime joinedAt;

    @Schema(description = "마지막 로그인 일시", example = "2024-06-23 09:15:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime lastLogin;

    // 확장 가능한 필드들 (필요 시 추가)
    @Schema(description = "총 퀴즈 시도 횟수", example = "42")
    private Integer totalQuizzes;

    @Schema(description = "총 리뷰 작성 횟수", example = "8")
    private Integer totalReviews;
}