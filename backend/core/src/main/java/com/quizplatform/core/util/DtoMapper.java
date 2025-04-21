package com.quizplatform.core.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO 변환 유틸리티 인터페이스
 * <p>
 * 엔티티와 DTO 간의 변환을 위한 인터페이스입니다.
 * </p>
 *
 * @param <E> 엔티티 타입
 * @param <D> DTO 타입
 */
public interface DtoMapper<E, D> {

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param entity 엔티티
     * @return DTO
     */
    D toDto(E entity);

    /**
     * DTO를 엔티티로 변환합니다.
     *
     * @param dto DTO
     * @return 엔티티
     */
    E toEntity(D dto);

    /**
     * 엔티티 컬렉션을 DTO 리스트로 변환합니다.
     *
     * @param entities 엔티티 컬렉션
     * @return DTO 리스트
     */
    default List<D> toDtoList(Collection<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * DTO 컬렉션을 엔티티 리스트로 변환합니다.
     *
     * @param dtos DTO 컬렉션
     * @return 엔티티 리스트
     */
    default List<E> toEntityList(Collection<D> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 기존 엔티티에 DTO의 값을 적용합니다.
     *
     * @param dto 업데이트할 DTO
     * @param entity 업데이트될 엔티티
     * @return 업데이트된 엔티티
     */
    E updateEntityFromDto(D dto, E entity);
}