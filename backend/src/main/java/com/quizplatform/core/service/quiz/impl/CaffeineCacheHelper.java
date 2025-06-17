package com.quizplatform.core.service.quiz.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Caffeine 캐시 작업 지원 유틸리티 클래스
 * 
 * <p>캐시를 안전하게 사용하고 예외 처리를 위한 헬퍼 메서드를 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaffeineCacheHelper {
    
    private final CacheManager cacheManager;
    
    /**
     * 캐시에서 값을 가져오거나, 없는 경우 supplier를 통해 값을 생성하고 캐시에 저장합니다.
     * 
     * @param <T> 반환 타입
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     * @param valueSupplier 값 생성 Supplier
     * @return 캐시된 값 또는 새로 생성된 값
     */
    public <T> T getOrCompute(String cacheName, Object key, Supplier<T> valueSupplier) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                T cachedValue = cache.get(key, () -> valueSupplier.get());
                if (cachedValue != null) {
                    log.debug("캐시 히트: 캐시명={}, 키={}", cacheName, key);
                    return cachedValue;
                }
            }
            
            log.debug("캐시 미스: 캐시명={}, 키={}", cacheName, key);
            return valueSupplier.get();
        } catch (Exception e) {
            log.error("캐시 접근 중 오류 발생: 캐시명={}, 키={}, 오류={}", cacheName, key, e.getMessage(), e);
            return valueSupplier.get();
        }
    }
    
    /**
     * 캐시에 값을 저장합니다.
     * 
     * @param <T> 저장할 값 타입
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     * @param value 저장할 값
     */
    public <T> void putInCache(String cacheName, Object key, T value) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                log.debug("캐시에 저장 완료: 캐시명={}, 키={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("캐시 저장 중 오류 발생: 캐시명={}, 키={}, 오류={}", cacheName, key, e.getMessage(), e);
        }
    }
    
    /**
     * 캐시에서 값을 삭제합니다.
     * 
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     */
    public void evictFromCache(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("캐시에서 삭제 완료: 캐시명={}, 키={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("캐시 삭제 중 오류 발생: 캐시명={}, 키={}, 오류={}", cacheName, key, e.getMessage(), e);
        }
    }
    
    /**
     * 특정 캐시의 모든 항목을 삭제합니다.
     * 
     * @param cacheName 캐시 이름
     */
    public void clearCache(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.debug("캐시 전체 삭제 완료: 캐시명={}", cacheName);
            }
        } catch (Exception e) {
            log.error("캐시 전체 삭제 중 오류 발생: 캐시명={}, 오류={}", cacheName, e.getMessage(), e);
        }
    }
} 