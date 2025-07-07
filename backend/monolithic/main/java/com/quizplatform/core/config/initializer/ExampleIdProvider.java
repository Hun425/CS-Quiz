package com.quizplatform.core.config.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Swagger 예제용 ID를 제공하는 컴포넌트
 * 
 * 주요 기능:
 * - 실제 데이터베이스에서 유효한 ID 조회
 * - Swagger API 문서의 예제 값 제공
 * - 기본값 fallback 처리
 * 
 * @author 채기훈
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExampleIdProvider {

    private final JdbcTemplate jdbcTemplate;
    
    private static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";
    
    // Swagger 예제로 사용할 ID 값들
    public static String EXAMPLE_QUIZ_ID;
    public static String EXAMPLE_USER_ID;
    public static String EXAMPLE_TAG_ID;

    /**
     * 모든 예제 ID를 설정합니다.
     */
    public void setAllExampleIds() {
        setExampleUserId();
        setExampleQuizId();
        setExampleTagId();
        
        log.info("모든 예제 ID 설정 완료 - User: {}, Quiz: {}, Tag: {}", 
                EXAMPLE_USER_ID, EXAMPLE_QUIZ_ID, EXAMPLE_TAG_ID);
    }

    /**
     * 예제 사용자 ID를 설정합니다.
     */
    public void setExampleUserId() {
        try {
            EXAMPLE_USER_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.users LIMIT 1", String.class);
            log.info("Example User ID: {}", EXAMPLE_USER_ID);
        } catch (Exception e) {
            EXAMPLE_USER_ID = DEFAULT_UUID;
            log.warn("사용자 ID 가져오기 실패, 기본값 사용: {}", e.getMessage());
        }
    }

    /**
     * 예제 퀴즈 ID를 설정합니다.
     */
    public void setExampleQuizId() {
        try {
            EXAMPLE_QUIZ_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.quizzes LIMIT 1", String.class);
            log.info("Example Quiz ID: {}", EXAMPLE_QUIZ_ID);
        } catch (Exception e) {
            EXAMPLE_QUIZ_ID = DEFAULT_UUID;
            log.warn("퀴즈 ID 가져오기 실패, 기본값 사용: {}", e.getMessage());
        }
    }

    /**
     * 예제 태그 ID를 설정합니다.
     */
    public void setExampleTagId() {
        try {
            EXAMPLE_TAG_ID = jdbcTemplate.queryForObject(
                    "SELECT id FROM public.tags LIMIT 1", String.class);
            log.info("Example Tag ID: {}", EXAMPLE_TAG_ID);
        } catch (Exception e) {
            EXAMPLE_TAG_ID = DEFAULT_UUID;
            log.warn("태그 ID 가져오기 실패, 기본값 사용: {}", e.getMessage());
        }
    }

    /**
     * 특정 조건에 맞는 예제 ID를 조회합니다.
     *
     * @param tableName 테이블명
     * @param condition WHERE 조건 (예: "is_public = true")
     * @return 조회된 ID 또는 기본값
     */
    public String getExampleIdWithCondition(String tableName, String condition) {
        try {
            String sql = String.format("SELECT id FROM public.%s WHERE %s LIMIT 1", tableName, condition);
            String id = jdbcTemplate.queryForObject(sql, String.class);
            log.debug("조건부 예제 ID 조회 성공 - 테이블: {}, 조건: {}, ID: {}", tableName, condition, id);
            return id;
        } catch (Exception e) {
            log.warn("조건부 예제 ID 조회 실패 - 테이블: {}, 조건: {}, 오류: {}", tableName, condition, e.getMessage());
            return DEFAULT_UUID;
        }
    }

    /**
     * 공개된 퀴즈 ID를 예제로 가져옵니다.
     *
     * @return 공개된 퀴즈 ID 또는 기본값
     */
    public String getPublicQuizExampleId() {
        return getExampleIdWithCondition("quizzes", "is_public = true");
    }

    /**
     * 특정 타입의 퀴즈 ID를 예제로 가져옵니다.
     *
     * @param quizType 퀴즈 타입 (REGULAR, DAILY 등)
     * @return 해당 타입의 퀴즈 ID 또는 기본값
     */
    public String getQuizExampleIdByType(String quizType) {
        return getExampleIdWithCondition("quizzes", "quiz_type = '" + quizType + "'");
    }
}