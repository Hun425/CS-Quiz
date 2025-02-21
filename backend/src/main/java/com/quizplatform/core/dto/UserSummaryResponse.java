package com.quizplatform.core.dto;

import com.quizplatform.core.domain.user.User;
import lombok.Builder;
import lombok.Getter;


import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String profileImage;
    private int level;
    private ZonedDateTime joinedAt;

    public static UserSummaryResponse from(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImage(user.getProfileImage())
                .level(user.getLevel())
                .joinedAt(user.getCreatedAt())
                .build();
    }
}