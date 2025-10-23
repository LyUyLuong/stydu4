package com.lul.Stydu4.mapper;


import com.lul.Stydu4.dto.request.User.UserCreationRequest;
import com.lul.Stydu4.dto.request.User.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "results", ignore = true)
    @Mapping(target = "authProvider", expression = "java(com.lul.Stydu4.enums.AuthProvider.LOCAL)")
    UserEntity toUserEntity(UserCreationRequest userCreationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "results", ignore = true)
    void updateUserEntity(@MappingTarget UserEntity userEntity, UserUpdateRequest userUpdateRequest);

    @Mapping(target = "authProvider", source = "authProvider", qualifiedByName = "authProviderToString")
    UserResponse toUserResponse(UserEntity userEntity);

    @Named("authProviderToString")
    default String authProviderToString(AuthProvider authProvider) {
        return authProvider != null ? authProvider.name() : null;
    }
}

