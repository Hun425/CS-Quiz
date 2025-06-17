package com.quizplatform.core.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * JPA 성능 테스트를 위한 유틸리티 클래스
 * 다양한 성능 지표를 수집하고 로깅하는 기능을 제공합니다.
 */
@Slf4j
@Component
public class PerformanceTestUtil {

    @PersistenceContext
    private EntityManager entityManager;

    private static final AtomicInteger testCounter = new AtomicInteger(0);

    /**
     * 성능 테스트를 실행하고 결과를 반환합니다.
     *
     * @param testName 테스트 이름
     * @param operation 테스트할 작업
     * @param <T> 반환 타입
     * @return 테스트 결과
     */
    public <T> PerformanceResult<T> measurePerformance(String testName, Supplier<T> operation) {
        // 테스트 ID 생성
        int testId = testCounter.incrementAndGet();
        
        // 통계 리셋
        Statistics statistics = getHibernateStatistics();
        statistics.clear();

        // 가비지 컬렉션 요청 (정확한 메모리 측정을 위해)
        System.gc();
        
        // 초기 메모리 상태 기록
        long initialMemory = getUsedMemory();
        
        // 실행 시간 측정 시작
        StopWatch stopWatch = new StopWatch(testName);
        stopWatch.start();
        
        // 작업 실행
        T result = operation.get();
        
        // 측정 종료
        stopWatch.stop();
        long finalMemory = getUsedMemory();
        
        // 결과 생성
        PerformanceResult<T> performanceResult = new PerformanceResult<>();
        performanceResult.setTestId(testId);
        performanceResult.setTestName(testName);
        performanceResult.setResult(result);
        performanceResult.setExecutionTime(stopWatch.getTotalTimeMillis());
        performanceResult.setMemoryUsed(finalMemory - initialMemory);
        
        // Hibernate 통계 수집
        performanceResult.setQueryCount(statistics.getQueryExecutionCount());
        performanceResult.setEntityLoadCount(statistics.getEntityLoadCount());
        performanceResult.setCollectionLoadCount(statistics.getCollectionLoadCount());
        
        // 결과 로깅
        logPerformanceResult(performanceResult);
        
        return performanceResult;
    }
    
    /**
     * Hibernate 통계 객체 반환
     */
    private Statistics getHibernateStatistics() {
        return entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
    }
    
    /**
     * 현재 사용 중인 메모리 양 반환 (MB)
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
    
    /**
     * 성능 결과 로깅
     */
    private <T> void logPerformanceResult(PerformanceResult<T> result) {
        log.info("========== Performance Test Result [{}] ==========", result.getTestId());
        log.info("Test Name: {}", result.getTestName());
        log.info("Execution Time: {} ms", result.getExecutionTime());
        log.info("Memory Used: {} MB", result.getMemoryUsed());
        log.info("Query Count: {}", result.getQueryCount());
        log.info("Entity Load Count: {}", result.getEntityLoadCount());
        log.info("Collection Load Count: {}", result.getCollectionLoadCount());
        log.info("=====================================================");
    }
    
    /**
     * 두 성능 결과 비교 로깅
     */
    public <T> void compareResults(PerformanceResult<T> before, PerformanceResult<T> after, String description) {
        log.info("========== Performance Comparison: {} ==========", description);
        log.info("Before Test: {} (ID: {})", before.getTestName(), before.getTestId());
        log.info("After Test: {} (ID: {})", after.getTestName(), after.getTestId());
        
        // 시간 비교
        long timeDiff = before.getExecutionTime() - after.getExecutionTime();
        double timeImprovement = calculateImprovement(before.getExecutionTime(), after.getExecutionTime());
        log.info("Time: {} ms -> {} ms (Diff: {} ms, Improvement: {:.2f}%)", 
                before.getExecutionTime(), after.getExecutionTime(), timeDiff, timeImprovement);
        
        // 쿼리 수 비교
        long queryDiff = before.getQueryCount() - after.getQueryCount();
        double queryImprovement = calculateImprovement(before.getQueryCount(), after.getQueryCount());
        log.info("Queries: {} -> {} (Diff: {}, Improvement: {:.2f}%)", 
                before.getQueryCount(), after.getQueryCount(), queryDiff, queryImprovement);
        
        // 메모리 사용량 비교
        long memoryDiff = before.getMemoryUsed() - after.getMemoryUsed();
        double memoryImprovement = calculateImprovement(before.getMemoryUsed(), after.getMemoryUsed());
        log.info("Memory: {} MB -> {} MB (Diff: {} MB, Improvement: {:.2f}%)", 
                before.getMemoryUsed(), after.getMemoryUsed(), memoryDiff, memoryImprovement);
        
        // 엔티티 로드 수 비교
        long entityDiff = before.getEntityLoadCount() - after.getEntityLoadCount();
        double entityImprovement = calculateImprovement(before.getEntityLoadCount(), after.getEntityLoadCount());
        log.info("Entities: {} -> {} (Diff: {}, Improvement: {:.2f}%)", 
                before.getEntityLoadCount(), after.getEntityLoadCount(), entityDiff, entityImprovement);
        
        // 컬렉션 로드 수 비교
        long collectionDiff = before.getCollectionLoadCount() - after.getCollectionLoadCount();
        double collectionImprovement = calculateImprovement(before.getCollectionLoadCount(), after.getCollectionLoadCount());
        log.info("Collections: {} -> {} (Diff: {}, Improvement: {:.2f}%)", 
                before.getCollectionLoadCount(), after.getCollectionLoadCount(), collectionDiff, collectionImprovement);
        
        log.info("=====================================================");
    }
    
    /**
     * 개선율 계산 (%)
     */
    private double calculateImprovement(long before, long after) {
        if (before == 0) return 0;
        return ((double) (before - after) / before) * 100;
    }
    
    /**
     * 성능 테스트 결과를 저장하는 클래스
     */
    public static class PerformanceResult<T> {
        private int testId;
        private String testName;
        private T result;
        private long executionTime;
        private long memoryUsed;
        private long queryCount;
        private long entityLoadCount;
        private long collectionLoadCount;
        private Map<String, Object> additionalMetrics = new HashMap<>();
        
        // Getters and Setters
        public int getTestId() { return testId; }
        public void setTestId(int testId) { this.testId = testId; }
        
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        
        public T getResult() { return result; }
        public void setResult(T result) { this.result = result; }
        
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        
        public long getMemoryUsed() { return memoryUsed; }
        public void setMemoryUsed(long memoryUsed) { this.memoryUsed = memoryUsed; }
        
        public long getQueryCount() { return queryCount; }
        public void setQueryCount(long queryCount) { this.queryCount = queryCount; }
        
        public long getEntityLoadCount() { return entityLoadCount; }
        public void setEntityLoadCount(long entityLoadCount) { this.entityLoadCount = entityLoadCount; }
        
        public long getCollectionLoadCount() { return collectionLoadCount; }
        public void setCollectionLoadCount(long collectionLoadCount) { this.collectionLoadCount = collectionLoadCount; }
        
        public Map<String, Object> getAdditionalMetrics() { return additionalMetrics; }
        
        public void addMetric(String name, Object value) {
            additionalMetrics.put(name, value);
        }
    }
}
