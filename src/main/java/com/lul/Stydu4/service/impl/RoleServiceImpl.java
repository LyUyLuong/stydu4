package com.lul.Stydu4.service.impl;


import com.lul.Stydu4.dto.request.RoleRequest;
import com.lul.Stydu4.dto.response.RoleResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.mapper.RoleMapper;
import com.lul.Stydu4.repository.IPermissionRepository;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.service.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements IRoleService {

    IRoleRepository repository;
    IPermissionRepository permissionRepository;
    RoleMapper mapper;

    @Override
    public RoleResponse createRole(RoleRequest roleRequest) {
        RoleEntity roleEntity = mapper.toRoleEntity(roleRequest);

        List<PermissionEntity> permissions = permissionRepository.findAllById(roleRequest.getPermissions());
        roleEntity.setPermissions(new HashSet<>(permissions));
        return mapper.toRoleResponse(repository.save(roleEntity));
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<RoleEntity> roleEntities = repository.findAll();
        return roleEntities.stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void deleteRole(String name) {
        repository.deleteById(name);
    }

    IRoleRepository roleRepository;
    RoleMapper roleMapper;



}
