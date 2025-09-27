package com.lul.Stydu4.mapper;

import com.lul.Stydu4.dto.request.PermissionRequest;
import com.lul.Stydu4.dto.response.PermissionResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionEntity toPermissionEntity(PermissionRequest request);
    PermissionResponse toPermissionResponse(PermissionEntity entity);

}
