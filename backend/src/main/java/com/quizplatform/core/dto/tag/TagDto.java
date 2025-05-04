package com.quizplatform.core.dto.tag;

import com.quizplatform.core.domain.tag.Tag; // 필요한 경우 Tag 엔티티 import
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 간단한 태그 정보를 전달하기 위한 DTO 클래스
 */
@Getter
@Builder
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자
public class TagDto {

    private Long id;
    private String name;

    /**
     * Tag 엔티티를 TagDto로 변환하는 정적 팩토리 메서드
     * @param tag Tag 엔티티 객체
     * @return 변환된 TagDto 객체
     */
    public static TagDto fromEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        return TagDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}