package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.PermissionRequest;
import com.lul.Stydu4.dto.response.PermissionResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.mapper.PermissionMapper;
import com.lul.Stydu4.repository.IPermissionRepository;
import com.lul.Stydu4.service.IPermissionService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService implements IPermissionService {

    IPermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionResponse createPermission(PermissionRequest permissionRequest) {

        PermissionEntity permissionEntity = permissionMapper.toPermissionEntity(permissionRequest);
        permissionEntity = permissionRepository.save(permissionEntity);
        return permissionMapper.toPermissionResponse(permissionEntity);

    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        List<PermissionEntity> permissionEntities = permissionRepository.findAll();
        return permissionEntities.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public void deletePermission(String name) {
        permissionRepository.deleteById(name);
    }


}
