package com.quizplatform.modules.user.mapper;

import com.quizplatform.core.mapper.GenericIdMapper;
import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.user.dto.UserProfileDto;
import com.quizplatform.modules.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * User 관련 매퍼 인터페이스
 * <p>
 * User 엔티티와 User 관련 DTO 간의 변환을 정의합니다.
 * </p>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper extends GenericIdMapper<UserResponse, User, Long> {

    /**
     * User 엔티티를 UserResponse DTO로 변환합니다.
     * 
     * @param user User 엔티티
     * @return UserResponse DTO
     */
    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "profileImage", source = "profileImage")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastLogin", source = "lastLogin")
    UserResponse toDto(User user);

    /**
     * User 엔티티를 UserProfileDto로 변환합니다.
     * 
     * @param user User 엔티티
     * @return UserProfileDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "profileImage", source = "profileImage")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "bio", source = "bio")
    UserProfileDto toProfileDto(User user);
}