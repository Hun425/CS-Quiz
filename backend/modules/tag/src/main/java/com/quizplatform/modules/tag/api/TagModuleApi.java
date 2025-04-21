package com.quizplatform.modules.tag.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 태그 모듈 API 인터페이스
 * <p>
 * 태그 모듈이 다른 모듈에 제공하는 API를 정의합니다.
 * </p>
 */
@RequestMapping("/api/v1/tags")
public interface TagModuleApi {

    /**
     * 태그 ID로 태그 정보를 조회합니다.
     *
     * @param tagId 태그 ID
     * @return 태그 정보
     */
    @GetMapping("/{tagId}")
    ResponseEntity<?> getTagById(@PathVariable Long tagId);

    /**
     * 모든 태그 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 태그 목록
     */
    @GetMapping
    ResponseEntity<?> getAllTags(Pageable pageable);

    /**
     * 새로운 태그를 생성합니다.
     *
     * @param tagDto 태그 생성 정보
     * @return 생성된 태그 정보
     */
    @PostMapping
    ResponseEntity<?> createTag(@RequestBody Object tagDto);

    /**
     * 기존 태그를 업데이트합니다.
     *
     * @param tagId 태그 ID
     * @param tagDto 태그 업데이트 정보
     * @return 업데이트된 태그 정보
     */
    @PutMapping("/{tagId}")
    ResponseEntity<?> updateTag(@PathVariable Long tagId, @RequestBody Object tagDto);

    /**
     * 태그를 삭제합니다.
     *
     * @param tagId 태그 ID
     * @return 성공 여부
     */
    @DeleteMapping("/{tagId}")
    ResponseEntity<?> deleteTag(@PathVariable Long tagId);

    /**
     * 인기 태그를 조회합니다.
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록
     */
    @GetMapping("/popular")
    ResponseEntity<?> getPopularTags(@RequestParam(defaultValue = "10") int limit);

    /**
     * 태그 이름으로 태그를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 태그 목록
     */
    @GetMapping("/search")
    ResponseEntity<?> searchTags(@RequestParam String keyword);

    /**
     * 컨텐츠 ID와 컨텐츠 타입에 연결된 태그를 조회합니다.
     *
     * @param contentType 컨텐츠 타입 (예: quiz, user)
     * @param contentId 컨텐츠 ID
     * @return 연결된 태그 목록
     */
    @GetMapping("/content/{contentType}/{contentId}")
    ResponseEntity<?> getTagsByContent(@PathVariable String contentType, @PathVariable Long contentId);

    /**
     * 컨텐츠에 태그를 연결합니다.
     *
     * @param contentType 컨텐츠 타입
     * @param contentId 컨텐츠 ID
     * @param tagIds 태그 ID 목록
     * @return 성공 여부
     */
    @PostMapping("/content/{contentType}/{contentId}")
    ResponseEntity<?> assignTagsToContent(@PathVariable String contentType, @PathVariable Long contentId, @RequestBody List<Long> tagIds);
}