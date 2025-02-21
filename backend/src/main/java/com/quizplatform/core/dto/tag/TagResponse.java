package com.quizplatform.core.dto.tag;

import com.quizplatform.core.domain.tag.Tag;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;
@Getter
@Builder
public class TagResponse {
    private Long id;
    private String name;
    private String description;
    private int quizCount;
    private Set<String> synonyms;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .quizCount(tag.getQuizzes().size())
                .synonyms(tag.getSynonyms())
                .build();
    }
}