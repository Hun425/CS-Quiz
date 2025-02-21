package com.quizplatform.core.domain.tag;


import com.quizplatform.core.domain.quiz.Quiz;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    @OneToMany(mappedBy = "parent")
    private Set<Tag> children = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Quiz> quizzes = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "tag_synonyms",
            joinColumns = @JoinColumn(name = "tag_id")
    )
    @Column(name = "synonym")
    private Set<String> synonyms = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
        this.synonyms = new HashSet<>();
    }

    // 태그 계층 구조 관리 메서드
    public void addChild(Tag child) {
        if (child.getParent() != null) {
            child.getParent().getChildren().remove(child);
        }
        child.setParent(this);
        this.children.add(child);
    }

    // 동의어 관리
    public void addSynonym(String synonym) {
        this.synonyms.add(synonym);
    }

    // setter 메서드들
    protected void setParent(Tag parent) {
        this.parent = parent;
    }
}