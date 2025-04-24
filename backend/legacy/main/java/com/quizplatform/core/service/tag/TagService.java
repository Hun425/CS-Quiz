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
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
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
        List<Tag> tags = tagRepository.findAll();
        
        // 결과 태그 응답 리스트
        List<TagResponse> responses = new ArrayList<>();

        // 세션이 열려있는 상태에서 지연 로딩된 컬렉션을 명시적으로 초기화
        for (Tag tag : tags) {
            Hibernate.initialize(tag.getSynonyms());
            // 퀴즈 개수는 별도의 쿼리로 조회
            int quizCount = tagRepository.countQuizzesForTag(tag.getId());
            responses.add(TagResponse.from(tag, quizCount));
        }

        return responses;
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

        // 태그 ID 목록 추출
        List<Long> tagIds = tagsPage.getContent().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());
        
        // 각 태그별 퀴즈 개수 조회
        Map<Long, Integer> quizCountMap = new HashMap<>();
        for (Long tagId : tagIds) {
            quizCountMap.put(tagId, tagRepository.countQuizzesForTag(tagId));
        }
        
        // DTO로 변환
        Page<TagResponse> tagResponses = tagsPage.map(tag -> {
            Hibernate.initialize(tag.getSynonyms());
            int quizCount = quizCountMap.getOrDefault(tag.getId(), 0);
            return TagResponse.from(tag, quizCount);
        });
        
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

        // 세션이 열려있는 상태에서 지연 로딩된 컬렉션을 명시적으로 초기화
        Hibernate.initialize(tag.getSynonyms());
        
        // 퀴즈 개수는 별도의 쿼리로 조회
        int quizCount = tagRepository.countQuizzesForTag(tagId);

        return TagResponse.from(tag, quizCount);
    }

    /**
     * 루트 태그(부모가 없는 최상위 태그)만을 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'roots'")
    public List<TagResponse> getRootTags() {
        List<Tag> rootTags = tagRepository.findAllRootTags();
        
        // 결과 태그 응답 리스트
        List<TagResponse> responses = new ArrayList<>();

        // 세션이 열려있는 상태에서 지연 로딩된 컬렉션을 명시적으로 초기화
        for (Tag tag : rootTags) {
            Hibernate.initialize(tag.getSynonyms());
            // 퀴즈 개수는 별도의 쿼리로 조회
            int quizCount = tagRepository.countQuizzesForTag(tag.getId());
            responses.add(TagResponse.from(tag, quizCount));
        }

        return responses;
    }

    /**
     * 인기 태그(퀴즈에 가장 많이 사용된 태그)를 조회합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tags", key = "'popular:' + #limit")
    public List<TagResponse> getPopularTags(int limit) {
        // 태그별 퀴즈 개수 조회
        List<Object[]> tagCounts = tagRepository.findTopTagsByQuizCount(limit);
        
        // 결과 리스트
        List<TagResponse> result = new ArrayList<>();
        
        // 퀴즈 개수를 기준으로 정렬된 태그 정보로 응답 생성
        for (Object[] row : tagCounts) {
            Tag tag = (Tag) row[0];
            Long count = (Long) row[1];
            
            // 동의어 초기화
            Hibernate.initialize(tag.getSynonyms());
            
            // 응답 생성 및 추가
            result.add(TagResponse.from(tag, count.intValue()));
        }
        
        return result;
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

        List<TagResponse> responses = new ArrayList<>();
        
        for (Tag child : parentTag.getChildren()) {
            // 각 자식 태그의 퀴즈 개수 조회
            int quizCount = tagRepository.countQuizzesForTag(child.getId());
            // 동의어 초기화
            Hibernate.initialize(child.getSynonyms());
            // 응답 생성
            responses.add(TagResponse.from(child, quizCount));
        }
        
        return responses;
    }
}