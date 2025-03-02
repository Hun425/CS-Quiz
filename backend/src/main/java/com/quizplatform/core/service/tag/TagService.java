package com.quizplatform.core.service.tag;

import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.dto.common.PageResponse;
import com.quizplatform.core.dto.tag.TagCreateRequest;
import com.quizplatform.core.dto.tag.TagResponse;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 모든 태그 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'all'")
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 태그 검색 결과를 페이지네이션하여 반환합니다.
     */
    @Transactional(readOnly = true)
    public PageResponse<TagResponse> searchTags(String name, Pageable pageable) {
        Page<Tag> tagsPage;

        if (StringUtils.hasText(name)) {
            // 태그 이름으로 검색 로직
            tagsPage = tagRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            // 모든 태그 조회
            tagsPage = tagRepository.findAll(pageable);
        }

        Page<TagResponse> tagResponses = tagsPage.map(TagResponse::from);
        return PageResponse.of(tagResponses);
    }

    /**
     * 특정 태그를 ID로 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "#tagId")
    public TagResponse getTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다. ID: " + tagId));
        return TagResponse.from(tag);
    }

    /**
     * 루트 태그(부모가 없는 최상위 태그)만을 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'roots'")
    public List<TagResponse> getRootTags() {
        List<Tag> rootTags = tagRepository.findAllRootTags();
        return rootTags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 인기 태그(퀴즈에 가장 많이 사용된 태그)를 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'popular:' + #limit")
    public List<TagResponse> getPopularTags(int limit) {
        return tagRepository.findAll().stream()
                .sorted(Comparator.comparing(tag -> tag.getQuizzes().size(), Comparator.reverseOrder()))
                .limit(limit)
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 태그를 생성합니다.
     */
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagResponse createTag(TagCreateRequest request) {
        // 이름 중복 체크
        if (tagRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, "이미 존재하는 태그 이름입니다: " + request.getName());
        }

        // 태그 생성
        Tag tag = Tag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 동의어 추가
        if (request.getSynonyms() != null) {
            request.getSynonyms().forEach(tag::addSynonym);
        }

        // 부모 태그 설정
        if (request.getParentId() != null) {
            Tag parent = tagRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부모 태그를 찾을 수 없습니다. ID: " + request.getParentId()));
            parent.addChild(tag);
        }

        Tag savedTag = tagRepository.save(tag);
        return TagResponse.from(savedTag);
    }

    /**
     * 기존 태그를 수정합니다.
     */
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagResponse updateTag(Long tagId, TagCreateRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다. ID: " + tagId));

        // 이름 중복 체크 (자기 자신 제외)
        if (!tag.getName().equals(request.getName()) &&
                tagRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, "이미 존재하는 태그 이름입니다: " + request.getName());
        }

        // 기본 정보 업데이트
        tag = Tag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 동의어 업데이트
        tag.getSynonyms().clear();
        if (request.getSynonyms() != null) {
            request.getSynonyms().forEach(tag::addSynonym);
        }

        // 부모 태그 업데이트
        if (request.getParentId() != null &&
                (tag.getParent() == null || !tag.getParent().getId().equals(request.getParentId()))) {
            Tag parent = tagRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부모 태그를 찾을 수 없습니다. ID: " + request.getParentId()));
            parent.addChild(tag);
        } else if (request.getParentId() == null && tag.getParent() != null) {
            // 부모 태그 제거
            tag.getParent().getChildren().remove(tag);
            tag.setParent(null);
        }

        Tag updatedTag = tagRepository.save(tag);
        return TagResponse.from(updatedTag);
    }

    /**
     * 태그를 삭제합니다.
     */
    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다. ID: " + tagId));

        // 자식 태그가 있는 경우
        if (!tag.getChildren().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "자식 태그가 있어 삭제할 수 없습니다. 먼저 자식 태그를 삭제하세요.");
        }

        // 퀴즈에서 참조하고 있는지 확인
        if (!tag.getQuizzes().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이 태그를 사용하는 퀴즈가 있어 삭제할 수 없습니다.");
        }

        tagRepository.delete(tag);
    }

    /**
     * 특정 태그의 자식 태그들을 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'children:' + #parentId")
    public List<TagResponse> getChildTags(Long parentId) {
        Tag parentTag = tagRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "부모 태그를 찾을 수 없습니다. ID: " + parentId));

        return parentTag.getChildren().stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }
}