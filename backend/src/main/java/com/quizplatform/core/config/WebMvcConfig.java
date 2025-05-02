package com.quizplatform.core.config;

import com.quizplatform.core.controller.interceptor.CacheControlInterceptor;
import com.quizplatform.core.controller.interceptor.CacheStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CacheStatusInterceptor cacheStatusInterceptor;
    private final CacheControlInterceptor cacheControlInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CacheControlInterceptor 먼저 등록 (우선 순위: 1)
        registry.addInterceptor(cacheControlInterceptor)
                .addPathPatterns("/api/**")
                .order(1);

        // CacheStatusInterceptor 등록 (우선 순위: 2)
        registry.addInterceptor(cacheStatusInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }
}