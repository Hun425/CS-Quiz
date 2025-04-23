package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.battle.BattleResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 퀴즈 엔티티 클래스
 * 
 * <p>퀴즈 정보, 문제 목록, 태그, 통계 등을 관리하는 핵심 엔티티입니다.
 * 일반 퀴즈와 데일리 퀴즈 두 가지 유형을 지원합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "quizzes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 퀴즈 생성자(사용자)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    /**
     * 퀴즈 제목
     */
    private String title;

    /**
     * 퀴즈 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 퀴즈 유형 (REGULAR, DAILY 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type")
    private QuizType quizType;

    /**
     * 퀴즈 난이도 (BEGINNER, INTERMEDIATE, ADVANCED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    /**
     * 퀴즈에 포함된 문제 수
     */
    @Column(name = "question_count")
    private int questionCount;

    /**
     * 퀴즈 제한 시간 (분 단위)
     */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /**
     * 퀴즈 공개 여부
     */
    @Column(name = "is_public")
    private boolean isPublic = true;

    /**
     * 퀴즈 조회수
     */
    @Column(name = "view_count")
    private int viewCount = 0;

    /**
     * 퀴즈 시도 횟수
     */
    @Column(name = "attempt_count")
    private int attemptCount = 0;

    /**
     * 퀴즈 평균 점수
     */
    @Column(name = "avg_score")
    private double avgScore = 0.0;

    /**
     * 퀴즈 유효 기간 (주로 데일리 퀴즈에서 사용)
     */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /**
     * 퀴즈에 포함된 문제 목록
     */
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 30)
    @OrderBy("id ASC")
    private Set<Question> questions = new LinkedHashSet<>();

    /**
     * 퀴즈에 연결된 태그 목록
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "quiz_tags", 
        joinColumns = @JoinColumn(name = "quiz_id"), 
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * 퀴즈 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 퀴즈 최종 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 퀴즈 생성자
     * 
     * @param creator 퀴즈 생성자(사용자)
     * @param title 퀴즈 제목
     * @param description 퀴즈 설명
     * @param quizType 퀴즈 유형
     * @param difficultyLevel 퀴즈 난이도
     * @param timeLimit 퀴즈 제한 시간 (분 단위)
     */
    @Builder
    public Quiz(User creator, String title, String description, QuizType quizType, DifficultyLevel difficultyLevel, Integer timeLimit) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.quizType = quizType;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
    }

    /**
     * 퀴즈 정보 업데이트
     * 
     * @param title 새 제목
     * @param description 새 설명
     * @param difficultyLevel 새 난이도
     * @param timeLimit 새 제한 시간
     */
    public void update(String title, String description, DifficultyLevel difficultyLevel, Integer timeLimit) {
        this.title = title;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
    }

    /**
     * 퀴즈에 문제 추가
     * 
     * @param question 추가할 문제
     */
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
        this.questionCount = questions.size();
    }

    /**
     * 퀴즈 태그 업데이트
     * 
     * @param newTags 새 태그 세트
     */
    public void updateTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    /**
     * 퀴즈에 태그 추가
     * 
     * @param tag 추가할 태그
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    /**
     * 퀴즈 시도 기록 및 통계 업데이트
     * 
     * @param score 이번 시도의 점수
     */
    public void recordAttempt(double score) {
        this.attemptCount++;
        // 가중 평균 계산
        this.avgScore = ((this.avgScore * (this.attemptCount - 1)) + score) / this.attemptCount;
    }

    /**
     * 퀴즈 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 퀴즈 유효 기간 설정
     * 
     * @param validUntil 유효 기간 만료 시점
     */
    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * 데일리 퀴즈 복사본 생성
     * 
     * <p>기존 퀴즈의 정보를 바탕으로 데일리 퀴즈 복사본을 생성합니다.</p>
     * 
     * @return 생성된 데일리 퀴즈 복사본
     */
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

    /**
     * 배틀 결과를 반영하여 퀴즈 통계 업데이트
     * 
     * @param result 배틀 결과
     */
    public void updateBattleStats(BattleResult result) {
        // 배틀의 최고 점수를 이번 퀴즈 시도의 점수로 기록
        recordAttempt(result.getHighestScore());
    }

    /**
     * 퀴즈 만료 여부 확인
     * 
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    /**
     * 퀴즈 공개 여부 설정
     * 
     * @param isPublic 공개 여부
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}