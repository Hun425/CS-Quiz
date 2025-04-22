package domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 태그 도메인 모델
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class Tag {
    private final Long id;
    private String name;
    private String description;
    private final Long parentId;
    private final Set<String> synonyms;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Tag(
            Long id,
            String name,
            String description,
            Long parentId,
            Set<String> synonyms,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.synonyms = synonyms != null ? synonyms : new HashSet<>();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
    }

    /**
     * 태그 정보 업데이트
     */
    public Tag update(String name, String description) {
        return Tag.builder()
                .id(this.id)
                .name(name != null ? name : this.name)
                .description(description != null ? description : this.description)
                .parentId(this.parentId)
                .synonyms(new HashSet<>(this.synonyms))
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 태그 동의어 추가
     */
    public Tag addSynonym(String synonym) {
        Set<String> updatedSynonyms = new HashSet<>(this.synonyms);
        updatedSynonyms.add(synonym);
        
        return Tag.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .parentId(this.parentId)
                .synonyms(updatedSynonyms)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 새로운 하위 태그 생성
     */
    public Tag createChild(String childName, String childDescription) {
        return Tag.builder()
                .name(childName)
                .description(childDescription)
                .parentId(this.id)
                .build();
    }
    
    /**
     * 이름 설정
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 설명 설정
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 업데이트 시간 설정
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}