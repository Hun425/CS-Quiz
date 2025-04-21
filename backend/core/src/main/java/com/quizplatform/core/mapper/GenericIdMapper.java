package com.quizplatform.core.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 범용 ID 기반 Mapper 인터페이스
 * <p>
 * ID 필드를 가진 엔티티와 DTO 간의 변환을 위한 확장 인터페이스입니다.
 * </p>
 *
 * @param <D> DTO 타입
 * @param <E> 엔티티 타입
 * @param <I> ID 타입
 */
public interface GenericIdMapper<D, E, I> extends GenericMapper<D, E> {

    /**
     * ID로 DTO를 변환합니다.
     * 데이터베이스 조회 없이 ID만 설정된 객체를 생성할 때 유용합니다.
     *
     * @param id 엔티티의 ID
     * @return ID가 설정된 DTO
     */
    D toDto(I id);

    /**
     * DTO의 ID를 사용하여 엔티티 참조를 생성합니다.
     * 데이터베이스 조회 없이 ID만 설정된 엔티티를 생성할 때 유용합니다.
     *
     * @param dto ID를 포함한 DTO
     * @return ID가 설정된 엔티티 참조
     */
    E toEntityReference(D dto);

    /**
     * DTO 목록의 ID를 사용하여 엔티티 참조 목록을 생성합니다.
     *
     * @param dtos ID를 포함한 DTO 목록
     * @return ID가 설정된 엔티티 참조 목록
     */
    List<E> toEntityReference(List<D> dtos);
}