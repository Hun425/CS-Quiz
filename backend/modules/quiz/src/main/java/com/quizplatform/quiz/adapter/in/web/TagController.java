package com.quizplatform.quiz.adapter.in.web;

import com.quizplatform.common.auth.CurrentUser;
import com.quizplatform.common.auth.CurrentUserInfo;
import com.quizplatform.quiz.application.dto.*;
import com.quizplatform.quiz.domain.model.Tag;
import com.quizplatform.quiz.domain.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag as SwaggerTag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 관리 REST 컨트롤러
 * 
 * <p>계층구조 태그 시스템의 관리자 전용 API를 제공합니다.
 * 태그 CRUD, 검색, 통계, 계층 관리 등의 기능을 포함합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@Slf4j
@SwaggerTag(name = "Tag Management", description = "태그 관리 API (관리자 전용)")
public class TagController {
    
    private final TagService tagService;
    
    // ===== 기본 CRUD 작업 =====
    
    @PostMapping
    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다. 관리자 권한이 필요합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "태그 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "409", description = "중복된 태그 이름")
    })
    public ResponseEntity<TagResponse> createTag(
            @RequestBody @Valid TagCreateRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Creating tag: name={}, parentId={}, userId={}", 
                request.name(), request.parentId(), currentUser.id());
        
        Tag createdTag = tagService.createTag(
                request.name(),
                request.description(),
                request.parentId(),
                currentUser.id()
        );
        
        TagResponse response = TagResponse.from(createdTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{tagId}")
    @Operation(summary = "태그 수정", description = "기존 태그의 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "태그 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "중복된 태그 이름")
    })
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long tagId,
            @RequestBody @Valid TagUpdateRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Updating tag: id={}, name={}, userId={}", 
                tagId, request.name(), currentUser.id());
        
        Tag updatedTag = tagService.updateTag(
                tagId,
                request.name(),
                request.description(),
                currentUser.id()
        );
        
        TagResponse response = TagResponse.from(updatedTag);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{tagId}")
    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다. 하위 태그나 연결된 퀴즈가 있으면 삭제할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "태그 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "삭제할 수 없는 태그 (하위 태그 또는 연결된 퀴즈 존재)")
    })
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long tagId,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Deleting tag: id={}, userId={}", tagId, currentUser.id());
        
        tagService.deleteTag(tagId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{tagId}/active")
    @Operation(summary = "태그 활성화/비활성화", description = "태그의 활성화 상태를 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    public ResponseEntity<TagResponse> setTagActive(
            @PathVariable Long tagId,
            @RequestParam boolean active,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Setting tag active: id={}, active={}, userId={}", 
                tagId, active, currentUser.id());
        
        Tag updatedTag = tagService.setTagActive(tagId, active, currentUser.id());
        TagResponse response = TagResponse.from(updatedTag);
        return ResponseEntity.ok(response);
    }
    
    // ===== 조회 작업 =====
    
    @GetMapping("/{tagId}")
    @Operation(summary = "태그 상세 조회", description = "특정 태그의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "태그 조회 성공"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    public ResponseEntity<TagResponse> getTag(@PathVariable Long tagId) {
        log.debug("Getting tag: id={}", tagId);
        
        return tagService.getTagById(tagId)
                .map(TagResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "태그 목록 조회", description = "태그 목록을 조회합니다. 다양한 필터 옵션을 제공합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "태그 목록 조회 성공")
    })
    public ResponseEntity<Page<TagResponse>> getTags(
            @Parameter(description = "계층 레벨 (0: 루트, 1: 1단계, 2: 2단계)")
            @RequestParam(required = false) Integer level,
            
            @Parameter(description = "활성화된 태그만 조회")
            @RequestParam(defaultValue = "true") boolean activeOnly,
            
            @Parameter(description = "부모 태그 ID")
            @RequestParam(required = false) Long parentId,
            
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Getting tags: level={}, activeOnly={}, parentId={}", level, activeOnly, parentId);
        
        List<Tag> tags;
        if (parentId != null) {
            tags = tagService.getChildrenTags(parentId);
        } else if (level != null) {
            tags = tagService.getTagsByLevel(level);
        } else {
            tags = activeOnly ? tagService.getAllActiveTags() : tagService.getFullHierarchy(null);
        }
        
        List<TagResponse> tagResponses = tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tagResponses.size());
        List<TagResponse> pagedTags = tagResponses.subList(start, end);
        
        Page<TagResponse> page = new PageImpl<>(pagedTags, pageable, tagResponses.size());
        return ResponseEntity.ok(page);
    }
    
    @GetMapping("/roots")
    @Operation(summary = "루트 태그 조회", description = "최상위 태그들을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "루트 태그 조회 성공")
    })
    public ResponseEntity<List<TagResponse>> getRootTags() {
        log.debug("Getting root tags");
        
        List<Tag> rootTags = tagService.getRootTags();
        List<TagResponse> responses = rootTags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    // ===== 계층구조 관련 =====
    
    @GetMapping("/{tagId}/children")
    @Operation(summary = "자식 태그 조회", description = "특정 태그의 직계 자식 태그들을 조회합니다.")
    public ResponseEntity<List<TagResponse>> getChildrenTags(@PathVariable Long tagId) {
        log.debug("Getting children tags: parentId={}", tagId);
        
        List<Tag> children = tagService.getChildrenTags(tagId);
        List<TagResponse> responses = children.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{tagId}/descendants")
    @Operation(summary = "후손 태그 조회", description = "특정 태그의 모든 후손 태그들을 조회합니다.")
    public ResponseEntity<List<TagResponse>> getDescendants(@PathVariable Long tagId) {
        log.debug("Getting descendants: tagId={}", tagId);
        
        List<Tag> descendants = tagService.getAllDescendants(tagId);
        List<TagResponse> responses = descendants.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{tagId}/ancestors")
    @Operation(summary = "조상 태그 조회", description = "특정 태그의 모든 조상 태그들을 조회합니다.")
    public ResponseEntity<List<TagResponse>> getAncestors(@PathVariable Long tagId) {
        log.debug("Getting ancestors: tagId={}", tagId);
        
        List<Tag> ancestors = tagService.getAllAncestors(tagId);
        List<TagResponse> responses = ancestors.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @PatchMapping("/{tagId}/move")
    @Operation(summary = "태그 이동", description = "태그를 다른 부모 하위로 이동합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "태그 이동 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이동 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이동할 수 없는 위치")
    })
    public ResponseEntity<TagResponse> moveTag(
            @PathVariable Long tagId,
            @RequestBody TagMoveRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        log.info("Moving tag: id={}, newParentId={}, userId={}", 
                tagId, request.newParentId(), currentUser.id());
        
        Tag movedTag = tagService.moveTag(tagId, request.newParentId(), currentUser.id());
        TagResponse response = TagResponse.from(movedTag);
        return ResponseEntity.ok(response);
    }
    
    // ===== 검색 기능 =====
    
    @GetMapping("/search")
    @Operation(summary = "태그 검색", description = "키워드로 태그를 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    public ResponseEntity<List<TagResponse>> searchTags(
            @Parameter(description = "검색 키워드")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "계층 레벨 필터")
            @RequestParam(required = false) Integer level,
            
            @Parameter(description = "활성화된 태그만 검색")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.debug("Searching tags: keyword={}, level={}, activeOnly={}", keyword, level, activeOnly);
        
        List<Tag> searchResults = tagService.advancedSearch(keyword, level, activeOnly);
        List<TagResponse> responses = searchResults.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    // ===== 통계 및 분석 =====
    
    @GetMapping("/stats/popular")
    @Operation(summary = "인기 태그 조회", description = "사용 횟수 기준 인기 태그를 조회합니다.")
    public ResponseEntity<List<TagResponse>> getPopularTags(
            @Parameter(description = "조회할 태그 수")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting popular tags: limit={}", limit);
        
        List<Tag> popularTags = tagService.getPopularTags(limit);
        List<TagResponse> responses = popularTags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/stats/recent")
    @Operation(summary = "최근 태그 조회", description = "최근 생성된 태그를 조회합니다.")
    public ResponseEntity<List<TagResponse>> getRecentTags(
            @Parameter(description = "조회할 태그 수")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting recent tags: limit={}", limit);
        
        List<Tag> recentTags = tagService.getRecentTags(limit);
        List<TagResponse> responses = recentTags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/stats/unused")
    @Operation(summary = "미사용 태그 조회", description = "사용되지 않는 태그들을 조회합니다.")
    public ResponseEntity<List<TagResponse>> getUnusedTags() {
        log.debug("Getting unused tags");
        
        List<Tag> unusedTags = tagService.getUnusedTags();
        List<TagResponse> responses = unusedTags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{tagId}/stats")
    @Operation(summary = "태그 상세 통계", description = "특정 태그의 상세 사용 통계를 조회합니다.")
    public ResponseEntity<TagStatsResponse> getTagStats(@PathVariable Long tagId) {
        log.debug("Getting tag stats: tagId={}", tagId);
        
        TagService.TagUsageStats stats = tagService.getTagUsageStats(tagId);
        TagStatsResponse response = TagStatsResponse.from(stats, 0); // rank는 별도 계산 필요
        
        return ResponseEntity.ok(response);
    }
    
    // ===== 관리자 전용 기능 =====
    
    @GetMapping("/hierarchy")
    @Operation(summary = "전체 계층구조 조회", description = "모든 태그의 계층구조를 조회합니다. (관리자 전용)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계층구조 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요")
    })
    public ResponseEntity<List<TagResponse>> getFullHierarchy(@CurrentUser CurrentUserInfo currentUser) {
        log.debug("Getting full hierarchy: userId={}", currentUser.id());
        
        List<Tag> hierarchy = tagService.getFullHierarchy(currentUser.id());
        List<TagResponse> responses = hierarchy.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
}