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

@Component
@Slf4j
@Profile({"dev", "local", "prod"})
public class TestDataInitializer {

    // Swagger 예제로 사용할 Long 값을 정적 변수로 선언
    public static String EXAMPLE_QUIZ_ID;
    public static String EXAMPLE_USER_ID;
    public static String EXAMPLE_TAG_ID;

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public TestDataInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws IOException {
        // 데이터가 이미 존재하는지 확인
        boolean dataExists = checkIfDataExists();

        if (!dataExists) {
            // 데이터가 없는 경우에만 스크립트 실행
            executeSqlScript("classpath:dummy_data_real.sql");
            log.info("더미 데이터가 성공적으로 로드되었습니다.");
        } else {
            log.info("데이터가 이미 존재합니다. 스크립트 실행을 건너뜁니다.");
        }

        // 3. Swagger 예제에 사용할 Long 값 저장
        fetchExampleIds();
    }

    private boolean checkIfDataExists() {
        // 간단한 확인: 사용자 테이블에 레코드가 있는지 확인
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.users", Integer.class);
        return count != null && count > 0;
    }

    private void executeSqlScript(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        String sql = new String(
                FileCopyUtils.copyToByteArray(resource.getInputStream()),
                StandardCharsets.UTF_8
        );

        // SQL 파일 실행
        jdbcTemplate.execute(sql);
    }

    private void fetchExampleIds() {
        // 첫 번째 퀴즈 ID 가져오기
        try {
            EXAMPLE_QUIZ_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.quizzes LIMIT 1", String.class);
            log.info("Example Quiz ID: " + EXAMPLE_QUIZ_ID);
        } catch (Exception e) {
            EXAMPLE_QUIZ_ID = "00000000-0000-0000-0000-000000000000";
            log.error("Failed to fetch example quiz ID: " + e.getMessage());
        }

        // 첫 번째 사용자 ID 가져오기
        try {
            EXAMPLE_USER_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.users LIMIT 1", String.class);
            log.info("Example User ID: " + EXAMPLE_USER_ID);
        } catch (Exception e) {
            EXAMPLE_USER_ID = "00000000-0000-0000-0000-000000000000";
            log.error("Failed to fetch example user ID: " + e.getMessage());
        }

        // 첫 번째 태그 ID 가져오기
        try {
            EXAMPLE_TAG_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.tags LIMIT 1", String.class);
            log.info("Example Tag ID: " + EXAMPLE_TAG_ID);
        } catch (Exception e) {
            EXAMPLE_TAG_ID = "00000000-0000-0000-0000-000000000000";
            log.error("Failed to fetch example tag ID: " + e.getMessage());
        }
    }
}