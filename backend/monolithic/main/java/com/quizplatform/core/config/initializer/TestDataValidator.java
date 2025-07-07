package com.quizplatform.core.config.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 테스트 데이터 검증을 전담하는 컴포넌트
 * 
 * 주요 기능:
 * - 기존 데이터 존재 여부 확인
 * - 테이블별 데이터 카운트
 * - 데이터 무결성 검증
 * 
 * @author 채기훈
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataValidator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 테스트 데이터가 존재하는지 확인합니다.
     *
     * @return 모든 필수 테이블에 데이터가 있으면 true
     */
    public boolean hasTestData() {
        try {
            Integer userCount = getTableCount("users");
            Integer quizCount = getTableCount("quizzes");
            Integer tagCount = getTableCount("tags");
            Integer questionCount = getTableCount("questions");
            
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
     * 특정 테이블의 행 개수를 안전하게 조회합니다.
     *
     * @param tableName 조회할 테이블명
     * @return 테이블의 행 개수 (조회 실패 시 0)
     */
    public int getTableCount(String tableName) {
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
     * SQL 실행 전후의 데이터 변화를 로깅합니다.
     *
     * @param beforeCounts 실행 전 카운트 배열 [users, quizzes, tags, questions]
     * @param afterCounts 실행 후 카운트 배열 [users, quizzes, tags, questions]
     */
    public void logDataChanges(int[] beforeCounts, int[] afterCounts) {
        String[] tableNames = {"Users", "Quizzes", "Tags", "Questions"};
        
        log.info("실행 전 데이터 개수 - Users: {}, Quizzes: {}, Tags: {}, Questions: {}", 
                beforeCounts[0], beforeCounts[1], beforeCounts[2], beforeCounts[3]);
        
        log.info("실행 후 데이터 개수 - Users: {}, Quizzes: {}, Tags: {}, Questions: {}", 
                afterCounts[0], afterCounts[1], afterCounts[2], afterCounts[3]);
        
        log.info("증가된 데이터 개수 - Users: +{}, Quizzes: +{}, Tags: +{}, Questions: +{}", 
                afterCounts[0] - beforeCounts[0], afterCounts[1] - beforeCounts[1], 
                afterCounts[2] - beforeCounts[2], afterCounts[3] - beforeCounts[3]);
    }

    /**
     * 모든 주요 테이블의 현재 카운트를 배열로 반환합니다.
     *
     * @return [users, quizzes, tags, questions] 순서의 카운트 배열
     */
    public int[] getCurrentCounts() {
        return new int[] {
            getTableCount("users"),
            getTableCount("quizzes"),
            getTableCount("tags"),
            getTableCount("questions")
        };
    }
}