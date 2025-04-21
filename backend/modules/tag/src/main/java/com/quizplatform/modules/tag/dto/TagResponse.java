package com.quizplatform.modules.tag.dto;

import com.quizplatform.modules.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor // 기본 생성자 추가 - JSON 직렬화/역직렬화에 필요
@AllArgsConstructor // 모든 필드를 포함하는 생성자 추가 - Builder 패턴에 필요
public class TagResponse {
    private Long id;
    private String name;
    private String description;
    private int quizCount;
    private Set<String> synonyms;
    private Long parentId; // 부모 태그 ID 추가

    public static TagResponse from(Tag tag, int quizCount) {
        // 지연 로딩된 컬렉션을 명시적으로 초기화하고 일반 Java Set으로 변환
        Set<String> synonymsSet = new HashSet<>();
        if (tag.getSynonyms() != null) {
            // Hibernate 프록시 컬렉션을 초기화하고 새 컬렉션으로 복사
            Hibernate.initialize(tag.getSynonyms());
            synonymsSet.addAll(tag.getSynonyms());
        }

        // 부모 태그 ID 처리
        Long parentTagId = null;
        if (tag.getParent() != null) {
            parentTagId = tag.getParent().getId();
        }

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .quizCount(quizCount)
                .synonyms(synonymsSet) // 일반 Java Set 사용
                .parentId(parentTagId) // 부모 태그 ID 추가
                .build();
    }

    // 기존 메서드도 유지 (하위 호환성)
    public static TagResponse from(Tag tag) {
        // 퀴즈 카운트 없이 변환 (0으로 설정)
        return from(tag, 0);
    }
}