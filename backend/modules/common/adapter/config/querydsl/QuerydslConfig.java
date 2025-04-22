package com.quizplatform.common.adapter.config.querydsl;


// com.querydsl.jpa.impl.JPAQueryFactory 패키지를 찾을 수 없는 오류 해결

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Querydsl 설정 클래스
 * 
 * <p>JPA를 사용한 동적 쿼리 생성을 위한 Querydsl 설정을 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
public class QuerydslConfig {

    /**
     * JPA 엔티티 매니저
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory 빈 생성
     * 
     * <p>애플리케이션 전반에서 사용할 수 있는 JPAQueryFactory 인스턴스를 생성합니다.</p>
     * 
     * @return 엔티티 매니저로 초기화된 JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
} 