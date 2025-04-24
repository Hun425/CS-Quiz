package com.quizplatform.core.controller.tag;

import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.common.PageResponse;
import com.quizplatform.core.dto.tag.TagCreateRequest;
import com.quizplatform.core.dto.tag.TagResponse;
import com.quizplatform.core.service.tag.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "태그 API", description = "퀴즈 태그 관련 API")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "모든 태그 조회", description = "모든 태그 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(CommonApiResponse.success(tags));
    }

    @Operation(summary = "태그 검색", description = "태그를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 검색 결과 조회 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<CommonApiResponse<PageResponse<TagResponse>>> searchTags(
            @Parameter(description = "검색할 태그 이름") @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<TagResponse> tags = tagService.searchTags(name, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(tags));
    }

    @Operation(summary = "태그 상세 조회", description = "특정 태그의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없습니다.")
    })
    @GetMapping("/{tagId}")
    public ResponseEntity<CommonApiResponse<TagResponse>> getTag(
            @Parameter(description = "조회할 태그 ID") @PathVariable Long tagId) {
        TagResponse tag = tagService.getTag(tagId);
        return ResponseEntity.ok(CommonApiResponse.success(tag));
    }

    @Operation(summary = "루트 태그 조회", description = "최상위 태그 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "루트 태그 조회 성공")
    })
    @GetMapping("/roots")
    public ResponseEntity<CommonApiResponse<List<TagResponse>>> getRootTags() {
        List<TagResponse> rootTags = tagService.getRootTags();
        return ResponseEntity.ok(CommonApiResponse.success(rootTags));
    }

    @Operation(summary = "인기 태그 조회", description = "가장 많이 사용된 태그 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 태그 조회 성공")
    })
    @GetMapping("/popular")
    public ResponseEntity<CommonApiResponse<List<TagResponse>>> getPopularTags(
            @Parameter(description = "반환할 태그 수") @RequestParam(defaultValue = "10") int limit) {
        List<TagResponse> popularTags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(CommonApiResponse.success(popularTags));
    }

    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다.")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<TagResponse>> createTag(
            @RequestBody TagCreateRequest request) {
        TagResponse createdTag = tagService.createTag(request);
        return ResponseEntity.ok(CommonApiResponse.success(createdTag));
    }

    @Operation(summary = "태그 수정", description = "기존 태그를 수정합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없습니다.")
    })
    @PutMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<TagResponse>> updateTag(
            @Parameter(description = "수정할 태그 ID") @PathVariable Long tagId,
            @RequestBody TagCreateRequest request) {
        TagResponse updatedTag = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(CommonApiResponse.success(updatedTag));
    }

    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonApiResponse<Void>> deleteTag(
            @Parameter(description = "삭제할 태그 ID") @PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok(CommonApiResponse.success(null, "태그가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "자식 태그 조회", description = "특정 태그의 자식 태그 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "자식 태그 조회 성공"),
            @ApiResponse(responseCode = "404", description = "부모 태그를 찾을 수 없습니다.")
    })
    @GetMapping("/{parentId}/children")
    public ResponseEntity<CommonApiResponse<List<TagResponse>>> getChildTags(
            @Parameter(description = "부모 태그 ID") @PathVariable Long parentId) {
        List<TagResponse> childTags = tagService.getChildTags(parentId);
        return ResponseEntity.ok(CommonApiResponse.success(childTags));
    }
}