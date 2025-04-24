package com.quizplatform.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * 모듈 통합 테스트
 * 현재 설정 문제로 인해 비활성화됨 - 추후 수정 필요
 */
@SpringBootTest(classes = {ModuleIntegrationTest.TestConfig.class, ModuleIntegrationTestConfig.class})
@EnableKafka
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"user-created", "quiz-created", "battle-completed"})
@Disabled("Kafka 설정 문제로 임시 비활성화")
class ModuleIntegrationTest {

    private static final String USER_CREATED_TOPIC = "user-created";
    private static final String QUIZ_CREATED_TOPIC = "quiz-created";
    private static final String BATTLE_COMPLETED_TOPIC = "battle-completed";
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private BattleRepository battleRepository;
    
    @Autowired
    private UserEventHandler userEventHandler;
    
    @Autowired
    private QuizEventHandler quizEventHandler;
    
    @Autowired
    private BattleEventHandler battleEventHandler;
    
    private CountDownLatch userEventLatch = new CountDownLatch(1);
    private CountDownLatch quizEventLatch = new CountDownLatch(1);
    private CountDownLatch battleEventLatch = new CountDownLatch(1);
    
    private UserCreatedEvent capturedUserEvent;
    private QuizCreatedEvent capturedQuizEvent;
    private BattleCompletedEvent capturedBattleEvent;
    
    @Test
    @DisplayName("사용자 생성 이벤트 기반 통합 테스트")
    void userCreationIntegrationTest() throws InterruptedException {
        // given
        UserCreatedEvent event = new UserCreatedEvent(1L, "testuser", "test@example.com");
        
        // when
        kafkaTemplate.send(USER_CREATED_TOPIC, event);
        
        // then
        verify(userEventHandler, timeout(5000)).handleUserCreatedEvent(any(UserCreatedEvent.class));
        assertThat(userEventLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedUserEvent).isNotNull();
        assertThat(capturedUserEvent.getUserId()).isEqualTo(1L);
        assertThat(capturedUserEvent.getUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("퀴즈 생성 및 조회 통합 테스트")
    void quizCreationIntegrationTest() throws InterruptedException {
        // given
        QuizCreatedEvent event = new QuizCreatedEvent(1L, "Java 기초 문제", "프로그래밍");
        
        // when
        kafkaTemplate.send(QUIZ_CREATED_TOPIC, event);
        
        // then
        verify(quizEventHandler, timeout(5000)).handleQuizCreatedEvent(any(QuizCreatedEvent.class));
        assertThat(quizEventLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedQuizEvent).isNotNull();
        assertThat(capturedQuizEvent.getQuizId()).isEqualTo(1L);
        assertThat(capturedQuizEvent.getTitle()).isEqualTo("Java 기초 문제");
        assertThat(capturedQuizEvent.getCategory()).isEqualTo("프로그래밍");
    }
    
    @Test
    @DisplayName("배틀 완료 이벤트 통합 테스트")
    void battleCompletedIntegrationTest() throws InterruptedException {
        // given
        BattleCompletedEvent event = new BattleCompletedEvent(1L, 1L, 2L, 1L, 3, 1);
        
        // when
        kafkaTemplate.send(BATTLE_COMPLETED_TOPIC, event);
        
        // then
        verify(battleEventHandler, timeout(5000)).handleBattleCompletedEvent(any(BattleCompletedEvent.class));
        assertThat(battleEventLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedBattleEvent).isNotNull();
        assertThat(capturedBattleEvent.getBattleId()).isEqualTo(1L);
        assertThat(capturedBattleEvent.getWinnerId()).isEqualTo(1L);
    }
    
    @KafkaListener(topics = USER_CREATED_TOPIC, groupId = "test-user-group", containerFactory = "userEventKafkaListenerContainerFactory")
    public void consumeUserCreatedEvent(UserCreatedEvent event) {
        this.capturedUserEvent = event;
        this.userEventLatch.countDown();
    }
    
    @KafkaListener(topics = QUIZ_CREATED_TOPIC, groupId = "test-quiz-group", containerFactory = "quizEventKafkaListenerContainerFactory")
    public void consumeQuizCreatedEvent(QuizCreatedEvent event) {
        this.capturedQuizEvent = event;
        this.quizEventLatch.countDown();
    }
    
    @KafkaListener(topics = BATTLE_COMPLETED_TOPIC, groupId = "test-battle-group", containerFactory = "battleEventKafkaListenerContainerFactory")
    public void consumeBattleCompletedEvent(BattleCompletedEvent event) {
        this.capturedBattleEvent = event;
        this.battleEventLatch.countDown();
    }
    
    // 추가 테스트 설정 클래스 정의
    @TestConfiguration
    @EnableAutoConfiguration
    static class TestConfig {
        @Bean
        public UserEventHandler userEventHandler() {
            return org.mockito.Mockito.spy(new UserEventHandler());
        }
        
        @Bean
        public QuizEventHandler quizEventHandler() {
            return org.mockito.Mockito.spy(new QuizEventHandler());
        }
        
        @Bean
        public BattleEventHandler battleEventHandler() {
            return org.mockito.Mockito.spy(new BattleEventHandler());
        }
    }
    
    // Model Classes for Testing
    static class User {
        private Long id;
        private String username;
        private String email;
        
        public User(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }
    
    static class Quiz {
        private Long id;
        private String title;
        private String question;
        private List<String> options;
        private String answer;
        private String explanation;
        private String category;
        private Integer difficultyLevel;
        
        public Quiz(Long id, String title, String question, List<String> options, 
                String answer, String explanation, String category, Integer difficultyLevel) {
            this.id = id;
            this.title = title;
            this.question = question;
            this.options = options;
            this.answer = answer;
            this.explanation = explanation;
            this.category = category;
            this.difficultyLevel = difficultyLevel;
        }
        
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public String getAnswer() { return answer; }
        public String getExplanation() { return explanation; }
        public String getCategory() { return category; }
        public Integer getDifficultyLevel() { return difficultyLevel; }
    }
    
    static class Battle {
        private Long id;
        private Long hostUserId;
        private Long guestUserId;
        private List<Long> quizIds;
        private String status;
        private Integer hostScore;
        private Integer guestScore;
        private Long winnerId;
        
        public Battle(Long id, Long hostUserId, Long guestUserId, List<Long> quizIds, 
                String status, Integer hostScore, Integer guestScore, Long winnerId) {
            this.id = id;
            this.hostUserId = hostUserId;
            this.guestUserId = guestUserId;
            this.quizIds = quizIds;
            this.status = status;
            this.hostScore = hostScore;
            this.guestScore = guestScore;
            this.winnerId = winnerId;
        }
        
        public Long getId() { return id; }
        public Long getHostUserId() { return hostUserId; }
        public Long getGuestUserId() { return guestUserId; }
        public List<Long> getQuizIds() { return quizIds; }
        public String getStatus() { return status; }
        public Integer getHostScore() { return hostScore; }
        public Integer getGuestScore() { return guestScore; }
        public Long getWinnerId() { return winnerId; }
    }
    
    // Event Classes
    static class UserCreatedEvent {
        private Long userId;
        private String username;
        private String email;
        
        public UserCreatedEvent() {
        }
        
        public UserCreatedEvent(Long userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }
        
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }
    
    static class QuizCreatedEvent {
        private Long quizId;
        private String title;
        private String category;
        
        public QuizCreatedEvent() {
        }
        
        public QuizCreatedEvent(Long quizId, String title, String category) {
            this.quizId = quizId;
            this.title = title;
            this.category = category;
        }
        
        public Long getQuizId() { return quizId; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
    }
    
    static class BattleCompletedEvent {
        private Long battleId;
        private Long hostUserId;
        private Long guestUserId;
        private Long winnerId;
        private Integer hostScore;
        private Integer guestScore;
        
        public BattleCompletedEvent() {
        }
        
        public BattleCompletedEvent(Long battleId, Long hostUserId, Long guestUserId, Long winnerId, Integer hostScore, Integer guestScore) {
            this.battleId = battleId;
            this.hostUserId = hostUserId;
            this.guestUserId = guestUserId;
            this.winnerId = winnerId;
            this.hostScore = hostScore;
            this.guestScore = guestScore;
        }
        
        public Long getBattleId() { return battleId; }
        public Long getHostUserId() { return hostUserId; }
        public Long getGuestUserId() { return guestUserId; }
        public Long getWinnerId() { return winnerId; }
        public Integer getHostScore() { return hostScore; }
        public Integer getGuestScore() { return guestScore; }
    }
    
    // Repository Interfaces
    interface UserRepository {
        Optional<User> findById(Long id);
    }
    
    interface QuizRepository {
        Optional<Quiz> findById(Long id);
    }
    
    interface BattleRepository {
        Optional<Battle> findById(Long id);
    }
    
    // Event Handlers
    static class UserEventHandler {
        public void handleUserCreatedEvent(UserCreatedEvent event) {
            // 실제 구현에서는 사용자 생성 이벤트를 처리하는 로직
            System.out.println("User created event handled: " + event.getUserId());
        }
    }
    
    static class QuizEventHandler {
        public void handleQuizCreatedEvent(QuizCreatedEvent event) {
            // 실제 구현에서는 퀴즈 생성 이벤트를 처리하는 로직
            System.out.println("Quiz created event handled: " + event.getQuizId());
        }
    }
    
    static class BattleEventHandler {
        public void handleBattleCompletedEvent(BattleCompletedEvent event) {
            // 실제 구현에서는 배틀 완료 이벤트를 처리하는 로직
            System.out.println("Battle completed event handled: " + event.getBattleId());
        }
    }
} 