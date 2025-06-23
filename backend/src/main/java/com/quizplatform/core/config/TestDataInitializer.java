package com.quizplatform.core.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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
    public TestDataInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            // 데이터가 이미 존재하는지 확인
            boolean dataExists = checkIfDataExists();

            if (!dataExists) {
                log.info("핵심 데이터가 부족합니다. 더미 데이터를 로드합니다.");
                
                // 방법 1: 통합 SQL 파일 사용 (빠른 로딩)
                // 임시로 주석 처리 - 문제 해결 후 다시 활성화
                // executeSqlFile("classpath:/dummy_data_real.sql");
                
                // 임시 해결책: 분리된 SQL 파일들 사용
                executeSqlFiles();
                
                // 방법 2: 분리된 SQL 파일들 사용 (체계적, 현재 사용중)
                // 이미 위에서 활성화됨
                
                log.info("더미 데이터가 성공적으로 로드되었습니다.");
                
                // 더미 데이터 로드 후에 테스트 계정 생성 (의존성 해결)
                // dummy 폴더 사용 시에는 테스트 계정이 이미 포함되어 있을 수 있음
                try {
                    executeSqlFile("classpath:/dummysql/test_account.sql");
                    log.info("테스트 계정이 성공적으로 생성되었습니다.");
                } catch (Exception e) {
                    log.warn("테스트 계정 생성 생략 (이미 포함되어 있을 수 있음): " + e.getMessage());
                }
            } else {
                log.info("모든 핵심 데이터가 존재합니다. 더미데이터 로딩을 건너뜁니다.");
                
                // 더미 데이터가 있을 때만 테스트 계정 생성 시도 (dummy 폴더 사용 시 생략 가능)
                try {
                    executeSqlFile("classpath:/dummysql/test_account.sql");
                    log.info("테스트 계정이 업데이트되었습니다.");
                } catch (Exception e) {
                    log.warn("테스트 계정 생성 중 오류 발생 (무시): " + e.getMessage());
                }
            }

            // Swagger 예제에 사용할 ID 값 저장
            fetchExampleIds();
        } catch (Exception e) {
            log.error("데이터 초기화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private boolean checkIfDataExists() {
        try {
            // 사용자와 퀴즈 모두 확인 (더미데이터의 핵심)
            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.users", Integer.class);
            Integer quizCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.quizzes", Integer.class);
            Integer tagCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.tags", Integer.class);
            
            boolean hasUsers = userCount != null && userCount > 0;
            boolean hasQuizzes = quizCount != null && quizCount > 0;
            boolean hasTags = tagCount != null && tagCount > 0;
            
            log.info("기존 데이터 확인 - Users: {}, Quizzes: {}, Tags: {}", userCount, quizCount, tagCount);
            
            // 모든 핵심 데이터가 존재해야 'true' 반환
            return hasUsers && hasQuizzes && hasTags;
            
        } catch (Exception e) {
            log.warn("데이터베이스 테이블을 확인할 수 없습니다. 새 데이터를 로드합니다: " + e.getMessage());
            return false;
        }
    }

    private void executeSqlFiles() {
        log.info("분리된 SQL 파일들을 순서대로 실행합니다.");
        
        // 각 SQL 파일을 순서대로 실행
        for (String sqlFilePath : SQL_FILES) {
            try {
                log.info("SQL 파일 실행 중: {}", sqlFilePath);
                executeSqlFile(sqlFilePath);
                log.info("SQL 파일 실행 완료: {}", sqlFilePath);
            } catch (Exception e) {
                log.error("SQL 파일 '{}' 실행 중 오류 발생: {}", sqlFilePath, e.getMessage(), e);
                // 중요하지 않은 파일의 경우 오류를 무시하고 계속 진행
                if (sqlFilePath.contains("cleanup") || sqlFilePath.contains("test_account")) {
                    log.warn("선택적 파일 '{}' 실행 실패, 계속 진행합니다.", sqlFilePath);
                } else {
                    throw new RuntimeException("필수 SQL 파일 실행 중 오류 발생: " + sqlFilePath, e);
                }
            }
        }
    }

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

        // 타임스탬프 문제가 있는 파일들을 처리
        if (resourcePath.contains("daily_quizzes.sql")) {
            sql = fixTimestampCastingIssues(sql);
        }

        // SQL 실행
        jdbcTemplate.execute(sql);
    }

    /**
     * SQL 스크립트에서 타임스탬프 캐스팅 문제를 수정
     */
    private String fixTimestampCastingIssues(String sql) {
        // NOW() + INTERVAL 표현식에 타임스탬프 캐스팅 추가
        sql = sql.replaceAll("NOW\\(\\) \\+ INTERVAL '1 day'",
                "(NOW() + INTERVAL '1 day')::timestamp");

        // NOW() - INTERVAL 표현식에 타임스탬프 캐스팅 추가
        sql = sql.replaceAll("NOW\\(\\) - \\(\\(seq-1\\) \\* INTERVAL '1 day'\\)",
                "(NOW() - ((seq-1) * INTERVAL '1 day'))::timestamp");

        return sql;
    }

    private void fetchExampleIds() {
        // 첫 번째 퀴즈 ID 가져오기
        try {
            EXAMPLE_QUIZ_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.quizzes LIMIT 1", String.class);
            log.info("Example Quiz ID: " + EXAMPLE_QUIZ_ID);
        } catch (Exception e) {
            EXAMPLE_QUIZ_ID = "00000000-0000-0000-0000-000000000000";
            log.error("퀴즈 ID 가져오기 실패: " + e.getMessage());
        }

        // 첫 번째 사용자 ID 가져오기
        try {
            EXAMPLE_USER_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.users LIMIT 1", String.class);
            log.info("Example User ID: " + EXAMPLE_USER_ID);
        } catch (Exception e) {
            EXAMPLE_USER_ID = "00000000-0000-0000-0000-000000000000";
            log.error("사용자 ID 가져오기 실패: " + e.getMessage());
        }

        // 첫 번째 태그 ID 가져오기
        try {
            EXAMPLE_TAG_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.tags LIMIT 1", String.class);
            log.info("Example Tag ID: " + EXAMPLE_TAG_ID);
        } catch (Exception e) {
            EXAMPLE_TAG_ID = "00000000-0000-0000-0000-000000000000";
            log.error("태그 ID 가져오기 실패: " + e.getMessage());
        }
    }
}