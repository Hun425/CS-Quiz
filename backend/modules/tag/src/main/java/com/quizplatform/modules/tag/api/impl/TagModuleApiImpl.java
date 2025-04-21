package com.quizplatform.modules.tag.api.impl;

import com.quizplatform.modules.tag.api.TagModuleApi;
import com.quizplatform.modules.tag.dto.TagDto;
import com.quizplatform.modules.tag.dto.TagRequest;
import com.quizplatform.modules.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 태그 모듈 API 구현체
 * <p>
 * TagModuleApi 인터페이스를 구현하여 태그 관련 API를 제공합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TagModuleApiImpl implements TagModuleApi {

    private final TagService tagService;

    @Override
    public ResponseEntity<?> getTagById(Long tagId) {
        log.debug("Fetching tag by ID: {}", tagId);
        TagDto tag = tagService.getTagById(tagId);
        return ResponseEntity.ok(tag);
    }

    @Override
    public ResponseEntity<?> getAllTags(Pageable pageable) {
        log.debug("Fetching all tags");
        Page<TagDto> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }

    @Override
    public ResponseEntity<?> createTag(Object tagDto) {
        log.debug("Creating new tag");
        TagRequest tagRequest = (TagRequest) tagDto;
        TagDto createdTag = tagService.createTag(tagRequest);
        return ResponseEntity.ok(createdTag);
    }

    @Override
    public ResponseEntity<?> updateTag(Long tagId, Object tagDto) {
        log.debug("Updating tag with ID: {}", tagId);
        TagRequest tagRequest = (TagRequest) tagDto;
        TagDto updatedTag = tagService.updateTag(tagId, tagRequest);
        return ResponseEntity.ok(updatedTag);
    }

    @Override
    public ResponseEntity<?> deleteTag(Long tagId) {
        log.debug("Deleting tag with ID: {}", tagId);
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getPopularTags(int limit) {
        log.debug("Fetching popular tags with limit: {}", limit);
        List<TagDto> popularTags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(popularTags);
    }

    @Override
    public ResponseEntity<?> searchTags(String keyword) {
        log.debug("Searching tags with keyword: {}", keyword);
        List<TagDto> searchResults = tagService.searchTags(keyword);
        return ResponseEntity.ok(searchResults);
    }

    @Override
    public ResponseEntity<?> getTagsByContent(String contentType, Long contentId) {
        log.debug("Fetching tags for content type: {} and ID: {}", contentType, contentId);
        List<TagDto> contentTags = tagService.getTagsByContent(contentType, contentId);
        return ResponseEntity.ok(contentTags);
    }

    @Override
    public ResponseEntity<?> assignTagsToContent(String contentType, Long contentId, List<Long> tagIds) {
        log.debug("Assigning tags: {} to content type: {} and ID: {}", tagIds, contentType, contentId);
        tagService.assignTagsToContent(contentType, contentId, tagIds);
        return ResponseEntity.ok().build();
    }
}