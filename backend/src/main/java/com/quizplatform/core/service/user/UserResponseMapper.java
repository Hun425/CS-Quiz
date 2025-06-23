package com.quizplatform.core.service.user;

import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.user.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * User 엔티티를 표준 응답 DTO로 변환하는 매퍼 클래스
 * 
 * <p>데이터 변환 로직을 중앙화하여 일관성을 보장하고,
 * 쿼리 방식 변경과 무관하게 동일한 응답 구조를 유지합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
public class UserResponseMapper {

    /**
     * User 엔티티를 UserProfileResponse로 변환합니다.
     * 
     * @param user 변환할 User 엔티티
     * @return 표준화된 UserProfileResponse
     */
    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) {
            log.warn("Null user provided to mapper");
            return null;
        }

        log.debug("Converting User entity to UserProfileResponse: id={}", user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .level(user.getLevel())
                .experience(user.getExperience())
                .requiredExperience(user.getRequiredExperience())
                .totalPoints(user.getTotalPoints())
                .joinedAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                // 추가 데이터는 별도 계산 메서드로 분리
                .totalQuizzes(calculateTotalQuizzes(user))
                .totalReviews(calculateTotalReviews(user))
                .build();
    }

    /**
     * 사용자의 총 퀴즈 시도 횟수를 계산합니다.
     * 
     * @param user 계산 대상 사용자
     * @return 총 퀴즈 시도 횟수
     */
    private Integer calculateTotalQuizzes(User user) {
        // Lazy Loading 방지 및 null 체크
        if (user.getQuizAttempts() == null) {
            log.debug("QuizAttempts not loaded for user {}, returning 0", user.getId());
            return 0;
        }
        return user.getQuizAttempts().size();
    }

    /**
     * 사용자의 총 리뷰 작성 횟수를 계산합니다.
     * 
     * @param user 계산 대상 사용자
     * @return 총 리뷰 작성 횟수
     */
    private Integer calculateTotalReviews(User user) {
        // 리뷰 관련 필드가 있다면 계산, 없으면 0 반환
        // TODO: User 엔티티에 reviews 관계가 추가되면 구현
        return 0;
    }

    /**
     * 여러 User 엔티티를 UserProfileResponse 리스트로 변환합니다.
     * 
     * @param users 변환할 User 엔티티 리스트
     * @return UserProfileResponse 리스트
     */
    public java.util.List<UserProfileResponse> toProfileResponseList(java.util.List<User> users) {
        if (users == null || users.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return users.stream()
                .map(this::toProfileResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}