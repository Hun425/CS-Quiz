package com.quizplatform.common.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.common.event.ModuleIntegrationTest.BattleCompletedEvent;
import com.quizplatform.common.event.ModuleIntegrationTest.BattleEventHandler;
import com.quizplatform.common.event.ModuleIntegrationTest.BattleRepository;
import com.quizplatform.common.event.ModuleIntegrationTest.QuizCreatedEvent;
import com.quizplatform.common.event.ModuleIntegrationTest.QuizEventHandler;
import com.quizplatform.common.event.ModuleIntegrationTest.QuizRepository;
import com.quizplatform.common.event.ModuleIntegrationTest.UserCreatedEvent;
import com.quizplatform.common.event.ModuleIntegrationTest.UserEventHandler;
import com.quizplatform.common.event.ModuleIntegrationTest.UserRepository;

@TestConfiguration
@EnableKafka
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.quizplatform.common.event")
public class ModuleIntegrationTestConfig {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
    
    // Event Handlers
    @Bean
    public UserEventHandler userEventHandler() {
        return new UserEventHandler();
    }
    
    @Bean
    public QuizEventHandler quizEventHandler() {
        return new QuizEventHandler();
    }
    
    @Bean
    public BattleEventHandler battleEventHandler() {
        return new BattleEventHandler();
    }
    
    // Repository 모의 객체
    @Bean
    public UserRepository userRepository() {
        return new MockUserRepository();
    }
    
    @Bean
    public QuizRepository quizRepository() {
        return new MockQuizRepository();
    }
    
    @Bean
    public BattleRepository battleRepository() {
        return new MockBattleRepository();
    }
    
    // 특정 이벤트 타입을 위한 리스너 팩토리
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> userEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-user-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        DefaultKafkaConsumerFactory<String, UserCreatedEvent> cf = 
                new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new JsonDeserializer<>(UserCreatedEvent.class)
                );
        
        factory.setConsumerFactory(cf);
        return factory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, QuizCreatedEvent> quizEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, QuizCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-quiz-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        DefaultKafkaConsumerFactory<String, QuizCreatedEvent> cf = 
                new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new JsonDeserializer<>(QuizCreatedEvent.class)
                );
        
        factory.setConsumerFactory(cf);
        return factory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BattleCompletedEvent> battleEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BattleCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-battle-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        DefaultKafkaConsumerFactory<String, BattleCompletedEvent> cf = 
                new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new JsonDeserializer<>(BattleCompletedEvent.class)
                );
        
        factory.setConsumerFactory(cf);
        return factory;
    }
    
    // 모의 저장소 클래스들
    static class MockUserRepository implements UserRepository {
        @Override
        public java.util.Optional<ModuleIntegrationTest.User> findById(Long id) {
            if (id == 1L) {
                return java.util.Optional.of(new ModuleIntegrationTest.User(1L, "testuser", "test@example.com"));
            } else if (id == 2L) {
                return java.util.Optional.of(new ModuleIntegrationTest.User(2L, "guestuser", "guest@example.com"));
            }
            return java.util.Optional.empty();
        }
    }
    
    static class MockQuizRepository implements QuizRepository {
        @Override
        public java.util.Optional<ModuleIntegrationTest.Quiz> findById(Long id) {
            if (id == 1L) {
                return java.util.Optional.of(new ModuleIntegrationTest.Quiz(1L, "Java 기초 문제", "자바의 기본 자료형이 아닌 것은?", 
                    java.util.Arrays.asList("int", "boolean", "char", "String"), 
                    "String", "String은 참조형 자료형입니다.", "프로그래밍", 2));
            }
            return java.util.Optional.empty();
        }
    }
    
    static class MockBattleRepository implements BattleRepository {
        @Override
        public java.util.Optional<ModuleIntegrationTest.Battle> findById(Long id) {
            if (id == 1L) {
                return java.util.Optional.of(new ModuleIntegrationTest.Battle(1L, 1L, 2L, 
                    java.util.Arrays.asList(1L, 2L, 3L), "COMPLETED", 3, 1, 1L));
            }
            return java.util.Optional.empty();
        }
    }
} 