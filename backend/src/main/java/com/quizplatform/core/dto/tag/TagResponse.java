package com.quizplatform.core.dto.tag;

import com.quizplatform.core.domain.tag.Tag;
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

    public static TagResponse from(Tag tag) {
        // 지연 로딩된 컬렉션을 명시적으로 초기화하고 일반 Java Set으로 변환
        Set<String> synonymsSet = new HashSet<>();
        if (tag.getSynonyms() != null) {
            // Hibernate 프록시 컬렉션을 초기화하고 새 컬렉션으로 복사
            Hibernate.initialize(tag.getSynonyms());
            synonymsSet.addAll(tag.getSynonyms());
        }

        // 퀴즈 컬렉션도 초기화
        Hibernate.initialize(tag.getQuizzes());

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .quizCount(tag.getQuizzes() != null ? tag.getQuizzes().size() : 0)
                .synonyms(synonymsSet) // 일반 Java Set 사용
                .build();
    }
}