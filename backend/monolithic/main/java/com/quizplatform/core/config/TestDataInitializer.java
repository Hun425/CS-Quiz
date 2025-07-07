package com.quizplatform.core.config;

import com.quizplatform.core.config.initializer.ExampleIdProvider;
import com.quizplatform.core.config.initializer.TestDataInitializationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 테스트 데이터 초기화의 메인 진입점
 * 
 * 리팩토링된 구조:
 * - SqlFileExecutor: SQL 파일 실행 전담
 * - TestDataValidator: 데이터 검증 전담
 * - ExampleIdProvider: Swagger 예제 ID 제공
 * - TestDataInitializationService: 전체 흐름 관리
 * 
 * <h3>포함되는 데이터:</h3>
 * <ul>
 *   <li>사용자 계정 (관리자, 일반 사용자)</li>
 *   <li>태그 계층구조 (Java, Spring, 알고리즘 등)</li>
 *   <li>퀴즈 (주제별, 데일리, 커스텀)</li>
 *   <li>문제 (다양한 프로그래밍 언어 및 CS 주제)</li>
 *   <li>테스트 계정 (API 테스트용)</li>
 * </ul>
 *
 * @author 채기훈
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local", "prod"})
public class TestDataInitializer {

    // Swagger 예제로 사용할 ID 값들 (하위 호환성 유지)
    public static String EXAMPLE_QUIZ_ID;
    public static String EXAMPLE_USER_ID;
    public static String EXAMPLE_TAG_ID;

    private final TestDataInitializationService initializationService;

    @PostConstruct
    public void init() {
        try {
            // 전체 초기화 프로세스 실행
            initializationService.initializeTestData();
            
            // 기존 정적 변수에 값 복사 (하위 호환성)
            copyExampleIds();
            
        } catch (Exception e) {
            log.error("테스트 데이터 초기화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 코드 호환성을 위해 정적 변수에 값을 복사합니다.
     */
    private void copyExampleIds() {
        EXAMPLE_USER_ID = ExampleIdProvider.EXAMPLE_USER_ID;
        EXAMPLE_QUIZ_ID = ExampleIdProvider.EXAMPLE_QUIZ_ID;
        EXAMPLE_TAG_ID = ExampleIdProvider.EXAMPLE_TAG_ID;
        
        log.info("정적 변수 복사 완료 - User: {}, Quiz: {}, Tag: {}", 
                EXAMPLE_USER_ID, EXAMPLE_QUIZ_ID, EXAMPLE_TAG_ID);
    }
}