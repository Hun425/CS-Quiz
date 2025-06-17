package com.quizplatform.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.quizplatform.core.controller.interceptor.CacheStatusInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 로컬 캐시 설정 클래스 - 성능 최적화 버전
 * 
 * <p>Spring의 표준 캐싱 메커니즘을 사용하여 Caffeine 캐시를 설정합니다.</p>
 * <p>application-performance.yml의 설정을 사용합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
@EnableCaching
@Slf4j
public class CaffeineCacheConfig {

    /**
     * Caffeine 캐시 매니저를 생성합니다.
     * 캐시별로 다른 설정(만료 시간, 최대 크기 등)을 적용합니다.
     * 
     * @return 설정된 CacheManager
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<>();
        
        // 자주 사용되는 캐시 (만료 시간 10분, 최대 사이즈 1000)
        caches.add(buildCache("quizDetails", 10, 1000));
        caches.add(buildCache("quizzes", 10, 1000));
        caches.add(buildCache("quizSearch", 10, 1000));
        caches.add(buildCache("popularQuizzes", 10, 1000));
        
        // 덜 자주 사용되는 캐시 (만료 시간 30분, 최대 사이즈 500)
        caches.add(buildCache("quizRecommendations", 30, 500));
        caches.add(buildCache("dailyQuiz", 30, 10));
        caches.add(buildCache("quizStatistics", 30, 500));
        caches.add(buildCache("userProfiles", 30, 500));
        caches.add(buildCache("userStatistics", 30, 500));
        caches.add(buildCache("userAchievements", 30, 300));
        caches.add(buildCache("userTopicPerformance", 30, 300));
        caches.add(buildCache("tags", 60, 200));  // 태그는 더 오래 유지
        
        cacheManager.setCaches(caches);
        log.info("Caffeine 캐시 매니저 구성 완료 - 총 {} 개의 캐시 설정됨", caches.size());
        return cacheManager;
    }
    
    /**
     * 개별 캐시를 생성합니다.
     * 
     * @param name 캐시 이름
     * @param expiryMinutes 캐시 만료 시간(분)
     * @param maxSize 최대 캐시 항목 수
     * @return 설정된 CaffeineCache
     */
    private CaffeineCache buildCache(String name, int expiryMinutes, int maxSize) {
        log.info("캐시 생성: 이름={}, 만료시간={}분, 최대크기={}", name, expiryMinutes, maxSize);
        
        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = 
            Caffeine.newBuilder()
                .recordStats() // 캐시 통계 기록 - 매우 중요
                .expireAfterWrite(expiryMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build();
        
        // 캐시 상태 로깅
        log.info("캐시 초기화 상태: 이름={}, 통계기록={}", name, caffeineCache.stats() != null);
        
        return new CaffeineCache(name, caffeineCache);
    }
    
    /**
     * 캐시 작업 이벤트 핸들러
     * Spring의 캐시 작업(get, put, evict 등)을 가로채서 캐시 히트 여부를 ThreadLocal에 설정합니다.
     */
    @Bean
    public CacheOperationListeners cacheEventListeners() {
        return new CacheOperationListeners();
    }
    
    /**
     * 캐시 이벤트 리스너 클래스
     * Spring의 캐시 작업을 모니터링하고 캐시 히트/미스 정보를 수집합니다.
     */
    @Slf4j
    public static class CacheOperationListeners {
        
        /**
         * 캐시 히트 이벤트를 처리합니다.
         * 
         * @param name 캐시 이름
         * @param key 캐시 키
         */
        public void onCacheHit(String name, Object key) {
            CacheStatusInterceptor.setCacheHit(true);
            log.debug("캐시 히트: 캐시명={}, 키={}", name, key);
        }
        
        /**
         * 캐시 미스 이벤트를 처리합니다.
         * 
         * @param name 캐시 이름
         * @param key 캐시 키
         */
        public void onCacheMiss(String name, Object key) {
            CacheStatusInterceptor.setCacheHit(false);
            log.debug("캐시 미스: 캐시명={}, 키={}", name, key);
        }
    }
    
    /**
     * 캐시 리졸버
     * 캐시 작업 전후에 이벤트를 발생시켜 캐시 상태를 추적합니다.
     */
    @Bean
    @Primary
    public CacheResolver trackedCacheResolver(CacheManager cacheManager, CacheOperationListeners listeners) {
        return new CacheResolver() {
            @Override
            public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
                Collection<String> cacheNames = context.getOperation().getCacheNames();
                Collection<Cache> result = new ArrayList<>(cacheNames.size());
                
                for (String cacheName : cacheNames) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        // 캐시 조회 작업 추적 로직 추가
                        result.add(new TrackedCache(cache, listeners));
                    }
                }
                
                return result;
            }
        };
    }
    
    /**
     * 캐시 작업을 추적하는 데코레이터 캐시 구현체
     */
    private static class TrackedCache implements Cache {
        private final Cache delegate;
        private final CacheOperationListeners listeners;
        
        public TrackedCache(Cache delegate, CacheOperationListeners listeners) {
            this.delegate = delegate;
            this.listeners = listeners;
        }
        
        @Override
        public String getName() {
            return delegate.getName();
        }
        
        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }
        
        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper wrapper = delegate.get(key);
            if (wrapper != null) {
                listeners.onCacheHit(getName(), key);
            } else {
                listeners.onCacheMiss(getName(), key);
            }
            return wrapper;
        }
        
        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            if (value != null) {
                listeners.onCacheHit(getName(), key);
            } else {
                listeners.onCacheMiss(getName(), key);
            }
            return value;
        }
        
        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            // 먼저 캐시에서 값을 조회
            ValueWrapper wrapper = delegate.get(key);
            
            // 캐시 히트인 경우
            if (wrapper != null) {
                listeners.onCacheHit(getName(), key);
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return value;
            }
            
            // 캐시 미스인 경우
            listeners.onCacheMiss(getName(), key);
            
            try {
                // 값을 로드하여 캐시에 저장하고 반환
                T value = valueLoader.call();
                if (value != null) {
                    put(key, value);
                }
                return value;
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Error loading cache value for key: " + key, e);
            }
        }
        
        @Override
        public void put(Object key, Object value) {
            delegate.put(key, value);
        }
        
        @Override
        public void evict(Object key) {
            delegate.evict(key);
        }
        
        @Override
        public void clear() {
            delegate.clear();
        }
    }
}