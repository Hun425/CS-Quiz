package com.quizplatform.core.controller.advice;

import com.quizplatform.core.controller.interceptor.CacheStatusInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 응답 본문 작성 전에 캐시 상태 헤더를 추가하는 Advice
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class CacheStatusResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final String CACHE_STATUS_HEADER = "X-Cache-Status";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 응답 타입에 대해 적용 (필요시 특정 컨트롤러나 반환 타입으로 제한 가능)
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // Servlet 기반 응답인지 확인 (일반적인 경우)
        if (response instanceof ServletServerHttpResponse servletResponse) {
            // 응답이 이미 커밋되었는지 확인
            if (servletResponse.getServletResponse().isCommitted()) {
                log.warn("ResponseBodyAdvice: 응답이 이미 커밋되어 캐시 상태 헤더를 설정할 수 없습니다: {}", request.getURI());
                return body; // 이미 커밋되었으면 아무 작업도 하지 않음
            }

            try {
                String requestPath = request.getURI().getPath();

                // CacheStatusInterceptor의 ThreadLocal 값 접근 (static 메서드 사용)
                Boolean skipCache = CacheStatusInterceptor.shouldSkipCache(); // 캐시 우회 여부
                Boolean isHit = CacheStatusInterceptor.getCurrentCacheHitStatus(); // 현재 캐시 히트 상태

                // 캐시 상태 결정 로직 (Interceptor와 유사하게)
                String cacheStatus = "BYPASS"; // 기본값
                boolean isCacheablePath = CacheStatusInterceptor.isCacheablePath(requestPath); // 캐시 대상 경로인지 확인

                if (skipCache != null && skipCache) {
                    cacheStatus = "SKIP";
                    log.debug("ResponseBodyAdvice: 캐시 우회 요청이므로 SKIP 상태 설정: {}", requestPath);
                } else if (isCacheablePath) {
                    // isHit가 null일 수 있음 (캐시 조회가 없었던 경우 등) -> 명시적 false 비교 대신 null 허용
                    cacheStatus = (isHit != null && isHit) ? "HIT" : "MISS";
                    log.debug("ResponseBodyAdvice: 캐시 상태 결정: 경로={}, 캐시상태={}, ThreadLocal히트={}",
                             requestPath, cacheStatus, isHit);
                } else {
                     log.debug("ResponseBodyAdvice: 매핑된 URL 패턴 없음, BYPASS 상태 설정: {}", requestPath);
                }

                // 응답 헤더에 캐시 상태 추가
                response.getHeaders().add(CACHE_STATUS_HEADER, cacheStatus.toUpperCase());
                response.getHeaders().add("x-cache-status", cacheStatus.toUpperCase());
                response.getHeaders().add("X-Cache", cacheStatus.toUpperCase());
                response.getHeaders().add("x-cache", cacheStatus.toUpperCase());

                log.debug("ResponseBodyAdvice: 캐시 상태 헤더 설정 완료: {}={}", CACHE_STATUS_HEADER, cacheStatus);

            } catch (Exception e) {
                log.error("ResponseBodyAdvice에서 헤더 설정 중 오류 발생", e);
                // 예외 발생 시에도 본문은 그대로 반환
            }
        } else {
             log.warn("ResponseBodyAdvice: ServletServerHttpResponse 타입이 아니므로 헤더를 설정할 수 없습니다. Response type: {}", response.getClass().getName());
        }

        // 수정 없이 원래 본문 반환
        return body;
    }
} 