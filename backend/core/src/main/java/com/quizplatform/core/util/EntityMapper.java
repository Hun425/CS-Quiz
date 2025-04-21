package com.quizplatform.core.util;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 엔티티와 DTO 간 매핑을 위한 기본 인터페이스
 * <p>
 * MapStruct 기반의 매퍼를 위한 공통 메서드를 정의합니다.
 * </p>
 *
 * @param <D> DTO 타입
 * @param <E> 엔티티 타입
 */
public interface EntityMapper<D, E> {

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
     * 엔티티 리스트를 DTO 리스트로 변환합니다.
     *
     * @param entityList 엔티티 리스트
     * @return DTO 리스트
     */
    List<D> toDto(List<E> entityList);

    /**
     * DTO 리스트를 엔티티 리스트로 변환합니다.
     *
     * @param dtoList DTO 리스트
     * @return 엔티티 리스트
     */
    List<E> toEntity(List<D> dtoList);

    /**
     * 엔티티를 업데이트합니다.
     * null 값은 무시하고 기존 값을 유지합니다.
     *
     * @param dto DTO (소스)
     * @param entity 엔티티 (타겟)
     * @return 업데이트된 엔티티
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    E partialUpdate(D dto, @MappingTarget E entity);
}