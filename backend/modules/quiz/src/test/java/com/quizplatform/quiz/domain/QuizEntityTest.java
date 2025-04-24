package com.quizplatform.quiz.domain;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.Question;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuizEntityTest {

    @Test
    @DisplayName("퀴즈 엔티티 생성 테스트")
    void createQuizTest() {
        // given
        String title = "Java 기초 문제";
        String description = "자바 기초 지식을 테스트합니다";
        String category = "프로그래밍";
        int difficulty = 2;
        int passingScore = 70;
        
        // when
        Quiz quiz = Quiz.builder()
                .title(title)
                .description(description)
                .category(category)
                .difficulty(difficulty)
                .passingScore(passingScore)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
        
        // then
        assertThat(quiz).isNotNull();
        assertThat(quiz.getTitle()).isEqualTo(title);
        assertThat(quiz.getDescription()).isEqualTo(description);
        assertThat(quiz.getCategory()).isEqualTo(category);
        assertThat(quiz.getDifficulty()).isEqualTo(difficulty);
        assertThat(quiz.getPassingScore()).isEqualTo(passingScore);
    }
    
    @Test
    @DisplayName("퀴즈 정보 업데이트 테스트")
    void updateQuizTest() {
        // given
        Quiz quiz = Quiz.builder()
                .title("Java 기초 문제")
                .description("자바 기초 지식을 테스트합니다")
                .category("프로그래밍")
                .difficulty(2)
                .passingScore(70)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
        
        String newTitle = "Java 중급 문제";
        String newDescription = "자바 중급 지식을 테스트합니다";
        String newCategory = "Java";
        int newDifficulty = 3;
        int newPassingScore = 80;
        DifficultyLevel newDifficultyLevel = DifficultyLevel.INTERMEDIATE;
        
        // when
        quiz.updateInfo(
                newTitle,
                newDescription,
                newCategory,
                newDifficulty,
                30, // timeLimit
                newPassingScore,
                newDifficultyLevel
        );
        
        // then
        assertThat(quiz.getTitle()).isEqualTo(newTitle);
        assertThat(quiz.getDescription()).isEqualTo(newDescription);
        assertThat(quiz.getCategory()).isEqualTo(newCategory);
        assertThat(quiz.getDifficulty()).isEqualTo(newDifficulty);
        assertThat(quiz.getPassingScore()).isEqualTo(newPassingScore);
        assertThat(quiz.getDifficultyLevel()).isEqualTo(newDifficultyLevel);
    }
    
    @Test
    @DisplayName("퀴즈에 문제 추가 테스트")
    void addQuestionTest() {
        // given
        Quiz quiz = Quiz.builder()
                .title("Java 기초 문제")
                .description("자바 기초 지식을 테스트합니다")
                .category("프로그래밍")
                .difficulty(2)
                .passingScore(70)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
                
        Question question = Question.builder()
                .content("자바의 기본 자료형이 아닌 것은?")
                .type("MULTIPLE_CHOICE")
                .answer("String")
                .explanation("String은 참조형 자료형입니다.")
                .points(10)
                .order(1)
                .build();
                
        // when
        quiz.addQuestion(question);
        
        // then
        assertThat(quiz.getQuestions()).hasSize(1);
        assertThat(quiz.getQuestionCount()).isEqualTo(1);
        assertThat(quiz.getQuestions().get(0).getContent()).isEqualTo("자바의 기본 자료형이 아닌 것은?");
    }
} 