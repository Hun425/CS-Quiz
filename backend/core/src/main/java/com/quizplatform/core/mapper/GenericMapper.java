package com.quizplatform.core.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 범용 Mapper 인터페이스
 * <p>
 * 모든 매퍼 인터페이스의 기본 기능을 제공하는 제네릭 인터페이스입니다.
 * 도메인 엔티티와 DTO 간의 변환 기능을 정의합니다.
 * </p>
 * 
 * @param <D> DTO 타입
 * @param <E> 엔티티 타입
 */
public interface GenericMapper<D, E> {

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param entity 엔티티 객체
     * @return DTO 객체
     */
    D toDto(E entity);

    /**
     * DTO를 엔티티로 변환합니다.
     *
     * @param dto DTO 객체
     * @return 엔티티 객체
     */
    E toEntity(D dto);

    /**
     * 엔티티 목록을 DTO 목록으로 변환합니다.
     *
     * @param entities 엔티티 객체 목록
     * @return DTO 객체 목록
     */
    List<D> toDto(List<E> entities);

    /**
     * DTO 목록을 엔티티 목록으로 변환합니다.
     *
     * @param dtos DTO 객체 목록
     * @return 엔티티 객체 목록
     */
    List<E> toEntity(List<D> dtos);

    /**
     * DTO의 필드값을 기존 엔티티에 복사합니다.
     * null 값은 무시합니다.
     *
     * @param dto DTO 객체 (소스)
     * @param entity 엔티티 객체 (타겟)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}