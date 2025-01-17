package com.quizplatform.core.domain.quiz;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "tags")
    private Set<Quiz> quizzes = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    @Builder
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // 비즈니스 메서드
    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
        quiz.getTags().add(this);
    }

    public void removeQuiz(Quiz quiz) {
        quizzes.remove(quiz);
        quiz.getTags().remove(this);
    }
}