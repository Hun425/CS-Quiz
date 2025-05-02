package com.quizplatform.core.controller.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 모든 RestController 응답에 공통 헤더 적용
 */
@ControllerAdvice
public class GlobalResponseHeaderAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 컨트롤러 응답에 적용
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // 모든 응답에 Content-Type 헤더 추가
        if (!response.getHeaders().containsKey("Content-Type")) {
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            // k6 테스트를 위해 소문자 버전도 추가
            response.getHeaders().add("content-type", "application/json;charset=UTF-8");
        }
        
        return body;
    }
}
