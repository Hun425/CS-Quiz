package com.quizplatform.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Caffeine 캐시 작업을 가로채서 예외 처리를 제공하는 애스펙트
 * 
 * <p>Caffeine 캐시 관련 서비스 메서드에서 발생하는 예외를 로깅하고
 * 애플리케이션 흐름을 유지합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Aspect
@Component
@Slf4j
public class CaffeineOperationAspect {
    
    private final CacheManager cacheManager;
    
    public CaffeineOperationAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * Caffeine 캐시 사용 메서드를 위한 어드바이스
     * 
     * <p>캐시 관련 예외가 발생하면 로깅하고, 원본 메서드를 재실행합니다.</p>
     * 
     * @param joinPoint AOP 조인 포인트
     * @return 메서드 실행 결과
     * @throws Throwable 예외 발생 시
     */
    @Around("execution(* com.quizplatform.core.service.quiz.impl.QuizServiceImpl.*(..))")
    public Object handleCaffeineException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 원본 메서드 실행
            return joinPoint.proceed();
        } catch (Exception e) {
            // 예외가 캐시 관련인지 확인
            if (isCacheException(e)) {
                logCacheError(joinPoint, e, "Caffeine 캐시 관련 예외");
                return retryWithoutCache(joinPoint);
            }
            // 그 외의 예외는 그대로 전파
            throw e;
        }
    }
    
    /**
     * 예외가 캐시 관련인지 확인합니다.
     * 
     * @param e 검사할 예외
     * @return 캐시 관련 예외인 경우 true
     */
    private boolean isCacheException(Exception e) {
        // 예외 클래스 이름이나 메시지에서 캐시 관련 키워드 확인
        String exceptionName = e.getClass().getName().toLowerCase();
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        
        return exceptionName.contains("cache") || 
               exceptionName.contains("caffeine") || 
               message.contains("cache");
    }
    
    /**
     * 캐시 관련 예외를 로깅합니다.
     * 
     * @param joinPoint AOP 조인 포인트
     * @param e 발생한 예외
     * @param errorPrefix 오류 로그 접두사
     */
    private void logCacheError(ProceedingJoinPoint joinPoint, Exception e, String errorPrefix) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        log.error("{}: {} - 메서드: {}, 매개변수: {}", 
                 errorPrefix, e.getMessage(), method.getName(), joinPoint.getArgs(), e);
    }
    
    /**
     * 캐시 없이 메서드를 재실행합니다.
     * 
     * @param joinPoint AOP 조인 포인트
     * @return 메서드 실행 결과
     * @throws Throwable 예외 발생 시
     */
    private Object retryWithoutCache(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("캐시 없이 메서드 재시도: {}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("캐시 없이 재시도 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}