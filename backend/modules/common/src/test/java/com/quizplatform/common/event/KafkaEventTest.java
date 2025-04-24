package com.quizplatform.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kafka 이벤트 테스트
 * 현재 설정 문제로 인해 비활성화됨 - 추후 수정 필요
 */
@SpringBootTest(classes = KafkaEventTest.KafkaTestConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"user-created", "quiz-created", "battle-completed"})
@Disabled("Kafka 설정 문제로 임시 비활성화")
class KafkaEventTest {

    private static final String USER_CREATED_TOPIC = "user-created";
    private static final String QUIZ_CREATED_TOPIC = "quiz-created";
    private static final String BATTLE_COMPLETED_TOPIC = "battle-completed";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaMessageListenerContainer<String, UserCreatedEvent> userCreatedContainer;
    private KafkaMessageListenerContainer<String, QuizCreatedEvent> quizCreatedContainer;
    private KafkaMessageListenerContainer<String, BattleCompletedEvent> battleCompletedContainer;

    private final AtomicInteger userCreatedCount = new AtomicInteger(0);
    private final AtomicInteger quizCreatedCount = new AtomicInteger(0);
    private final AtomicInteger battleCompletedCount = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        // 프로듀서 설정
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, Object> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);

        // 컨슈머 설정 - UserCreatedEvent
        setupUserCreatedConsumer();
        
        // 컨슈머 설정 - QuizCreatedEvent
        setupQuizCreatedConsumer();
        
        // 컨슈머 설정 - BattleCompletedEvent
        setupBattleCompletedConsumer();
        
        // 모든 컨슈머 컨테이너 시작
        userCreatedContainer.start();
        quizCreatedContainer.start();
        battleCompletedContainer.start();
        
        // 파티션 할당 대기
        ContainerTestUtils.waitForAssignment(userCreatedContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        ContainerTestUtils.waitForAssignment(quizCreatedContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        ContainerTestUtils.waitForAssignment(battleCompletedContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    private void setupUserCreatedConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("user-consumer-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        DefaultKafkaConsumerFactory<String, UserCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                        new JsonDeserializer<>(UserCreatedEvent.class, objectMapper, false));
        
        ContainerProperties containerProperties = new ContainerProperties(USER_CREATED_TOPIC);
        userCreatedContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        userCreatedContainer.setupMessageListener((MessageListener<String, UserCreatedEvent>) record -> {
            System.out.println("UserCreatedEvent Received: " + record.value());
            userCreatedCount.incrementAndGet();
        });
    }
    
    private void setupQuizCreatedConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("quiz-consumer-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        DefaultKafkaConsumerFactory<String, QuizCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                        new JsonDeserializer<>(QuizCreatedEvent.class, objectMapper, false));
        
        ContainerProperties containerProperties = new ContainerProperties(QUIZ_CREATED_TOPIC);
        quizCreatedContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        quizCreatedContainer.setupMessageListener((MessageListener<String, QuizCreatedEvent>) record -> {
            System.out.println("QuizCreatedEvent Received: " + record.value());
            quizCreatedCount.incrementAndGet();
        });
    }
    
    private void setupBattleCompletedConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("battle-consumer-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        DefaultKafkaConsumerFactory<String, BattleCompletedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                        new JsonDeserializer<>(BattleCompletedEvent.class, objectMapper, false));
        
        ContainerProperties containerProperties = new ContainerProperties(BATTLE_COMPLETED_TOPIC);
        battleCompletedContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        battleCompletedContainer.setupMessageListener((MessageListener<String, BattleCompletedEvent>) record -> {
            System.out.println("BattleCompletedEvent Received: " + record.value());
            battleCompletedCount.incrementAndGet();
        });
    }

    @AfterEach
    void tearDown() {
        userCreatedContainer.stop();
        quizCreatedContainer.stop();
        battleCompletedContainer.stop();
    }

    @Test
    @DisplayName("User 생성 이벤트 발행 및 수신 테스트")
    void publishAndConsumeUserCreatedEventTest() {
        // given
        UserCreatedEvent event = new UserCreatedEvent(1L, "testuser", "test@example.com");
        
        // when
        kafkaTemplate.send(USER_CREATED_TOPIC, event);
        
        // then
        await().atMost(5, TimeUnit.SECONDS).until(() -> userCreatedCount.get() > 0);
        assertThat(userCreatedCount.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Quiz 생성 이벤트 발행 및 수신 테스트")
    void publishAndConsumeQuizCreatedEventTest() {
        // given
        QuizCreatedEvent event = new QuizCreatedEvent(1L, "Java 기초 문제", "프로그래밍");
        
        // when
        kafkaTemplate.send(QUIZ_CREATED_TOPIC, event);
        
        // then
        await().atMost(5, TimeUnit.SECONDS).until(() -> quizCreatedCount.get() > 0);
        assertThat(quizCreatedCount.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Battle 완료 이벤트 발행 및 수신 테스트")
    void publishAndConsumeBattleCompletedEventTest() {
        // given
        BattleCompletedEvent event = new BattleCompletedEvent(1L, 1L, 2L, 1L, 3, 1);
        
        // when
        kafkaTemplate.send(BATTLE_COMPLETED_TOPIC, event);
        
        // then
        await().atMost(5, TimeUnit.SECONDS).until(() -> battleCompletedCount.get() > 0);
        assertThat(battleCompletedCount.get()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("여러 이벤트 동시 발행 및 수신 테스트")
    void publishAndConsumeMultipleEventsTest() {
        // given
        UserCreatedEvent userEvent = new UserCreatedEvent(2L, "newuser", "new@example.com");
        QuizCreatedEvent quizEvent = new QuizCreatedEvent(2L, "Python 기초 문제", "프로그래밍");
        BattleCompletedEvent battleEvent = new BattleCompletedEvent(2L, 3L, 4L, 4L, 2, 3);
        
        // when
        kafkaTemplate.send(USER_CREATED_TOPIC, userEvent);
        kafkaTemplate.send(QUIZ_CREATED_TOPIC, quizEvent);
        kafkaTemplate.send(BATTLE_COMPLETED_TOPIC, battleEvent);
        
        // then
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            userCreatedCount.get() > 0 && 
            quizCreatedCount.get() > 0 && 
            battleCompletedCount.get() > 0
        );
        
        assertThat(userCreatedCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(quizCreatedCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(battleCompletedCount.get()).isGreaterThanOrEqualTo(1);
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
        
        public Long getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getEmail() {
            return email;
        }
        
        @Override
        public String toString() {
            return "UserCreatedEvent{userId=" + userId + ", username='" + username + "', email='" + email + "'}";
        }
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
        
        public Long getQuizId() {
            return quizId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getCategory() {
            return category;
        }
        
        @Override
        public String toString() {
            return "QuizCreatedEvent{quizId=" + quizId + ", title='" + title + "', category='" + category + "'}";
        }
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
        
        public Long getBattleId() {
            return battleId;
        }
        
        public Long getHostUserId() {
            return hostUserId;
        }
        
        public Long getGuestUserId() {
            return guestUserId;
        }
        
        public Long getWinnerId() {
            return winnerId;
        }
        
        public Integer getHostScore() {
            return hostScore;
        }
        
        public Integer getGuestScore() {
            return guestScore;
        }
        
        @Override
        public String toString() {
            return "BattleCompletedEvent{battleId=" + battleId + 
                ", hostUserId=" + hostUserId + 
                ", guestUserId=" + guestUserId + 
                ", winnerId=" + winnerId + 
                ", hostScore=" + hostScore + 
                ", guestScore=" + guestScore + "}";
        }
    }
    
    @TestConfiguration
    @EnableAutoConfiguration
    static class KafkaTestConfig {
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
} 