package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * 사용자 레벨의 '정의'를 나타내는 도메인 모델입니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class UserLevel {

    private final Long id;
    private final int level;
    private final String name;
    private final int requiredExperience;
    private final String description;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime modifiedAt;
} 