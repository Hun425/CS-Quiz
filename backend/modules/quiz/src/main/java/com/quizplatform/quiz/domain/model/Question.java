package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 문제 엔티티 클래스
 * 
 * <p>퀴즈를 구성하는 개별 문제를 관리합니다.
 * 여러 유형의 문제와 답변 옵션을 지원합니다.</p>
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
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /**
     * 문제 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 문제 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    /**
     * 정답 설명
     */
    @Column(name = "explanation", columnDefinition = "TEXT")
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
     * 정답 옵션 (MULTIPLE_CHOICE, SINGLE_CHOICE 유형용)
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * 주관식 정답 (TEXT, CODE 유형용)
     */
    @Column(name = "correct_answer")
    private String correctAnswer;

    /**
     * 코드 스니펫 (프로그래밍 문제용)
     */
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * 문제 생성자
     * 
     * @param content 문제 내용
     * @param questionType 문제 유형
     * @param explanation 정답 설명
     * @param points 배점
     * @param order 문제 순서
     * @param correctAnswer 주관식 정답
     * @param codeSnippet 코드 스니펫
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
    public void updateInfo(String content, String explanation, 
                         int points, String correctAnswer, String codeSnippet) {
        this.content = content;
        this.explanation = explanation;
        this.points = points;
        this.correctAnswer = correctAnswer;
        this.codeSnippet = codeSnippet;
    }
} 