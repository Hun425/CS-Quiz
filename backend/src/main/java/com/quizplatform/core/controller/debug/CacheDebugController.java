package com.quizplatform.core.controller.debug;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 캐시 디버깅을 위한 컨트롤러
 * 
 * <p>캐시 상태 및 통계를 확인할 수 있는 API를 제공합니다.</p>
 * <p>개발 및 테스트 환경에서만 활성화해야 합니다.</p>
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class CacheDebugController {

    private final CacheManager cacheManager;
    
    /**
     * 모든 캐시의 상태 및 통계를 반환합니다.
     * 
     * @return 캐시별 통계 정보
     */
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 모든 캐시에 대한 통계 수집
        Collection<String> cacheNames = ((SimpleCacheManager) cacheManager).getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                        (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
                
                CacheStats caffeineStats = nativeCache.stats();
                Map<String, Object> cacheStats = new HashMap<>();
                cacheStats.put("hitCount", caffeineStats.hitCount());
                cacheStats.put("missCount", caffeineStats.missCount());
                cacheStats.put("hitRate", caffeineStats.hitRate());
                cacheStats.put("evictionCount", caffeineStats.evictionCount());
                cacheStats.put("estimatedSize", nativeCache.estimatedSize());
                
                stats.put(cacheName, cacheStats);
            }
        }
        
        return stats;
    }
    
    /**
     * 특정 캐시의 모든 항목을 제거합니다.
     * 
     * @return 작업 결과
     */
    @GetMapping("/cache/clear-all")
    public Map<String, String> clearAllCaches() {
        Collection<String> cacheNames = ((SimpleCacheManager) cacheManager).getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("캐시 비움: {}", cacheName);
            }
        }
        
        return Map.of("status", "success", "message", "모든 캐시가 초기화되었습니다.");
    }
}
