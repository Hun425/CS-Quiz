package com.quizplatform.core.service.tag;

import com.quizplatform.core.dto.common.PageResponse;
import com.quizplatform.core.dto.tag.TagCreateRequest;
import com.quizplatform.core.dto.tag.TagResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 태그 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 *
 * <p>태그의 생성, 조회, 수정, 삭제 및 계층 구조 관리 기능을 정의합니다.
 * 태그는 퀴즈를 분류하고 검색하는데 사용됩니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface TagService {

    /**
     * 모든 태그 목록을 조회합니다.
     *
     * <p>시스템에 등록된 모든 태그를 조회하며, 각 태그에 연결된 퀴즈 개수도 함께 제공합니다.</p>
     *
     * @return 모든 태그 목록
     */
    List<TagResponse> getAllTags();

    /**
     * 태그 검색 결과를 페이지네이션하여 반환합니다.
     *
     * <p>태그 이름에 검색어가 포함된 태그를 페이지 단위로 조회합니다.
     * 검색어가 제공되지 않으면 모든 태그를 페이지 단위로 조회합니다.</p>
     *
     * @param name 검색할 태그 이름 (null 가능)
     * @param pageable 페이지 정보
     * @return 페이지네이션된 태그 목록
     */
    PageResponse<TagResponse> searchTags(String name, Pageable pageable);

    /**
     * 특정 태그를 ID로 조회합니다.
     *
     * <p>지정된 ID의 태그를 조회하며, 태그에 연결된 퀴즈 개수도 함께 제공합니다.</p>
     *
     * @param tagId 조회할 태그 ID
     * @return 태그 정보
     */
    TagResponse getTag(Long tagId);

    /**
     * 루트 태그(부모가 없는 최상위 태그)만을 조회합니다.
     *
     * <p>부모 태그가 없는 최상위 태그만을 조회합니다.</p>
     *
     * @return 루트 태그 목록
     */
    List<TagResponse> getRootTags();

    /**
     * 인기 태그(퀴즈에 가장 많이 사용된 태그)를 조회합니다.
     *
     * <p>퀴즈에 가장 많이 사용된 인기 태그를 제한된 개수만큼 조회합니다.</p>
     *
     * @param limit 조회할 인기 태그 수
     * @return 인기 태그 목록
     */
    List<TagResponse> getPopularTags(int limit);

    /**
     * 새로운 태그를 생성합니다.
     *
     * <p>새로운 태그를 생성하고, 동의어와 부모 태그를 설정합니다.</p>
     *
     * @param request 태그 생성 요청
     * @return 생성된 태그 정보
     */
    TagResponse createTag(TagCreateRequest request);

    /**
     * 기존 태그를 수정합니다.
     *
     * <p>기존 태그의 이름, 설명, 동의어, 부모 태그를 수정합니다.</p>
     *
     * @param tagId 수정할 태그 ID
     * @param request 태그 수정 요청
     * @return 수정된 태그 정보
     */
    TagResponse updateTag(Long tagId, TagCreateRequest request);

    /**
     * 태그를 삭제합니다.
     *
     * <p>지정된 ID의 태그를 삭제합니다. 자식 태그가 있거나 퀴즈에서 참조하고 있는 경우 삭제할 수 없습니다.</p>
     *
     * @param tagId 삭제할 태그 ID
     */
    void deleteTag(Long tagId);

    /**
     * 특정 태그의 자식 태그들을 조회합니다.
     *
     * <p>지정된 ID의 태그에 속한 자식 태그들을 조회합니다.</p>
     *
     * @param parentId 부모 태그 ID
     * @return 자식 태그 목록
     */
    List<TagResponse> getChildTags(Long parentId);
}