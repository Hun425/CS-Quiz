package com.quizplatform.core.domain.quiz;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "quizzes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type")
    private QuizType quizType;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "question_count")
    private int questionCount;

    @Column(name = "time_limit")
    private Integer timeLimit; // 분 단위

    @Column(name = "is_public")
    private boolean isPublic = true;

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "attempt_count")
    private int attemptCount = 0;

    @Column(name = "avg_score")
    private double avgScore = 0.0;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "quiz_tags",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Quiz(User creator, String title, String description, QuizType quizType,
                DifficultyLevel difficultyLevel, Integer timeLimit) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.quizType = quizType;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
    }

    // 퀴즈 정보 업데이트
    public void update(String title, String description, DifficultyLevel difficultyLevel, Integer timeLimit) {
        this.title = title;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
    }

    // 문제 추가
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
        this.questionCount = questions.size();
    }

    // 태그 업데이트
    public void updateTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    // 태그 추가
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    // 퀴즈 시도 기록
    public void recordAttempt(double score) {
        this.attemptCount++;
        this.avgScore = ((this.avgScore * (this.attemptCount - 1)) + score) / this.attemptCount;
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 데일리 퀴즈 유효기간 설정
    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    // 데일리 퀴즈 복사본 생성
    public Quiz createDailyCopy() {
        Quiz dailyQuiz = Quiz.builder()
                .creator(this.creator)
                .title("[Daily] " + this.title)
                .description(this.description)
                .quizType(QuizType.DAILY)
                .difficultyLevel(this.difficultyLevel)
                .timeLimit(this.timeLimit)
                .build();

        // 태그 복사
        this.tags.forEach(dailyQuiz::addTag);

        // 문제 복사
        this.questions.forEach(question -> {
            Question copiedQuestion = question.copy();
            dailyQuiz.addQuestion(copiedQuestion);
        });

        return dailyQuiz;
    }

    // 남은 시간 확인
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    // 공개 여부 설정
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}