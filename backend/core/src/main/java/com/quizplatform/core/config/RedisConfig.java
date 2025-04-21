package com.quizplatform.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 데이터베이스 설정 클래스
 * 
 * <p>캐싱 기능과 Redis 데이터 저장소 연결을 위한 설정을 담당합니다.
 * Redis 연결, 직렬화, 캐시 TTL(Time-To-Live) 등을 구성합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Redis 연결 팩토리 빈
     * 
     * <p>Redis 서버 연결을 위한 설정을 제공합니다.</p>
     * 
     * @return Redis 연결 팩토리
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis 템플릿 빈
     * 
     * <p>Redis 데이터 저장소 사용을 위한 템플릿을 구성합니다.
     * 키와 값의 직렬화 방식, 트랜잭션 지원 등을 설정합니다.</p>
     * 
     * @return Redis 템플릿
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // 문자열 키를 위한 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        // JSON 형식으로 값을 직렬화
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Hash 작업을 위한 직렬화 설정
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setEnableDefaultSerializer(false);
        template.setEnableTransactionSupport(true);

        return template;
    }

    /**
     * Redis 캐시 매니저 빈
     * 
     * <p>Spring의 캐싱 추상화에 사용할 Redis 캐시 관리자를 구성합니다.
     * 각 캐시별 TTL 설정 및 기본 캐시 속성을 정의합니다.</p>
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return Redis 캐시 매니저
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 각 캐시의 TTL 설정
        cacheConfigurations.put("userProfiles", createCacheConfiguration(Duration.ofHours(1)));
        cacheConfigurations.put("userStatistics", createCacheConfiguration(Duration.ofMinutes(30)));
        cacheConfigurations.put("userAchievements", createCacheConfiguration(Duration.ofHours(1)));
        cacheConfigurations.put("userTopicPerformance", createCacheConfiguration(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(createCacheConfiguration(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Redis 캐시 설정 생성 메서드
     * 
     * <p>지정된 TTL을 가진 캐시 설정을 생성합니다.
     * 키와 값에 대한 직렬화 방식도 설정합니다.</p>
     * 
     * @param ttl 캐시 항목의 생존 시간
     * @return Redis 캐시 설정
     */
    private RedisCacheConfiguration createCacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}