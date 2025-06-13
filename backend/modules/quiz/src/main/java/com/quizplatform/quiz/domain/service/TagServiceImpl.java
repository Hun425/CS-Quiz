package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.quiz.adapter.out.persistence.repository.TagRepository;
import com.quizplatform.quiz.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 태그 도메인 서비스 구현체
 * 
 * <p>태그 관리의 모든 비즈니스 로직을 처리합니다.
 * 관리자 권한 체크, 계층구조 검증, 데이터 일관성 보장 등을 담당합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {
    
    private final TagRepository tagRepository;
    // TODO: UserService 연동으로 관리자 권한 체크 (현재는 임시로 모든 사용자 허용)
    
    // ===== 기본 CRUD 작업 =====
    
    @Override
    @Transactional
    public Tag createTag(String name, String description, Long parentId, Long currentUserId) {
        log.info("Creating new tag: name={}, parentId={}, userId={}", name, parentId, currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        // 부모 태그 조회 및 검증
        Tag parent = null;
        if (parentId != null) {
            parent = tagRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                           "부모 태그를 찾을 수 없습니다: " + parentId));
            
            if (!parent.isActive()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                          "비활성화된 태그의 하위에는 새 태그를 생성할 수 없습니다.");
            }
        }
        
        // 같은 부모 하위에서 이름 중복 체크
        if (tagRepository.existsByNameAndParent(name, parentId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTITY, 
                                      "같은 부모 하위에 동일한 이름의 태그가 이미 존재합니다: " + name);
        }
        
        // 태그 생성
        Tag newTag = Tag.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .parent(parent)
                .build();
        
        Tag savedTag = tagRepository.save(newTag);
        log.info("Tag created successfully: id={}, name={}, level={}", 
                savedTag.getId(), savedTag.getName(), savedTag.getLevel());
        
        return savedTag;
    }
    
    @Override
    @Transactional
    public Tag updateTag(Long tagId, String name, String description, Long currentUserId) {
        log.info("Updating tag: id={}, name={}, userId={}", tagId, name, currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        // 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        // 이름 변경 시 중복 체크
        if (!tag.getName().equalsIgnoreCase(name.trim())) {
            Long parentId = tag.getParent() != null ? tag.getParent().getId() : null;
            if (tagRepository.existsByNameAndParentExcludingId(name, parentId, tagId)) {
                throw new BusinessException(ErrorCode.DUPLICATE_ENTITY, 
                                          "같은 부모 하위에 동일한 이름의 태그가 이미 존재합니다: " + name);
            }
        }
        
        // 태그 정보 업데이트
        tag.update(name.trim(), description != null ? description.trim() : null);
        
        Tag updatedTag = tagRepository.save(tag);
        log.info("Tag updated successfully: id={}, name={}", updatedTag.getId(), updatedTag.getName());
        
        return updatedTag;
    }
    
    @Override
    @Transactional
    public void deleteTag(Long tagId, Long currentUserId) {
        log.info("Deleting tag: id={}, userId={}", tagId, currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        // 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        // 삭제 가능 여부 체크
        if (tagRepository.existsByParentId(tagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                      "하위 태그가 있는 태그는 삭제할 수 없습니다. 먼저 하위 태그를 처리해주세요.");
        }
        
        if (tagRepository.hasConnectedQuizzes(tagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                      "퀴즈와 연결된 태그는 삭제할 수 없습니다. 먼저 연결을 해제해주세요.");
        }
        
        // 태그 삭제
        tagRepository.delete(tag);
        log.info("Tag deleted successfully: id={}, name={}", tagId, tag.getName());
    }
    
    @Override
    @Transactional
    public Tag setTagActive(Long tagId, boolean active, Long currentUserId) {
        log.info("Setting tag active status: id={}, active={}, userId={}", tagId, active, currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        // 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        // 활성화 상태 변경
        tag.setActive(active);
        
        Tag updatedTag = tagRepository.save(tag);
        log.info("Tag active status updated: id={}, active={}", tagId, active);
        
        return updatedTag;
    }
    
    // ===== 조회 작업 =====
    
    @Override
    public Optional<Tag> getTagById(Long tagId) {
        return tagRepository.findById(tagId);
    }
    
    @Override
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByNameIgnoreCase(name);
    }
    
    @Override
    public List<Tag> getAllActiveTags() {
        return tagRepository.findByActiveTrue();
    }
    
    @Override
    public List<Tag> getTagsByLevel(int level) {
        return tagRepository.findByLevelAndActiveTrue(level);
    }
    
    // ===== 계층구조 관련 =====
    
    @Override
    public List<Tag> getRootTags() {
        return tagRepository.findByParentIsNullAndActiveTrue();
    }
    
    @Override
    public List<Tag> getChildrenTags(Long parentId) {
        return tagRepository.findByParentIdAndActiveTrue(parentId);
    }
    
    @Override
    public List<Tag> getAllDescendants(Long tagId) {
        return tagRepository.findAllDescendants(tagId);
    }
    
    @Override
    public List<Tag> getAllAncestors(Long tagId) {
        return tagRepository.findAllAncestors(tagId);
    }
    
    @Override
    @Transactional
    public Tag moveTag(Long tagId, Long newParentId, Long currentUserId) {
        log.info("Moving tag: id={}, newParentId={}, userId={}", tagId, newParentId, currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        // 태그 조회
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        // 새 부모 태그 조회 및 검증
        Tag newParent = null;
        if (newParentId != null) {
            newParent = tagRepository.findById(newParentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                           "새 부모 태그를 찾을 수 없습니다: " + newParentId));
            
            // 자기 자신이나 후손을 부모로 설정하는 것 방지
            if (tagId.equals(newParentId) || isDescendantOf(newParentId, tagId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                          "태그를 자기 자신이나 후손의 하위로 이동할 수 없습니다.");
            }
        }
        
        // 새 부모 하위에서 이름 중복 체크
        if (tagRepository.existsByNameAndParentExcludingId(tag.getName(), newParentId, tagId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTITY, 
                                      "새 부모 하위에 동일한 이름의 태그가 이미 존재합니다: " + tag.getName());
        }
        
        // 태그 이동
        tag.setParent(newParent);
        
        Tag movedTag = tagRepository.save(tag);
        log.info("Tag moved successfully: id={}, newLevel={}", tagId, movedTag.getLevel());
        
        return movedTag;
    }
    
    // ===== 검색 기능 =====
    
    @Override
    public List<Tag> searchTags(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveTags();
        }
        
        return tagRepository.searchByKeyword(keyword.trim());
    }
    
    @Override
    public List<Tag> advancedSearch(String keyword, Integer level, boolean activeOnly) {
        List<Tag> allTags = activeOnly ? tagRepository.findByActiveTrue() : tagRepository.findAll();
        
        return allTags.stream()
                .filter(tag -> keyword == null || keyword.trim().isEmpty() || 
                             tag.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                             (tag.getDescription() != null && 
                              tag.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(tag -> level == null || tag.getLevel() == level)
                .sorted((t1, t2) -> {
                    // 사용량 순, 이름 순 정렬
                    int usageComparison = Integer.compare(t2.getUsageCount(), t1.getUsageCount());
                    return usageComparison != 0 ? usageComparison : t1.getName().compareTo(t2.getName());
                })
                .collect(Collectors.toList());
    }
    
    // ===== 통계 및 분석 =====
    
    @Override
    public List<Tag> getPopularTags(int limit) {
        List<Tag> popularTags = tagRepository.findTop10ByActiveTrueOrderByUsageCountDesc();
        return popularTags.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Tag> getRecentTags(int limit) {
        List<Tag> recentTags = tagRepository.findTop10ByActiveTrueOrderByCreatedAtDesc();
        return recentTags.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Tag> getUnusedTags() {
        return tagRepository.findByUsageCountAndActiveTrue(0);
    }
    
    @Override
    public TagUsageStats getTagUsageStats(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        List<Object[]> quizCounts = tagRepository.getTagQuizCounts();
        int connectedQuizCount = quizCounts.stream()
                .filter(row -> tagId.equals(row[0]))
                .mapToInt(row -> ((Number) row[1]).intValue())
                .findFirst()
                .orElse(0);
        
        List<Tag> descendants = getAllDescendants(tagId);
        
        return new TagUsageStats(
                tag.getId(),
                tag.getName(),
                tag.getFullPath(),
                tag.getUsageCount(),
                connectedQuizCount,
                descendants.size()
        );
    }
    
    // ===== 퀴즈와의 연동 =====
    
    @Override
    public List<Tag> getQuizTags(Long quizId) {
        return tagRepository.findByQuizId(quizId);
    }
    
    @Override
    public boolean canAddTagToQuiz(Long quizId, Long tagId) {
        // 퀴즈의 현재 태그 수 체크
        List<Tag> currentTags = getQuizTags(quizId);
        if (currentTags.size() >= Tag.MAX_TAGS_PER_QUIZ) {
            return false;
        }
        
        // 태그가 활성화 상태인지 체크
        Optional<Tag> tag = getTagById(tagId);
        return tag.isPresent() && tag.get().isActive();
    }
    
    // ===== 관리자 전용 기능 =====
    
    @Override
    public List<Tag> getFullHierarchy(Long currentUserId) {
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        return tagRepository.findAll();
    }
    
    @Override
    @Transactional
    public TagImportResult importTags(List<TagImportData> tags, Long currentUserId) {
        log.info("Importing {} tags, userId={}", tags.size(), currentUserId);
        
        // 관리자 권한 체크
        validateAdminPermission(currentUserId);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<String> errorMessages = new ArrayList<>();
        
        // 태그 가져오기 처리
        tags.forEach(tagData -> {
            try {
                // 부모 태그 찾기
                Tag parent = findParentByPath(tagData.parentPath());
                
                // 중복 체크
                Long parentId = parent != null ? parent.getId() : null;
                if (!tagRepository.existsByNameAndParent(tagData.name(), parentId)) {
                    Tag newTag = Tag.builder()
                            .name(tagData.name())
                            .description(tagData.description())
                            .parent(parent)
                            .build();
                    
                    newTag.setActive(tagData.active());
                    tagRepository.save(newTag);
                    successCount.incrementAndGet();
                } else {
                    errorMessages.add("중복된 태그: " + tagData.name());
                    errorCount.incrementAndGet();
                }
            } catch (Exception e) {
                errorMessages.add("태그 생성 실패 (" + tagData.name() + "): " + e.getMessage());
                errorCount.incrementAndGet();
            }
        });
        
        TagImportResult result = new TagImportResult(
                tags.size(),
                successCount.get(),
                errorCount.get(),
                errorMessages
        );
        
        log.info("Tag import completed: total={}, success={}, errors={}", 
                result.totalProcessed(), result.successCount(), result.errorCount());
        
        return result;
    }
    
    // ===== 내부 헬퍼 메서드 =====
    
    /**
     * 관리자 권한 검증 (임시 구현)
     */
    private void validateAdminPermission(Long currentUserId) {
        // TODO: UserService와 연동하여 실제 관리자 권한 체크
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        // 임시로 모든 로그인한 사용자를 관리자로 처리
        log.debug("Admin permission validated for user: {}", currentUserId);
    }
    
    /**
     * 태그가 다른 태그의 후손인지 확인
     */
    private boolean isDescendantOf(Long potentialDescendantId, Long ancestorId) {
        List<Tag> descendants = getAllDescendants(ancestorId);
        return descendants.stream()
                .anyMatch(tag -> potentialDescendantId.equals(tag.getId()));
    }
    
    /**
     * 경로 문자열로 부모 태그 찾기
     */
    private Tag findParentByPath(String parentPath) {
        if (parentPath == null || parentPath.trim().isEmpty()) {
            return null;
        }
        
        // 경로를 ">" 로 분리하여 단계별로 태그 찾기
        String[] pathComponents = parentPath.split(">");
        Tag currentTag = null;
        
        for (String component : pathComponents) {
            String tagName = component.trim();
            Long parentId = currentTag != null ? currentTag.getId() : null;
            
            currentTag = tagRepository.findByNameIgnoreCase(tagName)
                    .filter(tag -> {
                        if (parentId == null) {
                            return tag.getParent() == null;
                        } else {
                            return tag.getParent() != null && parentId.equals(tag.getParent().getId());
                        }
                    })
                    .orElse(null);
            
            if (currentTag == null) {
                break;
            }
        }
        
        return currentTag;
    }
}