package com.quizplatform.core.domain.tag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

// 태그 검색을 위한 Elasticsearch 문서
@Document(indexName = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private Set<String> synonyms;

    @Field(type = FieldType.Keyword)
    private String parentId;

    @Field(type = FieldType.Integer)
    private int quizCount;

    @Field(type = FieldType.Float)
    private float avgDifficulty;

    public static TagDocument from(Tag tag, int quizCount, float avgDifficulty) {
        TagDocument document = new TagDocument();
        document.id = tag.getId().toString();
        document.name = tag.getName();
        document.description = tag.getDescription();
        document.synonyms = tag.getSynonyms();
        document.parentId = tag.getParent() != null ? tag.getParent().getId().toString() : null;
        document.quizCount = quizCount;
        document.avgDifficulty = avgDifficulty;
        return document;
    }
}