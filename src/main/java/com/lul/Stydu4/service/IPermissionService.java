package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.PermissionRequest;
import com.lul.Stydu4.dto.response.PermissionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IPermissionService {

    PermissionResponse createPermission(PermissionRequest permissionRequest);
    List<PermissionResponse> getAllPermissions();
    void deletePermission(String name);
}
