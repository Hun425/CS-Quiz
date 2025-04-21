package com.quizplatform.modules.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 검색 모듈의 진입점 클래스
 * <p>
 * 이 모듈은 퀴즈, 태그, 사용자 등에 대한 검색 기능을 제공합니다.
 * ElasticSearch를 활용한 고급 검색 기능을 지원합니다.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.search",
    "com.quizplatform.modules.quiz",
    "com.quizplatform.modules.tag"
})
@EntityScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.search.domain",
    "com.quizplatform.modules.quiz.domain",
    "com.quizplatform.modules.tag.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.quizplatform.modules.search.repository.jpa"
})
@EnableElasticsearchRepositories(basePackages = {
    "com.quizplatform.modules.search.repository.elasticsearch"
})
public class SearchModuleApplication {

    /**
     * 검색 모듈 애플리케이션 실행 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(SearchModuleApplication.class, args);
    }
}