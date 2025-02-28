package com.quizplatform.core.domain.user;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_level_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "previous_level", nullable = false)
    private int previousLevel;

    @Column(name = "level", nullable = false)
    private int level;

    @CreatedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserLevelHistory(User user, int previousLevel, int level) {
        this.user = user;
        this.previousLevel = previousLevel;
        this.level = level;
    }
}