package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPermissionRepository extends JpaRepository<PermissionEntity, String> {
}
