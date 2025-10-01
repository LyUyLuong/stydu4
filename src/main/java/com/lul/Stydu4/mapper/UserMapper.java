package com.lul.Stydu4.mapper;


import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

//@Mapper(componentModel = "spring", uses = RoleMapper.class)
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUserEntity(UserCreationRequest userCreationRequest);

    @Mapping(target = "roles", ignore = true)
    void updateUserEntity(@MappingTarget UserEntity userEntity, UserUpdateRequest userUpdateRequest);
    UserResponse toUserResponse(UserEntity userEntity);

}
