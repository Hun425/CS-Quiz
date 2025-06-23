package com.quizplatform.core.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 테스트 데이터 초기설정 클래스
 * 
 * <p>애플리케이션 시작 시 더미 데이터를 자동으로 로드합니다.</p>
 * 
 * <h3>더미 데이터 로딩 방식:</h3>
 * <ul>
 *   <li><strong>방법 1 (기본):</strong> dummy_data_real.sql - 통합 파일로 빠른 로딩</li>
 *   <li><strong>방법 2 (선택):</strong> dummysql/ 폴더의 분리된 파일들 - 체계적 관리</li>
 * </ul>
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
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Component
@Slf4j
@Profile({"dev", "local", "prod"})
public class TestDataInitializer {

    // Swagger 예제로 사용할 ID 값을 정적 변수로 선언
    public static String EXAMPLE_QUIZ_ID;
    public static String EXAMPLE_USER_ID;
    public static String EXAMPLE_TAG_ID;

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final PlatformTransactionManager transactionManager;

    // SQL 파일 실행 순서 정의 (dummy 폴더의 new*.sql 파일들 사용)
    private static final List<String> SQL_FILES = Arrays.asList(
            "classpath:dummy/new.sql",
            "classpath:dummy/new2.sql",
            "classpath:dummy/new3.sql",
            "classpath:dummy/new4.sql",
            "classpath:dummy/new5.sql",
            "classpath:dummy/new6.sql",
            "classpath:dummy/new7.sql",
            "classpath:dummy/new8.sql"
    );

    @Autowired
    public TestDataInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader, 
                              PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.transactionManager = transactionManager;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("=== 테스트 데이터 초기화 시작 ===");
            
            // 테스트 데이터가 있는지 확인
            if (!hasTestData()) {
                log.info("테스트 데이터가 없습니다. 먼저 간단한 테스트 SQL을 실행합니다.");
                
                // 먼저 간단한 테스트 실행
                try {
                    executeSimpleTestWithManualTransaction();
                } catch (Exception e) {
                    log.error("간단한 테스트 실행 실패, 기존 방식으로 진행: {}", e.getMessage(), e);
                    // 기존 방식으로 실행
                    executeAllSqlFiles();
                }
                
                log.info("모든 SQL 파일 실행이 완료되었습니다.");
            } else {
                log.info("테스트 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            }
            
            // Swagger 예제용 ID 설정
            setExampleIds();
            
            log.info("=== 테스트 데이터 초기화 완료 ===");
            
        } catch (Exception e) {
            log.error("테스트 데이터 초기화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 테스트 데이터가 존재하는지 확인
     */
    private boolean hasTestData() {
        try {
            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.users", Integer.class);
            Integer quizCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.quizzes", Integer.class);
            Integer tagCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.tags", Integer.class);
            Integer questionCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.questions", Integer.class);
            
            log.info("현재 데이터 상태:");
            log.info("  - Users: {} 개", userCount);
            log.info("  - Quizzes: {} 개", quizCount);
            log.info("  - Tags: {} 개", tagCount);
            log.info("  - Questions: {} 개", questionCount);
            
            // 모든 테이블에 최소 데이터가 있어야 true 반환
            boolean hasData = userCount != null && userCount > 0 
                    && quizCount != null && quizCount > 0 
                    && tagCount != null && tagCount > 0
                    && questionCount != null && questionCount > 0;
            
            log.info("테스트 데이터 존재 여부: {}", hasData);
            return hasData;
            
        } catch (Exception e) {
            log.warn("데이터 확인 중 오류 발생 (테이블이 없을 수 있음): " + e.getMessage());
            return false;
        }
    }

    /**
     * 모든 SQL 파일을 순차적으로 실행
     */
    public void executeAllSqlFiles() {
        for (String sqlFile : SQL_FILES) {
            try {
                log.info("실행 중: {}", sqlFile);
                executeSqlFileWithManualTransaction(sqlFile);
                log.info("완료: {}", sqlFile);
            } catch (Exception e) {
                log.error("SQL 파일 실행 실패: {} - {}", sqlFile, e.getMessage(), e);
                // 오류가 발생해도 다음 파일 계속 실행
            }
        }
    }

    /**
     * 개별 SQL 파일을 수동 트랜잭션으로 실행
     */
    public void executeSqlFileWithManualTransaction(String sqlFile) throws IOException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            executeSqlFile(sqlFile);
            transactionManager.commit(status);
            log.info("트랜잭션 커밋 성공: {}", sqlFile);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("트랜잭션 롤백됨: {} - {}", sqlFile, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 간단한 테스트 SQL을 수동 트랜잭션으로 실행
     */
    public void executeSimpleTestWithManualTransaction() throws IOException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            log.info("간단한 테스트 SQL 수동 트랜잭션 시작");
            executeSqlFile("classpath:test-simple.sql");
            transactionManager.commit(status);
            log.info("간단한 테스트 SQL 트랜잭션 커밋 성공");
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("간단한 테스트 SQL 트랜잭션 롤백됨: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 개별 SQL 파일 실행
     */
    private void executeSqlFile(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);

        if (!resource.exists()) {
            log.warn("SQL 파일을 찾을 수 없습니다: {}", resourcePath);
            return;
        }

        String sql = new String(
                FileCopyUtils.copyToByteArray(resource.getInputStream()),
                StandardCharsets.UTF_8
        );

        // SQL 문장 개수 체크 (대략적으로)
        long insertCount = sql.toLowerCase().split("insert into").length - 1;
        log.info("SQL 파일 크기: {} bytes, INSERT 문 개수: {}", sql.length(), insertCount);

        try {
            // SQL 실행 전 데이터 개수 확인
            int beforeUsers = getTableCount("users");
            int beforeQuizzes = getTableCount("quizzes");
            int beforeTags = getTableCount("tags");
            int beforeQuestions = getTableCount("questions");
            
            log.info("실행 전 데이터 개수 - Users: {}, Quizzes: {}, Tags: {}, Questions: {}", 
                    beforeUsers, beforeQuizzes, beforeTags, beforeQuestions);

            // 전체 SQL을 한 번에 실행 (복잡한 구문 지원)
            log.info("전체 SQL 실행 시작: {}", resourcePath);
            jdbcTemplate.execute(sql);
            log.info("전체 SQL 실행 완료: {}", resourcePath);
            
            // SQL 실행 후 데이터 개수 확인
            int afterUsers = getTableCount("users");
            int afterQuizzes = getTableCount("quizzes");
            int afterTags = getTableCount("tags");
            int afterQuestions = getTableCount("questions");
            
            log.info("실행 후 데이터 개수 - Users: {}, Quizzes: {}, Tags: {}, Questions: {}", 
                    afterUsers, afterQuizzes, afterTags, afterQuestions);
            log.info("증가된 데이터 개수 - Users: +{}, Quizzes: +{}, Tags: +{}, Questions: +{}", 
                    afterUsers - beforeUsers, afterQuizzes - beforeQuizzes, 
                    afterTags - beforeTags, afterQuestions - beforeQuestions);
            
            log.info("SQL 실행 성공: {}", resourcePath);
        } catch (Exception e) {
            log.error("SQL 실행 중 오류: {} - {}", resourcePath, e.getMessage(), e);
            
            // SQL 내용의 일부를 로그로 출력 (디버깅용)
            String sqlPreview = sql.length() > 500 ? sql.substring(0, 500) + "..." : sql;
            log.error("실패한 SQL 내용 (처음 500자): {}", sqlPreview);
            
            throw e;
        }
    }

    /**
     * 테이블의 행 개수를 안전하게 조회
     */
    private int getTableCount(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public." + tableName, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("테이블 {} 개수 조회 실패: {}", tableName, e.getMessage());
            return 0;
        }
    }

    /**
     * Swagger 예제용 ID 설정
     */
    private void setExampleIds() {
        try {
            EXAMPLE_USER_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.users LIMIT 1", String.class);
            log.info("Example User ID: {}", EXAMPLE_USER_ID);
        } catch (Exception e) {
            EXAMPLE_USER_ID = "00000000-0000-0000-0000-000000000000";
            log.warn("사용자 ID 가져오기 실패, 기본값 사용");
        }

        try {
            EXAMPLE_QUIZ_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.quizzes LIMIT 1", String.class);
            log.info("Example Quiz ID: {}", EXAMPLE_QUIZ_ID);
        } catch (Exception e) {
            EXAMPLE_QUIZ_ID = "00000000-0000-0000-0000-000000000000";
            log.warn("퀴즈 ID 가져오기 실패, 기본값 사용");
        }

        try {
            EXAMPLE_TAG_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.tags LIMIT 1", String.class);
            log.info("Example Tag ID: {}", EXAMPLE_TAG_ID);
        } catch (Exception e) {
            EXAMPLE_TAG_ID = "00000000-0000-0000-0000-000000000000";
            log.warn("태그 ID 가져오기 실패, 기본값 사용");
        }
    }
}