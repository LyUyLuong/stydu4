package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.RoleRequest;
import com.lul.Stydu4.dto.response.RoleResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IRoleService {

    RoleResponse createRole(RoleRequest roleRequest);
    List<RoleResponse> getAllRoles();
    void deleteRole(String name);

}
