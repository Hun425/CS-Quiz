package com.quizplatform.core.config.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 테스트 데이터 초기화 전체 흐름을 관리하는 서비스
 * 
 * 주요 기능:
 * - 초기화 프로세스 조율
 * - 설정 기반 SQL 파일 관리
 * - 오류 처리 및 복구
 * 
 * @author 채기훈
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "quiz.data-initialization")
public class TestDataInitializationService {

    private final SqlFileExecutor sqlFileExecutor;
    private final TestDataValidator testDataValidator;
    private final ExampleIdProvider exampleIdProvider;

    // 설정 가능한 SQL 파일 목록 (application.yml에서 오버라이드 가능)
    private List<String> sqlFiles = Arrays.asList(
            "classpath:dummy/new.sql",
            "classpath:dummy/new2.sql", 
            "classpath:dummy/new3.sql",
            "classpath:dummy/new4.sql",
            "classpath:dummy/new5.sql",
            "classpath:dummy/new6.sql",
            "classpath:dummy/new7.sql",
            "classpath:dummy/new8.sql"
    );

    private boolean enabled = true;
    private String simpleTestFile = "classpath:test-simple.sql";

    /**
     * 전체 테스트 데이터 초기화 프로세스를 실행합니다.
     */
    public void initializeTestData() {
        if (!enabled) {
            log.info("테스트 데이터 초기화가 비활성화되었습니다.");
            return;
        }

        try {
            log.info("=== 테스트 데이터 초기화 시작 ===");
            
            // 1. 기존 데이터 확인
            if (testDataValidator.hasTestData()) {
                log.info("테스트 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
                setExampleIds();
                return;
            }

            // 2. 초기화 실행
            executeDataInitialization();
            
            // 3. 예제 ID 설정
            setExampleIds();
            
            log.info("=== 테스트 데이터 초기화 완료 ===");
            
        } catch (Exception e) {
            log.error("테스트 데이터 초기화 중 오류 발생: " + e.getMessage(), e);
            throw new RuntimeException("테스트 데이터 초기화 실패", e);
        }
    }

    /**
     * 데이터 초기화를 실행합니다.
     */
    private void executeDataInitialization() {
        log.info("테스트 데이터가 없습니다. 초기화를 시작합니다.");
        
        try {
            // 간단한 테스트 먼저 실행
            if (simpleTestFile != null) {
                log.info("간단한 테스트 SQL을 실행합니다: {}", simpleTestFile);
                sqlFileExecutor.executeSqlFileWithTransaction(simpleTestFile);
            }
        } catch (Exception e) {
            log.warn("간단한 테스트 실행 실패, 전체 SQL 파일로 진행: {}", e.getMessage());
        }

        // 전체 SQL 파일 실행
        log.info("전체 SQL 파일 실행을 시작합니다. 파일 개수: {}", sqlFiles.size());
        sqlFileExecutor.executeAllSqlFiles(sqlFiles);
        log.info("모든 SQL 파일 실행이 완료되었습니다.");
    }

    /**
     * Swagger 예제용 ID를 설정합니다.
     */
    private void setExampleIds() {
        try {
            exampleIdProvider.setAllExampleIds();
            log.info("Swagger 예제 ID 설정 완료");
        } catch (Exception e) {
            log.warn("예제 ID 설정 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    // Getter/Setter for configuration properties

    public List<String> getSqlFiles() {
        return sqlFiles;
    }

    public void setSqlFiles(List<String> sqlFiles) {
        this.sqlFiles = sqlFiles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSimpleTestFile() {
        return simpleTestFile;
    }

    public void setSimpleTestFile(String simpleTestFile) {
        this.simpleTestFile = simpleTestFile;
    }
}