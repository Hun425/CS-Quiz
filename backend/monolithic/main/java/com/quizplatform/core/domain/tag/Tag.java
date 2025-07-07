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

/**
 * 태그 엔티티 클래스
 * 
 * <p>퀴즈의 주제나 카테고리를 분류하는 태그 정보를 관리합니다.
 * 계층 구조와 동의어를 지원하여 효과적인 퀴즈 분류와 검색을 가능하게 합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 태그 이름 (고유값)
     */
    @Column(unique = true)
    private String name;

    /**
     * 태그 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 상위 태그 (계층 구조 형성)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;

    /**
     * 하위 태그 목록
     */
    @OneToMany(mappedBy = "parent")
    private Set<Tag> children = new HashSet<>();

    /**
     * 이 태그를 사용하는 퀴즈 목록
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Quiz> quizzes = new HashSet<>();

    /**
     * 태그 동의어 목록
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tag_synonyms",
            joinColumns = @JoinColumn(name = "tag_id")
    )
    @Column(name = "synonym")
    private Set<String> synonyms = new HashSet<>();

    /**
     * 태그 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 태그 생성자
     * 
     * @param name 태그 이름
     * @param description 태그 설명
     */
    @Builder
    public Tag(String name, String description) {
        this.name = name;
        this.description = description;
        this.synonyms = new HashSet<>();
    }

    /**
     * 하위 태그 추가
     * 
     * <p>기존에 다른 상위 태그에 속한 경우 관계를 재설정합니다.</p>
     * 
     * @param child 하위 태그로 추가할 태그
     */
    public void addChild(Tag child) {
        if (child.getParent() != null) {
            child.getParent().getChildren().remove(child);
        }
        child.setParent(this);
        this.children.add(child);
    }

    /**
     * 태그 동의어 추가
     * 
     * @param synonym 추가할 동의어
     */
    public void addSynonym(String synonym) {
        this.synonyms.add(synonym);
    }

    /**
     * 상위 태그 설정
     * 
     * @param parent 상위 태그
     */
    public void setParent(Tag parent) {
        this.parent = parent;
    }
}