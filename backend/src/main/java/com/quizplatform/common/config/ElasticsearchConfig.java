package com.quizplatform.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 검색 엔진 설정 클래스
 * 
 * <p>Elasticsearch 연결 및 검색 기능 사용을 위한 설정을 담당합니다.
 * REST 클라이언트, 트랜스포트, 클라이언트, 템플릿 등을 구성합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.quizplatform.modules.tag.repository")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    /**
     * Elasticsearch REST 클라이언트 빈
     * 
     * <p>Elasticsearch 서버와의 HTTP 통신을 담당하는 저수준 클라이언트를 구성합니다.</p>
     * 
     * @return 구성된 REST 클라이언트
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(elasticsearchUri))
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(5000)
                                .setSocketTimeout(60000))
                .setCompressionEnabled(true)
                .build();
    }

    /**
     * Elasticsearch 트랜스포트 빈
     * 
     * <p>REST 클라이언트를 사용하여 Elasticsearch 서버와 통신할 트랜스포트 계층을 구성합니다.</p>
     * 
     * @param restClient REST 클라이언트
     * @return 구성된 Elasticsearch 트랜스포트
     */
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(
                restClient,
                new JacksonJsonpMapper());
    }

    /**
     * Elasticsearch 클라이언트 빈
     * 
     * <p>Elasticsearch API를 사용하기 위한 고수준 클라이언트를 구성합니다.</p>
     * 
     * @param transport Elasticsearch 트랜스포트
     * @return 구성된 Elasticsearch 클라이언트
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    /**
     * Elasticsearch 템플릿 빈
     * 
     * <p>Spring Data Elasticsearch를 사용하기 위한 템플릿을 구성합니다.</p>
     * 
     * @param elasticsearchClient Elasticsearch 클라이언트
     * @return 구성된 Elasticsearch 템플릿
     */
    @Bean
    public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        return new ElasticsearchTemplate(elasticsearchClient);
    }
}