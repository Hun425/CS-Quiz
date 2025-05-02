package com.quizplatform.core.filter;

import com.quizplatform.core.controller.interceptor.CacheStatusInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ThreadLocal 변수 정리 필터
 * 
 * <p>요청 처리 완료 후 ThreadLocal 변수를 정리하여 메모리 누수를 방지합니다.</p>
 * <p>인터셉터에서 처리되지 않는 예외 상황에서도 ThreadLocal 변수를 정리합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ThreadLocalCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 필터 체인 실행
            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리 완료 후 항상 ThreadLocal 변수 정리
            CacheStatusInterceptor.cleanUpThreadLocals();
            log.debug("ThreadLocal 변수 정리 완료");
        }
    }
} 