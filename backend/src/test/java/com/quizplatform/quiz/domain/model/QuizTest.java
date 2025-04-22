package com.quizplatform.quiz.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Quiz 도메인 모델 테스트 클래스
 */
public class QuizTest {

    @Test
    @DisplayName("퀴즈 생성 시 기본 상태값이 올바르게 설정되어야 한다")
    void createQuizWithBasicProperties() {
        // given
        Long createdBy = 1L;
        LocalDateTime now = LocalDateTime.now();
        
        // when
        Quiz quiz = Quiz.builder()
                .title("자바 기초 퀴즈")
                .description("자바 기초 문법에 관한 퀴즈입니다.")
                .difficultyLevel(DifficultyLevel.MEDIUM)
                .timeLimitSeconds(300)
                .passingScore(70)
                .isPublic(true)
                .quizType(QuizType.PRACTICE)
                .createdBy(createdBy)
                .createdAt(now)
                .build();
        
        // then
        assertThat(quiz.getTitle()).isEqualTo("자바 기초 퀴즈");
        assertThat(quiz.getDescription()).isEqualTo("자바 기초 문법에 관한 퀴즈입니다.");
        assertThat(quiz.getDifficultyLevel()).isEqualTo(DifficultyLevel.MEDIUM);
        assertThat(quiz.getTimeLimitSeconds()).isEqualTo(300);
        assertThat(quiz.getPassingScore()).isEqualTo(70);
        assertThat(quiz.isPublic()).isTrue();
        assertThat(quiz.getQuizType()).isEqualTo(QuizType.PRACTICE);
        assertThat(quiz.getCreatedBy()).isEqualTo(createdBy);
        assertThat(quiz.getCreatedAt()).isEqualTo(now);
        
        // 초기 통계는 0으로 설정
        assertThat(quiz.getTotalAttempts()).isNull();
        assertThat(quiz.getAverageScore()).isNull();
        assertThat(quiz.getPassRate()).isNull();
    }
    
    @Test
    @DisplayName("퀴즈에 문제를 추가하면 문제 목록에 포함되어야 한다")
    void addQuestionToQuiz() {
        // given
        Quiz quiz = Quiz.builder()
                .title("자바 기초 퀴즈")
                .build();
        
        Question question1 = Question.builder()
                .questionText("자바의 원시 타입이 아닌 것은?")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .points(10)
                .build();
        
        Question question2 = Question.builder()
                .questionText("자바의 접근 제한자 중 가장 제한적인 것은?")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .points(10)
                .build();
        
        // when
        quiz.setQuestions(Arrays.asList(question1, question2));
        
        // then
        assertThat(quiz.getQuestions()).hasSize(2);
        assertThat(quiz.getQuestions()).contains(question1, question2);
    }
    
    @Test
    @DisplayName("퀴즈에 태그를 추가하면 태그 목록에 포함되어야 한다")
    void addTagsToQuiz() {
        // given
        Quiz quiz = Quiz.builder()
                .title("자바 기초 퀴즈")
                .build();
        
        Tag tag1 = Tag.builder()
                .name("자바")
                .build();
        
        Tag tag2 = Tag.builder()
                .name("프로그래밍")
                .build();
        
        // when
        quiz.setTags(new HashSet<>(Arrays.asList(tag1, tag2)));
        
        // then
        assertThat(quiz.getTags()).hasSize(2);
        assertThat(quiz.getTags()).contains(tag1, tag2);
    }
    
    @Test
    @DisplayName("퀴즈 통계 정보가 올바르게 설정되어야 한다")
    void updateQuizStatistics() {
        // given
        Quiz quiz = Quiz.builder()
                .title("자바 기초 퀴즈")
                .passingScore(70)
                .build();
        
        // when
        quiz.setTotalAttempts(100);
        quiz.setAverageScore(75.5);
        quiz.setPassRate(0.85);
        
        // then
        assertThat(quiz.getTotalAttempts()).isEqualTo(100);
        assertThat(quiz.getAverageScore()).isEqualTo(75.5);
        assertThat(quiz.getPassRate()).isEqualTo(0.85);
    }
}
