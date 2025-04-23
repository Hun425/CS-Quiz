package com.quizplatform.quiz.domain.model;

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
 */
@Entity
@Table(name = "quizzes", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 퀴즈 생성자(사용자) ID
     */
    @Column(name = "creator_id")
    private Long creatorId;

    /**
     * 퀴즈 제목
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 퀴즈 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 퀴즈 주제/카테고리
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 난이도 (1-5)
     */
    @Column(nullable = false)
    private int difficulty;

    /**
     * 제한 시간(분)
     */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /**
     * 합격 점수(%)
     */
    @Column(name = "passing_score", nullable = false)
    private int passingScore;

    /**
     * 퀴즈 활성화 여부
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * 퀴즈 공개 여부
     */
    @Column(nullable = false)
    private boolean published;

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
    @ElementCollection
    @CollectionTable(name = "quiz_tags", schema = "quiz_schema", 
                    joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

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
     * @param creatorId 퀴즈 생성자(사용자) ID
     * @param title 퀴즈 제목
     * @param description 퀴즈 설명
     * @param category 카테고리
     * @param difficulty 난이도
     * @param timeLimit 제한 시간
     * @param passingScore 합격 점수
     */
    @Builder
    public Quiz(Long creatorId, String title, String description, String category, int difficulty, 
                Integer timeLimit, int passingScore) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
        this.passingScore = passingScore;
        this.active = true;
        this.published = false;
    }

    /**
     * 퀴즈 정보 업데이트
     * 
     * @param title 새 제목
     * @param description 새 설명
     * @param category 새 카테고리
     * @param difficulty 새 난이도
     * @param timeLimit 새 제한 시간
     * @param passingScore 새 합격 점수
     */
    public void updateInfo(String title, String description, String category, 
                         int difficulty, Integer timeLimit, int passingScore) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
        this.passingScore = passingScore;
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
     * 퀴즈에 태그 추가
     * 
     * @param tag 추가할 태그
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    /**
     * 퀴즈에 태그 제거
     * 
     * @param tag 제거할 태그
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
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
                .creatorId(this.creatorId)
                .title("[Daily] " + this.title)
                .description(this.description)
                .category(this.category)
                .difficulty(this.difficulty)
                .timeLimit(this.timeLimit)
                .passingScore(this.passingScore)
                .build();
        
        // 유효기간 설정 (하루)
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        dailyQuiz.setValidUntil(tomorrow);
        
        return dailyQuiz;
    }

    /**
     * 배틀 퀴즈 복사본 생성
     * 
     * <p>기존 퀴즈의 정보를 바탕으로 배틀용 퀴즈 복사본을 생성합니다.</p>
     * 
     * @return 생성된 배틀 퀴즈 복사본
     */
    public Quiz createBattleCopy() {
        Quiz battleQuiz = Quiz.builder()
                .creatorId(this.creatorId)
                .title("[Battle] " + this.title)
                .description(this.description)
                .category(this.category)
                .difficulty(this.difficulty)
                .timeLimit(this.timeLimit)
                .passingScore(this.passingScore)
                .build();
        
        return battleQuiz;
    }

    /**
     * 퀴즈 만료 여부 확인
     * 
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isExpired() {
        if (this.validUntil == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.validUntil);
    }

    /**
     * 퀴즈 공개 여부 설정
     * 
     * @param published 공개 여부
     */
    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * 퀴즈 활성화 상태 변경
     * 
     * @param active 활성화 여부
     */
    public void setActive(boolean active) {
        this.active = active;
    }
} 