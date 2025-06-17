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

    // SQL 파일 실행 순서 정의
    private static final List<String> SQL_FILES = Arrays.asList(
            "classpath:dummysql/cleanup.sql",
            "classpath:dummysql/users.sql",
            "classpath:dummysql/tags.sql",
            "classpath:dummysql/daily_quizzes.sql",
            "classpath:dummysql/topic_quizzes.sql",
            "classpath:dummysql/custom_quizzes.sql",
            "classpath:dummysql/java_quizzes.sql",
            "classpath:dummysql/quiz_tags.sql",
            "classpath:dummysql/js_questions.sql",
            "classpath:dummysql/py_questions.sql",
            "classpath:dummysql/algo_questions.sql",
            "classpath:dummysql/network_questions.sql",
            "classpath:dummysql/web_questions.sql",
            "classpath:dummysql/os_questions.sql",
            "classpath:dummysql/db_questions.sql",
            "classpath:dummysql/security_questions.sql",
            "classpath:dummysql/java_basic_questions.sql"
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
                // 데이터가 없는 경우에만 스크립트 실행
                executeSqlFile("classpath:/duymm_sql.sql");

                // executeSqlFiles();
                log.info("더미 데이터가 성공적으로 로드되었습니다.");
            } else {
                log.info("데이터가 이미 존재합니다. 스크립트 실행을 건너뜁니다.");
            }
            
            // 테스트 계정 생성
            executeSqlFile("classpath:/dummysql/test_account.sql");
            log.info("테스트 계정이 성공적으로 생성되었습니다.");

            // Swagger 예제에 사용할 ID 값 저장
            fetchExampleIds();
        } catch (Exception e) {
            log.error("데이터 초기화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private boolean checkIfDataExists() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public.users", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("사용자 테이블을 확인할 수 없습니다. 새 데이터를 로드합니다: " + e.getMessage());
            return false;
        }
    }

    private void executeSqlFiles() {
        // 각 SQL 파일을 순서대로 실행
        // for (String sqlFilePath : SQL_FILES) {
        //     try {
        //         log.info("SQL 파일 실행 중: {}", sqlFilePath);
        //         executeSqlFile(sqlFilePath);
        //         log.info("SQL 파일 실행 완료: {}", sqlFilePath);
        //     } catch (Exception e) {
        //         log.error("SQL 파일 '{}' 실행 중 오류 발생: {}", sqlFilePath, e.getMessage(), e);
        //         throw new RuntimeException("SQL 파일 실행 중 오류 발생: " + sqlFilePath, e);
        //     }
        // }


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