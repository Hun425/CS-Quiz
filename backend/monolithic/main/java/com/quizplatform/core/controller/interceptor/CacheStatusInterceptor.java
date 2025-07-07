package com.quizplatform.core.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 캐시 상태 인터셉터
 * 
 * <p>응답 헤더에 캐시 상태 정보를 추가합니다.
 * k6 테스트 스크립트 및 캐시 성능 분석을 위한 기능입니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheStatusInterceptor implements HandlerInterceptor {
    
    // 요청 경로별 캐시 이름 매핑
    private static final ConcurrentMap<String, String> PATH_CACHE_MAPPING = new ConcurrentHashMap<>();
    
    static {
        // 퀴즈 관련 캐시 매핑 - 정규식 패턴 사용
        PATH_CACHE_MAPPING.put("/api/quizzes/\\d+$", "quizDetails");
        PATH_CACHE_MAPPING.put("/api/quizzes/\\d+/questions", "quizzes");
        PATH_CACHE_MAPPING.put("/api/recommendations/popular", "popularQuizzes");
        PATH_CACHE_MAPPING.put("/api/quizzes/daily", "dailyQuiz");
        PATH_CACHE_MAPPING.put("/api/quizzes/search", "quizSearch");
        PATH_CACHE_MAPPING.put("/api/quizzes/tag/\\d+", "quizSearch");
        PATH_CACHE_MAPPING.put("/api/quizzes/\\d+/statistics", "quizStatistics");
        
        // 사용자 관련 캐시 매핑
        PATH_CACHE_MAPPING.put("/api/users/\\d+/profile", "userProfiles");
        PATH_CACHE_MAPPING.put("/api/users/\\d+/statistics", "userStatistics");
        PATH_CACHE_MAPPING.put("/api/users/\\d+/achievements", "userAchievements");
    }
    
    // 캐시 상태를 나타내는 헤더 이름
    private static final String CACHE_STATUS_HEADER = "X-Cache-Status";
    
    // 캐시 우회 헤더 이름
    private static final String NO_CACHE_HEADER = "X-No-Cache";
    
    // 쓰레드로컬 변수로 요청별 캐시 히트 여부 저장
    private static final ThreadLocal<Boolean> cacheHitThreadLocal = new ThreadLocal<>();
    
    // 쓰레드로컬 변수로 캐시 우회 요청 여부 저장
    private static final ThreadLocal<Boolean> skipCacheThreadLocal = new ThreadLocal<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 캐시 히트 여부 초기화 (명시적으로 false로 설정)
        cacheHitThreadLocal.set(false);
        
        // 캐시 우회 헤더 확인
        boolean skipCache = "true".equalsIgnoreCase(request.getHeader(NO_CACHE_HEADER))
                || "no-cache".equalsIgnoreCase(request.getHeader("Cache-Control"));
        
        skipCacheThreadLocal.set(skipCache);
        
        if (skipCache) {
            log.debug("캐시 우회 요청 감지: {}", request.getRequestURI());
        }
        
        // 요청 경로 기록 및 패턴 매칭 검증
        String requestPath = request.getRequestURI();
        boolean patternMatched = false;
        
        // URL 패턴 검증 로깅
        for (String pathPattern : PATH_CACHE_MAPPING.keySet()) {
            if (requestPath.matches(pathPattern)) {
                patternMatched = true;
                String cacheName = PATH_CACHE_MAPPING.get(pathPattern);
                log.debug("URL 패턴 매칭 성공: 요청={}, 패턴={}, 캐시={}", requestPath, pathPattern, cacheName);
                break;
            }
        }
        
        if (!patternMatched) {
            log.debug("URL 패턴 매칭 실패: 요청={}, 캐시 사용 안함", requestPath);
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // postHandle에서는 더 이상 헤더를 설정하지 않음
        // 응답이 커밋되기 전에 헤더를 설정해야 하므로 ResponseBodyAdvice에서 처리
        log.trace("CacheStatusInterceptor postHandle 실행됨 (헤더 설정은 ResponseBodyAdvice에서 처리): {}", request.getRequestURI());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // afterCompletion에서는 최종적으로 ThreadLocal 변수만 정리
        try {
            // 필요한 경우 예외 로깅 등 추가 작업
            if (ex != null) {
                 log.error("요청 처리 중 예외 발생: {}", request.getRequestURI(), ex);
            }
        } finally {
            // 쓰레드로컬 변수 정리
            cleanUpThreadLocals();
             log.debug("ThreadLocal 변수 정리 완료 (afterCompletion): {}", request.getRequestURI());
        }
    }
    
    /**
     * 모든 쓰레드로컬 변수를 정리합니다.
     * 요청 처리 완료 후 반드시 호출되어야 합니다.
     */
    public static void cleanUpThreadLocals() {
        cacheHitThreadLocal.remove();
        skipCacheThreadLocal.remove();
        log.debug("ThreadLocal 정리됨: cacheHit={}, skipCache={}", cacheHitThreadLocal.get(), skipCacheThreadLocal.get()); // 확인용 로그
    }
    
    /**
     * 캐시 히트 여부를 설정합니다.
     * 캐시 관련 이벤트 핸들러에서 호출됩니다.
     * 
     * @param isHit 캐시 히트 여부
     */
    public static void setCacheHit(boolean isHit) {
        log.debug("캐시 히트 상태 설정: {}", isHit);
        cacheHitThreadLocal.set(isHit);
    }
    
    /**
     * 현재 요청이 캐시를 우회해야 하는지 확인합니다.
     * 
     * @return 캐시 우회 여부
     */
    public static boolean shouldSkipCache() {
        Boolean skipCache = skipCacheThreadLocal.get();
        // 기본값 false 처리 (null일 경우 false)
        boolean shouldSkip = skipCache != null && skipCache;
        log.trace("shouldSkipCache() 호출됨: ThreadLocal={}, 결과={}", skipCache, shouldSkip);
        return shouldSkip;
    }

    /**
     * 현재 요청의 캐시 히트 상태를 반환합니다.
     * ResponseBodyAdvice에서 사용됩니다.
     *
     * @return 캐시 히트 여부 (HIT: true, MISS/BYPASS/SKIP: false 또는 null)
     */
    public static Boolean getCurrentCacheHitStatus() {
        Boolean isHit = cacheHitThreadLocal.get();
        log.trace("getCurrentCacheHitStatus() 호출됨: ThreadLocal={}", isHit);
        return isHit; // null일 수 있음
    }

    /**
     * 주어진 경로가 캐시 대상 경로 패턴과 일치하는지 확인합니다.
     * ResponseBodyAdvice에서 사용됩니다.
     *
     * @param requestPath 확인할 요청 경로
     * @return 캐시 대상 경로 여부
     */
    public static boolean isCacheablePath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        for (String pathPattern : PATH_CACHE_MAPPING.keySet()) {
            if (requestPath.matches(pathPattern)) {
                log.trace("isCacheablePath() 결과: 경로={}, 패턴={}, 결과=true", requestPath, pathPattern);
                return true;
            }
        }
        log.trace("isCacheablePath() 결과: 경로={}, 결과=false", requestPath);
        return false;
    }
} 