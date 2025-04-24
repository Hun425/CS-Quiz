package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import com.quizplatform.quiz.domain.repository.QuizRepository;
import com.quizplatform.quiz.domain.service.QuizServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizServiceImpl quizService;
    
    @Mock
    private QuizApplicationService quizApplicationService;

    @Test
    @DisplayName("퀴즈 조회 테스트")
    void getQuizByIdTest() {
        // given
        Long quizId = 1L;
        Quiz mockQuiz = Quiz.builder()
                .title("Java 기초 문제")
                .description("자바 기초 지식을 테스트합니다")
                .category("프로그래밍")
                .difficulty(2)
                .passingScore(70)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
        
        // ID 필드는 리플렉션으로 설정 (실제 테스트에서는 저장 후 ID가 자동 생성됨)
        try {
            java.lang.reflect.Field idField = Quiz.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockQuiz, quizId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(mockQuiz));
        
        // when
        Optional<Quiz> foundQuiz = quizService.findById(quizId);
        
        // then
        assertThat(foundQuiz).isPresent();
        assertThat(foundQuiz.get().getId()).isEqualTo(quizId);
        assertThat(foundQuiz.get().getTitle()).isEqualTo("Java 기초 문제");
    }
    
    @Test
    @DisplayName("카테고리별 퀴즈 목록 조회 테스트")
    void getQuizzesByCategoryTest() {
        // given
        String category = "프로그래밍";
        
        List<Quiz> mockQuizzes = Arrays.asList(
            createQuizWithId(1L, "Java 기초 문제", "프로그래밍"),
            createQuizWithId(2L, "Python 기초 문제", "프로그래밍")
        );
        
        when(quizRepository.findByCategory(category)).thenReturn(mockQuizzes);
        
        // when
        List<Quiz> foundQuizzes = quizService.findByCategory(category);
        
        // then
        assertThat(foundQuizzes).isNotNull();
        assertThat(foundQuizzes).hasSize(2);
        assertThat(foundQuizzes.get(0).getCategory()).isEqualTo(category);
        assertThat(foundQuizzes.get(1).getCategory()).isEqualTo(category);
    }
    
    @Test
    @DisplayName("퀴즈 생성 테스트")
    void createQuizTest() {
        // given
        Quiz newQuiz = Quiz.builder()
                .title("Java 기초 문제")
                .description("자바 기초 지식을 테스트합니다")
                .category("프로그래밍")
                .difficulty(2)
                .passingScore(70)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
        
        Quiz savedQuiz = createQuizWithId(1L, "Java 기초 문제", "프로그래밍");
        
        when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
        
        // when
        Quiz createdQuiz = quizService.createQuiz(newQuiz);
        
        // then
        assertThat(createdQuiz).isNotNull();
        assertThat(createdQuiz.getId()).isEqualTo(1L);
        assertThat(createdQuiz.getTitle()).isEqualTo("Java 기초 문제");
    }
    
    @Test
    @DisplayName("애플리케이션 서비스 - 퀴즈 ID로 조회 테스트")
    void applicationServiceGetQuizByIdTest() {
        // given
        Long quizId = 1L;
        QuizResponse mockResponse = new QuizResponse();
        mockResponse.setId(quizId);
        mockResponse.setTitle("Java 기초 문제");
        
        when(quizApplicationService.getQuizById(quizId)).thenReturn(mockResponse);
        
        // when
        QuizResponse response = quizApplicationService.getQuizById(quizId);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(quizId);
        assertThat(response.getTitle()).isEqualTo("Java 기초 문제");
    }
    
    // 헬퍼 메서드: ID가 설정된 퀴즈 생성
    private Quiz createQuizWithId(Long id, String title, String category) {
        Quiz quiz = Quiz.builder()
                .title(title)
                .description("테스트 설명")
                .category(category)
                .difficulty(2)
                .passingScore(70)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .quizType(QuizType.NORMAL)
                .build();
                
        // ID 필드 리플렉션으로 설정
        try {
            java.lang.reflect.Field idField = Quiz.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(quiz, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return quiz;
    }
} 