package com.quizplatform.core.controller.debug;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 헤더 및 응답 형식 테스트를 위한 디버깅 컨트롤러
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class HeaderTestController {

    /**
     * Content-Type 헤더 테스트
     * 
     * @return 응답 맵
     */
    @GetMapping("/headers/content-type-test")
    public Map<String, Object> testContentTypeHeader() {
        log.info("Content-Type 헤더 테스트 API 호출됨");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Content-Type 헤더 테스트입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 캐시 헤더 테스트
     * 
     * @return 응답 맵
     */
    @GetMapping("/headers/cache-test")
    public Map<String, Object> testCacheHeader() {
        log.info("캐시 헤더 테스트 API 호출됨");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "X-Cache-Status 헤더 테스트입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}
