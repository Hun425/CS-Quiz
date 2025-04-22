package domain.model;

import lombok.Builder;
import lombok.Getter;
import domain.model.DifficultyLevel;
import domain.model.Question;
import domain.model.QuizType;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * 퀴즈 도메인 모델
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class Quiz {
    private final Long id;
    private final Long creatorId;
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private int questionCount;
    private Integer timeLimit;
    private boolean isPublic;
    
    private int viewCount;
    private int attemptCount;
    private double avgScore;
    
    private LocalDateTime validUntil;
    private Set<Question> questions;
    private Set<Long> tagIds;
    
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Quiz(
            Long id, 
            Long creatorId, 
            String title, 
            String description, 
            QuizType quizType,
            DifficultyLevel difficultyLevel,
            Integer timeLimit,
            boolean isPublic,
            int viewCount,
            int attemptCount,
            double avgScore,
            LocalDateTime validUntil,
            Set<Question> questions,
            Set<Long> tagIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.quizType = quizType != null ? quizType : QuizType.REGULAR;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        this.timeLimit = timeLimit;
        this.isPublic = isPublic;
        this.viewCount = viewCount;
        this.attemptCount = attemptCount;
        this.avgScore = avgScore;
        this.validUntil = validUntil;
        this.questions = questions != null ? questions : new LinkedHashSet<>();
        this.questionCount = this.questions.size();
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    /**
     * 퀴즈 정보 업데이트
     */
    public void update(String title, String description, DifficultyLevel difficultyLevel, Integer timeLimit) {
        this.title = title;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 퀴즈에 문제 추가
     */
    public void addQuestion(Question question) {
        this.questions.add(question);
        this.questionCount = this.questions.size();
    }

    /**
     * 퀴즈 태그 ID 업데이트
     */
    public void updateTags(Set<Long> newTagIds) {
        this.tagIds.clear();
        this.tagIds.addAll(newTagIds);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 퀴즈에 태그 ID 추가
     */
    public void addTagId(Long tagId) {
        this.tagIds.add(tagId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 퀴즈 시도 기록 및 통계 업데이트
     */
    public void recordAttempt(double score) {
        this.attemptCount++;
        // 가중 평균 계산
        this.avgScore = ((this.avgScore * (this.attemptCount - 1)) + score) / this.attemptCount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 퀴즈 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 퀴즈 유효 기간 설정
     */
    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 데일리 퀴즈 복사본 생성
     */
    public Quiz createDailyCopy() {
        Quiz dailyQuiz = Quiz.builder()
                .creatorId(this.creatorId)
                .title("[Daily] " + this.title)
                .description(this.description)
                .quizType(QuizType.DAILY)
                .difficultyLevel(this.difficultyLevel)
                .timeLimit(this.timeLimit)
                .isPublic(true)
                .build();

        // 태그 복사
        dailyQuiz.tagIds.addAll(this.tagIds);

        // 문제 복사
        for (Question question : this.questions) {
            Question copiedQuestion = question.copy();
            dailyQuiz.addQuestion(copiedQuestion);
        }

        return dailyQuiz;
    }

    /**
     * 퀴즈 만료 여부 확인
     */
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }

    /**
     * 퀴즈 공개 여부 설정
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 문제 목록 설정
     */
    public void setQuestions(Set<Question> questions) {
        this.questions = new LinkedHashSet<>(questions);
        this.questionCount = this.questions.size();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 시간 제한을 초 단위로 반환합니다.
     */
    public Integer getTimeLimitSeconds() {
        return this.timeLimit != null ? this.timeLimit * 60 : null;
    }
    
    /**
     * 합격 점수를 반환합니다 (기본값 60).
     */
    public Integer getPassingScore() {
        return 60; // 기본 합격 점수 60점
    }
    
    /**
     * 생성자 ID로부터 생성자 이름 획득을 위한 메소드
     */
    public Long getCreatedBy() {
        return this.creatorId;
    }
    
    /**
     * 평균 점수를 반환합니다.
     */
    public double getAverageScore() {
        return this.avgScore;
    }
    
    /**
     * 총 시도 횟수를 반환합니다.
     */
    public int getTotalAttempts() {
        return this.attemptCount;
    }
    
    /**
     * 합격률을 반환합니다 (예시값 반환).
     */
    public double getPassRate() {
        // 실제 구현에서는 계산해야 하나, 샘플 데이터 반환
        return this.avgScore > 0 ? this.avgScore / 100.0 : 0.0;
    }
}