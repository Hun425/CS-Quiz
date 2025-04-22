package application.service;

import application.port.out.LoadTagPort;
import application.port.out.SaveTagPort;
import domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * 태그 생성, 수정, 조회 등 태그 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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
        Tag tag = loadTagPort.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("태그를 찾을 수 없습니다: " + tagId));
        
        // 동의어 추가 (도메인 객체 메소드 호출)
        tag.addSynonym(synonym); 
        
        // 변경된 태그 저장 (update 사용)
        return saveTagPort.update(tag); // saveTagPort.addSynonym 대신 update 사용
    }

    /**
     * ID로 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Tag getTagById(Long tagId) {
        return loadTagPort.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("태그를 찾을 수 없습니다: " + tagId));
    }

    /**
     * Tag 저장
     */
    @Transactional
    public Tag saveTag(Tag tag) {
        return saveTagPort.save(tag);
    }
    
    /**
     * 태그를 삭제합니다.
     */
    @Transactional
    public void deleteTag(Long tagId) {
        // 태그 조회
        Tag tag = loadTagPort.findById(tagId)
                .orElseThrow(() -> new NoSuchElementException("태그를 찾을 수 없습니다: " + tagId));
        
        // 태그 삭제
        saveTagPort.delete(tagId);
    }
    
    /**
     * 이름으로 태그를 검색합니다.
     */
    @Transactional(readOnly = true)
    public Page<Tag> searchTagsByName(String name, Pageable pageable) {
        return loadTagPort.findByNameContaining(name, pageable);
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
    public Set<Tag> getTagsByIds(Set<Long> tagIds) {
        return loadTagPort.findByIds(tagIds);
    }

    /**
     * 모든 태그를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<Tag> getAllTags(Pageable pageable) {
        return loadTagPort.findAll(pageable);
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