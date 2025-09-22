package com.lul.Stydu4.mapper;


import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUserEntity(UserCreationRequest userCreationRequest);
    void updateUserEntity(@MappingTarget UserEntity userEntity, UserUpdateRequest userUpdateRequest);
    UserResponse toUserResponse(UserEntity userEntity);

}
