package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.auth.CurrentUserInfo;
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
import java.util.stream.Stream;

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
    
    // ===== 기본 CRUD 작업 =====
    
    @Override
    @Transactional
    public Tag createTag(String name, String description, Long parentId, CurrentUserInfo currentUser) {
        log.info("Creating new tag: name={}, parentId={}, userId={}", name, parentId, currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
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
    public Tag updateTag(Long tagId, String name, String description, CurrentUserInfo currentUser) {
        log.info("Updating tag: id={}, name={}, userId={}", tagId, name, currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
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
    public void deleteTag(Long tagId, CurrentUserInfo currentUser) {
        log.info("Deleting tag: id={}, userId={}", tagId, currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
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
    public Tag setTagActive(Long tagId, boolean active, CurrentUserInfo currentUser) {
        log.info("Setting tag active status: id={}, active={}, userId={}", tagId, active, currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
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
    public Tag moveTag(Long tagId, Long newParentId, CurrentUserInfo currentUser) {
        log.info("Moving tag: id={}, newParentId={}, userId={}", tagId, newParentId, currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
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
        
        // Stream API를 활용한 효율적인 퀴즈 수 계산
        int connectedQuizCount = tagRepository.getTagQuizCounts().stream()
                .filter(row -> tagId.equals(row[0]))
                .findFirst()
                .map(row -> ((Number) row[1]).intValue())
                .orElse(0);
        
        // 후손 태그 수 계산
        int descendantCount = getAllDescendants(tagId).size();
        
        return new TagUsageStats(
                tag.getId(),
                tag.getName(),
                tag.getFullPath(),
                tag.getUsageCount(),
                connectedQuizCount,
                descendantCount
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
    public List<Tag> getFullHierarchy(CurrentUserInfo currentUser) {
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
        return tagRepository.findAll();
    }
    
    @Override
    @Transactional
    public TagImportResult importTags(List<TagImportData> tags, CurrentUserInfo currentUser) {
        log.info("Importing {} tags, userId={}", tags.size(), currentUser.id());
        
        // 관리자 권한 체크
        validateAdminPermission(currentUser);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<String> errorMessages = new ArrayList<>();
        
        // Stream API를 활용한 태그 가져오기 처리
        tags.stream().forEach(tagData -> {
            try {
                // 부모 태그 찾기
                Tag parent = findParentByPath(tagData.parentPath());
                Long parentId = Optional.ofNullable(parent).map(Tag::getId).orElse(null);
                
                // 중복 체크 후 태그 생성
                Optional.of(tagData)
                        .filter(data -> !tagRepository.existsByNameAndParent(data.name(), parentId))
                        .map(data -> {
                            Tag newTag = Tag.builder()
                                    .name(data.name())
                                    .description(data.description())
                                    .parent(parent)
                                    .build();
                            newTag.setActive(data.active());
                            return tagRepository.save(newTag);
                        })
                        .ifPresentOrElse(
                            tag -> successCount.incrementAndGet(),
                            () -> {
                                errorMessages.add("중복된 태그: " + tagData.name());
                                errorCount.incrementAndGet();
                            }
                        );
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
     * 관리자 권한 검증
     * 
     * <p>태그 관리 권한을 확인합니다. 다음 역할을 가진 사용자만 허용됩니다:</p>
     * <ul>
     *   <li>ADMIN - 시스템 관리자</li>
     *   <li>QUIZ_ADMIN - 퀴즈 관리자</li>
     *   <li>OWNER - 시스템 소유자</li>
     * </ul>
     * 
     * @param currentUser 현재 사용자 정보
     * @throws BusinessException 로그인하지 않았거나 관리자 권한이 없는 경우
     */
    private void validateAdminPermission(CurrentUserInfo currentUser) {
        if (currentUser == null || currentUser.id() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        // 관리자 권한 체크 (ADMIN, QUIZ_ADMIN, OWNER 역할 허용)
        boolean hasAdminPermission = currentUser.hasRole("ADMIN") || 
                                   currentUser.hasRole("QUIZ_ADMIN") || 
                                   currentUser.hasRole("OWNER");
        
        if (!hasAdminPermission) {
            log.warn("Unauthorized tag management attempt by user: {} with roles: {}", 
                    currentUser.id(), currentUser.roles());
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                                      "태그 관리 권한이 없습니다. 관리자 권한이 필요합니다.");
        }
        
        log.debug("Admin permission validated for user: {} with roles: {}", 
                 currentUser.id(), currentUser.roles());
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
     * 
     * <p>경로는 "루트태그명 > 자식태그명 > 손자태그명" 형식으로 구성됩니다.</p>
     * 
     * @param parentPath 태그 경로 문자열
     * @return 찾은 부모 태그 (없으면 null)
     */
    private Tag findParentByPath(String parentPath) {
        if (parentPath == null || parentPath.trim().isEmpty()) {
            return null;
        }
        
        String[] pathParts = parentPath.split(">");
        Tag currentTag = null;
        
        // 경로를 단계적으로 탐색
        for (String tagName : pathParts) {
            String trimmedName = tagName.trim();
            if (trimmedName.isEmpty()) {
                continue;
            }
            
            if (currentTag == null) {
                // 루트 태그 찾기
                currentTag = tagRepository.findByNameIgnoreCase(trimmedName)
                        .filter(tag -> tag.getParent() == null)
                        .orElse(null);
            } else {
                // 자식 태그 찾기
                Long parentId = currentTag.getId();
                currentTag = tagRepository.findByNameIgnoreCase(trimmedName)
                        .filter(tag -> tag.getParent() != null && 
                                     parentId.equals(tag.getParent().getId()))
                        .orElse(null);
            }
            
            if (currentTag == null) {
                break; // 경로에서 태그를 찾지 못하면 중단
            }
        }
        
        return currentTag;
    }
}