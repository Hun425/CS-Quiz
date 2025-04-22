package adapter.in.web;

import adapter.in.web.dto.TagResponse;
import application.service.TagService;
import domain.model.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 컨트롤러 클래스
 * 태그 생성, 조회, 수정, 삭제 등의 API 제공
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 태그 생성을 위한 요청 데이터를 담는 내부 DTO 클래스
     *
     * @author 채기훈
     * @since JDK 17.0.2 Eclipse Temurin
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateTagRequest {
        @NotBlank(message = "태그명은 필수 항목입니다")
        private String name;
        
        private String description;
    }

    /**
     * 태그 생성 API
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        log.info("Creating tag: {}", request.getName());
        
        Tag tag = Tag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();
        
        Tag createdTag = tagService.saveTag(tag);
        TagResponse response = mapToTagResponse(createdTag);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 태그 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTag(@PathVariable Long id) {
        log.info("Fetching tag with ID: {}", id);
        
        Tag tag = tagService.getTagById(id);
        TagResponse response = mapToTagResponse(tag);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<Page<TagResponse>> getTags(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Fetching tags with pageable: {}", pageable);
        
        Page<Tag> tags = tagService.getAllTags(pageable);
        Page<TagResponse> response = tags.map(this::mapToTagResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TagResponse>> searchTags(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Searching tags with name: {}", name);
        
        Page<Tag> tags = tagService.searchTagsByName(name, pageable);
        Page<TagResponse> response = tags.map(this::mapToTagResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 태그 조회 API
     */
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching popular tags with limit: {}", limit);
        
        List<Tag> popularTags = tagService.getPopularTags(limit);
        List<TagResponse> response = popularTags.stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 수정 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody CreateTagRequest request) {
        log.info("Updating tag with ID: {}", id);
        
        Tag existingTag = tagService.getTagById(id);
        existingTag.setName(request.getName());
        existingTag.setDescription(request.getDescription());
        existingTag.setUpdatedAt(LocalDateTime.now());
        
        Tag updatedTag = tagService.saveTag(existingTag);
        TagResponse response = mapToTagResponse(updatedTag);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("Deleting tag with ID: {}", id);
        
        tagService.deleteTag(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * 도메인 객체를 태그 응답 DTO로 변환
     */
    private TagResponse mapToTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                // 퀴즈 수는 선택적으로 설정 가능
                .build();
    }
}
