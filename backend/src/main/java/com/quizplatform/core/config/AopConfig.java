package com.quizplatform.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 설정 클래스
 * 
 * <p>애스펙트 오리엔티드 프로그래밍(AOP) 기능을 활성화합니다.
 * proxyTargetClass=true 설정으로 CGLIB 기반 프록시를 사용하여
 * 모든 클래스에 대해 AOP를 적용할 수 있도록 합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {
    // 추가 설정이 필요하면 여기에 빈 정의
}