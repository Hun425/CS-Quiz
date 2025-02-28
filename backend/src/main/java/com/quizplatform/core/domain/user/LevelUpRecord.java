package com.quizplatform.core.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class LevelUpRecord {
    private Long id;
    private Long userId;
    private Integer oldLevel;
    private Integer newLevel;
    private ZonedDateTime occurredAt;
}