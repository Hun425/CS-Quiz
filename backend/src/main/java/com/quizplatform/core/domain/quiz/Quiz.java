package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.user.User;
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
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 200)
    private String title;

    private String description;

    @Column(name = "quiz_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "avg_score", precision = 5, scale = 2)
    private Double avgScore;

    @Column(name = "valid_until")
    private ZonedDateTime validUntil;

    @ManyToMany
    @JoinTable(
            name = "quiz_tags",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        viewCount = 0;
        attemptCount = 0;
        avgScore = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    @Builder
    public Quiz(User creator, String title, String description, QuizType quizType,
                DifficultyLevel difficultyLevel, Integer questionCount,
                Integer timeLimit, boolean isPublic, ZonedDateTime validUntil) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.quizType = quizType;
        this.difficultyLevel = difficultyLevel;
        this.questionCount = questionCount;
        this.timeLimit = timeLimit;
        this.isPublic = isPublic;
        this.validUntil = validUntil;
        this.viewCount = 0;
        this.attemptCount = 0;
        this.avgScore = 0.0;
    }

    // 비즈니스 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    public void addNewAttempt(double score) {
        this.attemptCount++;
        this.avgScore = ((this.avgScore * (this.attemptCount - 1)) + score) / this.attemptCount;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public boolean isValid() {
        return validUntil == null || ZonedDateTime.now().isBefore(validUntil);
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }
}