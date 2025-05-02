package com.quizplatform.core.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 캐시 제어 인터셉터
 * 
 * <p>HTTP 요청 헤더를 기반으로 캐시 동작을 제어합니다.
 * 성능 테스트를 위한 캐시 건너뛰기 기능을 지원합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
public class CacheControlInterceptor implements HandlerInterceptor {

    /**
     * 캐시 스킵 여부를 요청 속성에 저장하는 상수
     */
    public static final String SKIP_CACHE_ATTRIBUTE = "skipCache";
    
    /**
     * 캐시 스킵을 지시하는 HTTP 헤더 이름
     */
    private static final String SKIP_CACHE_HEADER = "X-Skip-Cache";

    /**
     * 컨트롤러 실행 전에 호출됩니다.
     * X-Skip-Cache 헤더가 존재하고 값이 true인 경우,
     * 요청 속성에 캐시 스킵 플래그를 설정합니다.
     * 
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler 실행될 핸들러
     * @return 항상 true (요청 처리 계속 진행)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String skipCacheHeader = request.getHeader(SKIP_CACHE_HEADER);
        
        if (skipCacheHeader != null && skipCacheHeader.equalsIgnoreCase("true")) {
            request.setAttribute(SKIP_CACHE_ATTRIBUTE, true);
            log.debug("캐시 스킵 헤더 감지: {}", request.getRequestURI());
            
            // 응답 헤더에도 캐시 스킵 상태 표시 (모니터링용)
            response.setHeader(SKIP_CACHE_HEADER, "true");
        }
        
        return true;
    }
} 