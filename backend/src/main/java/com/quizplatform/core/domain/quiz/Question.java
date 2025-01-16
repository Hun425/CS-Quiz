package com.quizplatform.core.domain.quiz;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "question_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "code_snippet")
    private String codeSnippet;

    @Column(name = "diagram_data")
    private String diagramData;

    // JSONB 타입으로 선택지 저장
    @Column(name = "options", columnDefinition = "jsonb")
    private String options;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(name = "explanation")
    private String explanation;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        if (points == null) {
            points = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    @Builder
    public Question(Quiz quiz, QuestionType questionType, String questionText,
                    String codeSnippet, String diagramData, String options,
                    String correctAnswer, String explanation, Integer points,
                    DifficultyLevel difficultyLevel) {
        this.quiz = quiz;
        this.questionType = questionType;
        this.questionText = questionText;
        this.codeSnippet = codeSnippet;
        this.diagramData = diagramData;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.points = points != null ? points : 1;
        this.difficultyLevel = difficultyLevel;
    }

    // 비즈니스 메서드
    public boolean checkAnswer(String userAnswer) {
        // 문제 유형에 따른 정답 체크 로직
        return switch (questionType) {
            case MULTIPLE_CHOICE, TRUE_FALSE -> correctAnswer.equalsIgnoreCase(userAnswer.trim());
            case SHORT_ANSWER -> correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
            case CODE_ANALYSIS -> validateCodeAnswer(userAnswer);
            case DIAGRAM_BASED -> validateDiagramAnswer(userAnswer);
        };
    }

    private boolean validateCodeAnswer(String userAnswer) {
        // 코드 분석 문제의 경우, 여러 가능한 답안을 허용할 수 있음
        String[] acceptableAnswers = correctAnswer.split("\\|");
        for (String answer : acceptableAnswers) {
            if (answer.trim().equalsIgnoreCase(userAnswer.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean validateDiagramAnswer(String userAnswer) {
        // 다이어그램 기반 문제의 경우, 정해진 형식에 따라 답안 검증
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public void updateQuestion(String questionText, String correctAnswer, String explanation) {
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.updatedAt = ZonedDateTime.now();
    }
}