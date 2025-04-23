package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 문제 엔티티
 * 
 * <p>퀴즈에 포함된 문제 정보를 저장합니다.
 * 문제 내용, 정답, 설명, 배점 등을 관리합니다.</p>
 */
@Entity
@Table(name = "questions", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    /**
     * 문제 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    /**
     * 문제 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 문제 타입 (객관식, 주관식, 참/거짓 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    /**
     * 정답 설명
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * 배점
     */
    @Column(nullable = false)
    private int points;

    /**
     * 문제 순서
     */
    @Column(name = "question_order", nullable = false)
    private int order;

    /**
     * 주관식 정답 (주관식 문제인 경우에만 사용)
     */
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    /**
     * 코드 스니펫 (코딩 문제의 경우)
     */
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    /**
     * 문제 선택지 목록
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    @Builder
    public Question(String content, QuestionType questionType, String explanation, 
                   int points, int order, String correctAnswer, String codeSnippet) {
        this.content = content;
        this.questionType = questionType;
        this.explanation = explanation;
        this.points = points;
        this.order = order;
        this.correctAnswer = correctAnswer;
        this.codeSnippet = codeSnippet;
    }

    /**
     * 문제에 퀴즈 설정
     * 
     * @param quiz 연결할 퀴즈
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    /**
     * 선택지 추가
     * 
     * @param option 추가할 선택지
     */
    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    /**
     * 선택지 제거
     * 
     * @param option 제거할 선택지
     */
    public void removeOption(QuestionOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    /**
     * 정답 확인
     * 
     * @param answer 사용자 답변
     * @return 정답 여부
     */
    public boolean checkAnswer(String answer) {
        if (questionType == QuestionType.MULTIPLE_CHOICE || questionType == QuestionType.SINGLE_CHOICE) {
            // 객관식일 경우 선택지 ID로 확인
            return options.stream()
                    .filter(QuestionOption::isCorrect)
                    .anyMatch(o -> o.getId().toString().equals(answer));
        } else if (questionType == QuestionType.TRUE_FALSE) {
            // 참/거짓 문제
            return correctAnswer.equalsIgnoreCase(answer.trim());
        } else {
            // 주관식일 경우 텍스트 비교
            return correctAnswer.equalsIgnoreCase(answer.trim());
        }
    }

    /**
     * 문제 정보 업데이트
     * 
     * @param content 문제 내용
     * @param explanation 정답 설명
     * @param points 배점
     * @param correctAnswer 주관식 정답
     * @param codeSnippet 코드 스니펫
     */
    public void update(String content, String explanation, int points, 
                     String correctAnswer, String codeSnippet) {
        this.content = content;
        this.explanation = explanation;
        this.points = points;
        this.correctAnswer = correctAnswer;
        this.codeSnippet = codeSnippet;
    }
} 