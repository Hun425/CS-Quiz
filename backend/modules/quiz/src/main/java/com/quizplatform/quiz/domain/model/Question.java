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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * 문제가 포함된 퀴즈
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
     * 문제 설명 (옵션)
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * 문제 타입 (MULTIPLE_CHOICE, SHORT_ANSWER 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    /**
     * 객관식 옵션 (JSON 형식으로 저장)
     */
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    /**
     * 정답 (문제 유형에 따라 형식이 다름)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    /**
     * 배점 (기본값 1점)
     */
    @Column(nullable = false)
    private int points = 1;

    /**
     * 정렬 순서
     */
    @Column(name = "display_order")
    private int displayOrder;

    /**
     * 난이도
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    /**
     * 시간 제한 (초 단위, 옵션)
     */
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 문제 생성자
     * 
     * @param content 문제 내용
     * @param explanation 문제 설명
     * @param questionType 문제 타입
     * @param options 객관식 옵션 (리스트 형태)
     * @param answer 정답
     * @param points 배점
     * @param displayOrder 정렬 순서
     * @param difficultyLevel 난이도
     * @param timeLimitSeconds 개별 문제 시간 제한 (초)
     */
    @Builder
    public Question(String content, String explanation, QuestionType questionType, 
                   List<String> options, String answer, int points, int displayOrder,
                   DifficultyLevel difficultyLevel, Integer timeLimitSeconds) {
        this.content = content;
        this.explanation = explanation;
        this.questionType = questionType;
        this.setOptions(options);
        this.answer = answer;
        this.points = points > 0 ? points : 1;
        this.displayOrder = displayOrder;
        this.difficultyLevel = difficultyLevel;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    /**
     * 퀴즈 설정
     * 
     * @param quiz 연결할 퀴즈
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    /**
     * 문제 정보 업데이트
     * 
     * @param content 새 문제 내용
     * @param explanation 새 설명
     * @param options 새 객관식 옵션
     * @param answer 새 정답
     * @param points 새 배점
     * @param difficultyLevel 새 난이도
     * @param timeLimitSeconds 새 시간 제한
     */
    public void update(String content, String explanation, List<String> options, 
                      String answer, int points, DifficultyLevel difficultyLevel, Integer timeLimitSeconds) {
        this.content = content;
        this.explanation = explanation;
        this.setOptions(options);
        this.answer = answer;
        this.points = points > 0 ? points : 1;
        this.difficultyLevel = difficultyLevel;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    /**
     * 객관식 옵션 설정
     * 
     * @param options 옵션 리스트
     */
    private void setOptions(List<String> options) {
        if (options != null && !options.isEmpty()) {
            this.options = String.join("||", options);
        }
    }

    /**
     * 객관식 옵션 조회
     * 
     * @return 옵션 리스트
     */
    public List<String> getOptionsList() {
        if (this.options == null || this.options.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(this.options.split("\\|\\|"))
                .collect(Collectors.toList());
    }

    /**
     * 정답 목록 조회 (체크박스 유형)
     * 
     * @return 정답 인덱스 목록
     */
    public List<Integer> getMultipleAnswerIndices() {
        if (questionType != QuestionType.CHECKBOX) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(answer.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 답변 채점
     * 
     * @param userAnswer 사용자 답변
     * @return 정답 여부 (true: 정답, false: 오답)
     */
    public boolean checkAnswer(String userAnswer) {
        if (userAnswer == null) {
            return false;
        }
        
        switch (questionType) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                return answer.equals(userAnswer);
                
            case CHECKBOX:
                // 복수 정답 비교 (순서 상관없음)
                List<String> expected = Arrays.asList(answer.split(","));
                List<String> actual = Arrays.asList(userAnswer.split(","));
                return expected.size() == actual.size() && 
                       expected.containsAll(actual);
                
            case SHORT_ANSWER:
                // 대소문자 구분 없이 비교
                return answer.equalsIgnoreCase(userAnswer);
                
            case FILL_IN_BLANK:
                // 정확히 일치해야 함
                return answer.equals(userAnswer);
                
            case ESSAY:
                // 서술형은 자동 채점 불가
                return false;
                
            default:
                return false;
        }
    }

    /**
     * 부분 점수 계산 (체크박스 유형)
     * 
     * @param userAnswer 사용자 답변
     * @return 부분 점수 (0.0 ~ 1.0)
     */
    public double calculatePartialScore(String userAnswer) {
        if (userAnswer == null || questionType != QuestionType.CHECKBOX) {
            return 0.0;
        }
        
        List<Integer> correctAnswers = getMultipleAnswerIndices();
        List<Integer> userAnswers = Arrays.stream(userAnswer.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        
        if (correctAnswers.isEmpty()) {
            return 0.0;
        }
        
        int correctCount = 0;
        for (Integer selected : userAnswers) {
            if (correctAnswers.contains(selected)) {
                correctCount++;
            } else {
                // 오답 선택 시 감점
                correctCount--;
            }
        }
        
        // 최소 0점
        double score = Math.max(0.0, (double) correctCount / correctAnswers.size());
        return Math.min(1.0, score); // 최대 1점
    }
} 