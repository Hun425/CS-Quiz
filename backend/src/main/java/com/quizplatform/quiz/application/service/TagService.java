package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.port.out.LoadTagPort;
import com.quizplatform.quiz.application.port.out.SaveTagPort;
import com.quizplatform.quiz.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * 태그 서비스
 */
@Service
@RequiredArgsConstructor
public class TagService {
    private final LoadTagPort loadTagPort;
    private final SaveTagPort saveTagPort;

    /**
     * 새로운 태그를 생성합니다.
     */
    @Transactional
    public Tag createTag(String name, String description) {
        // 태그 이름 중복 확인
        Optional<Tag> existingTag = loadTagPort.findByName(name);
        if (existingTag.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다: " + name);
        }
        
        // 태그 생성
        Tag tag = Tag.builder()
                .name(name)
                .description(description)
                .build();
        
        // 태그 저장
        return saveTagPort.save(tag);
    }

    /**
     * 하위 태그를 생성합니다.
     */
    @Transactional
    public Tag createChildTag(Long parentId, String name, String description) {
        // 부모 태그 조회
        Optional<Tag> parentTagOptional = loadTagPort.findById(parentId);
        if (parentTagOptional.isEmpty()) {
            throw new NoSuchElementException("부모 태그를 찾을 수 없습니다: " + parentId);
        }
        
        Tag parentTag = parentTagOptional.get();
        
        // 태그 이름 중복 확인
        Optional<Tag> existingTag = loadTagPort.findByName(name);
        if (existingTag.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 태그입니다: " + name);
        }
        
        // 하위 태그 생성
        Tag childTag = parentTag.createChild(name, description);
        
        // 태그 저장
        return saveTagPort.save(childTag);
    }

    /**
     * 태그 정보를 수정합니다.
     */
    @Transactional
    public Tag updateTag(Long tagId, String name, String description) {
        // 태그 조회
        Optional<Tag> tagOptional = loadTagPort.findById(tagId);
        if (tagOptional.isEmpty()) {
            throw new NoSuchElementException("태그를 찾을 수 없습니다: " + tagId);
        }
        
        Tag tag = tagOptional.get();
        
        // 이름이 변경된 경우 중복 확인
        if (name != null && !name.equals(tag.getName())) {
            Optional<Tag> existingTag = loadTagPort.findByName(name);
            if (existingTag.isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + name);
            }
        }
        
        // 태그 정보 업데이트
        Tag updatedTag = tag.update(name, description);
        
        // 태그 저장
        return saveTagPort.update(updatedTag);
    }

    /**
     * 태그 동의어를 추가합니다.
     */
    @Transactional
    public Tag addSynonym(Long tagId, String synonym) {
        // 태그 조회
        Optional<Tag> tagOptional = loadTagPort.findById(tagId);
        if (tagOptional.isEmpty()) {
            throw new NoSuchElementException("태그를 찾을 수 없습니다: " + tagId);
        }
        
        // 동의어 추가
        return saveTagPort.addSynonym(tagId, synonym);
    }

    /**
     * ID로 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<Tag> getTagById(Long tagId) {
        return loadTagPort.findById(tagId);
    }

    /**
     * 이름으로 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<Tag> getTagByName(String name) {
        return loadTagPort.findByName(name);
    }

    /**
     * 여러 ID로 태그 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Tag> getTagsByIds(Set<Long> tagIds) {
        return loadTagPort.findByIds(tagIds);
    }

    /**
     * 모든 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Tag> getAllTags(int limit, int offset) {
        return loadTagPort.findAll(limit, offset);
    }

    /**
     * 특정 단어를 포함하는 태그를 검색합니다.
     */
    @Transactional(readOnly = true)
    public List<Tag> searchTags(String keyword, int limit) {
        return loadTagPort.search(keyword, limit);
    }

    /**
     * 인기 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Tag> getPopularTags(int limit) {
        return loadTagPort.findPopularTags(limit);
    }
}