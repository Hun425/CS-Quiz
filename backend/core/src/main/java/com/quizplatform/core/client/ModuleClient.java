package com.quizplatform.core.client;

import java.util.Map;

/**
 * 모듈 간 통신을 위한 인터페이스
 * <p>
 * 모듈 간 API 통신에 사용되는 공통 클라이언트 인터페이스입니다.
 * 각 모듈은 이 인터페이스를 구현하여 다른 모듈과 통신할 수 있습니다.
 * </p>
 */
public interface ModuleClient {

    /**
     * GET 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     */
    <T> T get(String path, Class<T> responseType);

    /**
     * GET 요청을 쿼리 파라미터와 함께 수행합니다.
     *
     * @param path 요청 경로
     * @param params 쿼리 파라미터
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     */
    <T> T get(String path, Map<String, Object> params, Class<T> responseType);

    /**
     * POST 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param body 요청 바디
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     */
    <T> T post(String path, Object body, Class<T> responseType);

    /**
     * PUT 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param body 요청 바디
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     */
    <T> T put(String path, Object body, Class<T> responseType);

    /**
     * DELETE 요청을 수행합니다.
     *
     * @param path 요청 경로
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     */
    <T> T delete(String path, Class<T> responseType);
}