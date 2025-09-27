package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.RoleRequest;
import com.lul.Stydu4.dto.response.RoleResponse;
import com.lul.Stydu4.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PermissionMapper.class)
public interface RoleMapper {

    @Mapping(target = "permissions", ignore = true)
    RoleEntity toRoleEntity(RoleRequest request);

//    @Mapping(target = "permissions", ignore = true)
    RoleResponse toRoleResponse(RoleEntity entity);
}