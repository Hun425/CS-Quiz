package com.quizplatform.modules.tag.service;

import com.quizplatform.modules.tag.dto.TagDto;
import com.quizplatform.modules.tag.dto.TagRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 태그 서비스 인터페이스
 * <p>
 * 태그 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * </p>
 */
public interface TagService {

    /**
     * 태그 ID로 태그 정보를 조회합니다.
     *
     * @param tagId 태그 ID
     * @return 태그 정보
     */
    TagDto getTagById(Long tagId);

    /**
     * 모든 태그 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 태그 목록
     */
    Page<TagDto> getAllTags(Pageable pageable);

    /**
     * 새로운 태그를 생성합니다.
     *
     * @param tagRequest 태그 생성 정보
     * @return 생성된 태그 정보
     */
    TagDto createTag(TagRequest tagRequest);

    /**
     * 기존 태그를 업데이트합니다.
     *
     * @param tagId 태그 ID
     * @param tagRequest 태그 업데이트 정보
     * @return 업데이트된 태그 정보
     */
    TagDto updateTag(Long tagId, TagRequest tagRequest);

    /**
     * 태그를 삭제합니다.
     *
     * @param tagId 태그 ID
     */
    void deleteTag(Long tagId);

    /**
     * 인기 태그를 조회합니다.
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록
     */
    List<TagDto> getPopularTags(int limit);

    /**
     * 태그 이름으로 태그를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 태그 목록
     */
    List<TagDto> searchTags(String keyword);

    /**
     * 컨텐츠 ID와 컨텐츠 타입에 연결된 태그를 조회합니다.
     *
     * @param contentType 컨텐츠 타입 (예: quiz, user)
     * @param contentId 컨텐츠 ID
     * @return 연결된 태그 목록
     */
    List<TagDto> getTagsByContent(String contentType, Long contentId);

    /**
     * 컨텐츠에 태그를 연결합니다.
     *
     * @param contentType 컨텐츠 타입
     * @param contentId 컨텐츠 ID
     * @param tagIds 태그 ID 목록
     */
    void assignTagsToContent(String contentType, Long contentId, List<Long> tagIds);
}