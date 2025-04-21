package com.quizplatform.modules.user.dto;


import com.quizplatform.modules.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

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