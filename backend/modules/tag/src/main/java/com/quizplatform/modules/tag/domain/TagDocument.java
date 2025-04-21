package com.quizplatform.modules.tag.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

/**
 * 태그 검색을 위한 Elasticsearch 문서 클래스
 * 
 * <p>태그 엔티티의 검색 최적화 버전으로, Elasticsearch에 저장되어
 * 빠른 태그 검색과 자동 완성 기능을 지원합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Document(indexName = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagDocument {
    /**
     * 문서 ID (태그 ID와 동일)
     */
    @Id
    private String id;

    /**
     * 태그 이름 (텍스트 검색 최적화)
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    /**
     * 태그 설명 (텍스트 검색 최적화)
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    /**
     * 태그 동의어 목록
     */
    @Field(type = FieldType.Keyword)
    private Set<String> synonyms;

    /**
     * 상위 태그 ID
     */
    @Field(type = FieldType.Keyword)
    private String parentId;

    /**
     * 이 태그를 사용하는 퀴즈 수
     */
    @Field(type = FieldType.Integer)
    private int quizCount;

    /**
     * 이 태그 관련 퀴즈들의 평균 난이도
     */
    @Field(type = FieldType.Float)
    private float avgDifficulty;

    /**
     * 태그 엔티티로부터 검색 문서 생성
     * 
     * @param tag 변환할 태그 엔티티
     * @param quizCount 이 태그를 사용하는 퀴즈 수
     * @param avgDifficulty 태그 관련 퀴즈들의 평균 난이도
     * @return 생성된 태그 문서 객체
     */
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