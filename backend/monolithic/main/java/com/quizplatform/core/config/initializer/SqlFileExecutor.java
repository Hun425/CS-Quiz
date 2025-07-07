package com.quizplatform.core.config.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * SQL 파일 실행을 전담하는 컴포넌트
 * 
 * 주요 기능:
 * - SQL 파일 로딩 및 실행
 * - 트랜잭션 관리
 * - 실행 로깅 및 오류 처리
 * 
 * @author 채기훈
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlFileExecutor {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;
    private final PlatformTransactionManager transactionManager;

    /**
     * SQL 파일을 수동 트랜잭션으로 안전하게 실행합니다.
     *
     * @param sqlFile 실행할 SQL 파일 경로
     * @throws IOException SQL 파일 읽기 실패 시
     */
    public void executeSqlFileWithTransaction(String sqlFile) throws IOException {
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
     * SQL 파일을 실행합니다.
     *
     * @param resourcePath SQL 파일의 리소스 경로
     * @throws IOException 파일 읽기 실패 시
     */
    public void executeSqlFile(String resourcePath) throws IOException {
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
            log.info("SQL 실행 시작: {}", resourcePath);
            jdbcTemplate.execute(sql);
            log.info("SQL 실행 완료: {}", resourcePath);
        } catch (Exception e) {
            log.error("SQL 실행 중 오류: {} - {}", resourcePath, e.getMessage(), e);
            
            // SQL 내용의 일부를 로그로 출력 (디버깅용)
            String sqlPreview = sql.length() > 500 ? sql.substring(0, 500) + "..." : sql;
            log.error("실패한 SQL 내용 (처음 500자): {}", sqlPreview);
            
            throw e;
        }
    }

    /**
     * 여러 SQL 파일을 순차적으로 실행합니다.
     *
     * @param sqlFiles 실행할 SQL 파일 경로 리스트
     */
    public void executeAllSqlFiles(java.util.List<String> sqlFiles) {
        for (String sqlFile : sqlFiles) {
            try {
                log.info("실행 중: {}", sqlFile);
                executeSqlFileWithTransaction(sqlFile);
                log.info("완료: {}", sqlFile);
            } catch (Exception e) {
                log.error("SQL 파일 실행 실패: {} - {}", sqlFile, e.getMessage(), e);
                // 오류가 발생해도 다음 파일 계속 실행
            }
        }
    }
}