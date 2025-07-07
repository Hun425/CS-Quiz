package com.quizplatform.core.config.p6spy;

import com.p6spy.engine.spy.P6SpyOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * P6Spy 설정 클래스
 * SQL 로깅을 위한 P6Spy 구성을 초기화합니다.
 * 개발 및 성능 테스트 환경에서만 활성화됩니다.
 */
@Configuration
@Profile({"dev", "performance-test"}) // 개발 및 성능 테스트 환경에서만 활성화
public class P6SpyConfig {

    /**
     * P6Spy 설정 초기화
     * spy.properties 파일의 설정을 적용하고, 로그 포맷터를 등록합니다.
     */
    @PostConstruct
    public void init() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(CustomLineFormat.class.getName());
    }
}
