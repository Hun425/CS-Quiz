package com.quizplatform.core.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

/**
 * RestTemplate을 사용한 모듈 클라이언트 구현체
 * <p>
 * RestTemplate을 사용하여 HTTP 통신을 수행하는 모듈 클라이언트 구현입니다.
 * </p>
 */
public class RestTemplateModuleClient implements ModuleClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    /**
     * RestTemplateModuleClient 생성자
     *
     * @param restTemplate RestTemplate 인스턴스
     * @param baseUrl 기본 URL
     */
    public RestTemplateModuleClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public <T> T get(String path, Class<T> responseType) {
        return get(path, Collections.emptyMap(), responseType);
    }

    @Override
    public <T> T get(String path, Map<String, Object> params, Class<T> responseType) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
        
        // 쿼리 파라미터 추가
        params.forEach(builder::queryParam);
        
        return restTemplate.getForObject(builder.toUriString(), responseType);
    }

    @Override
    public <T> T post(String path, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        
        return restTemplate.postForObject(baseUrl + path, entity, responseType);
    }

    @Override
    public <T> T put(String path, Object body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        
        return restTemplate.exchange(baseUrl + path, HttpMethod.PUT, entity, responseType).getBody();
    }

    @Override
    public <T> T delete(String path, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + path, HttpMethod.DELETE, null, responseType).getBody();
    }
}