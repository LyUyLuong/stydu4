package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.RoleRequest;
import com.lul.Stydu4.dto.response.PermissionResponse;
import com.lul.Stydu4.dto.response.RoleResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.mapper.RoleMapper;
import com.lul.Stydu4.repository.IPermissionRepository;
import com.lul.Stydu4.repository.IRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Tests")
class RoleServiceImplTest {

    @Mock
    private IRoleRepository repository;

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private RoleMapper mapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleRequest roleRequest;
    private RoleEntity roleEntity;
    private RoleResponse roleResponse;
    private PermissionEntity permissionEntity1;
    private PermissionEntity permissionEntity2;
    private PermissionResponse permissionResponse1;
    private PermissionResponse permissionResponse2;

    @BeforeEach
    void setUp() {
        // Setup Permission Entities
        permissionEntity1 = PermissionEntity.builder()
                .name("READ_DATA")
                .description("Permission to read data")
                .build();

        permissionEntity2 = PermissionEntity.builder()
                .name("WRITE_DATA")
                .description("Permission to write data")
                .build();

        // Setup Permission Responses
        permissionResponse1 = PermissionResponse.builder()
                .name("READ_DATA")
                .description("Permission to read data")
                .build();

        permissionResponse2 = PermissionResponse.builder()
                .name("WRITE_DATA")
                .description("Permission to write data")
                .build();

        // Setup Role Request
        roleRequest = RoleRequest.builder()
                .name("ADMIN")
                .description("Administrator role")
                .permissions(Set.of("READ_DATA", "WRITE_DATA"))
                .build();

        // Setup Role Entity
        roleEntity = RoleEntity.builder()
                .name("ADMIN")
                .description("Administrator role")
                .permissions(new HashSet<>(Set.of(permissionEntity1, permissionEntity2)))
                .build();

        // Setup Role Response
        roleResponse = RoleResponse.builder()
                .name("ADMIN")
                .description("Administrator role")
                .permissions(Set.of(permissionResponse1, permissionResponse2))
                .build();
    }

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully with permissions")
        void createRole_WithPermissions_Success() {
            // GIVEN
            List<PermissionEntity> permissionsList = List.of(permissionEntity1, permissionEntity2);

            when(mapper.toRoleEntity(roleRequest)).thenReturn(roleEntity);
            when(permissionRepository.findAllById(roleRequest.getPermissions()))
                    .thenReturn(permissionsList);
            when(repository.save(any(RoleEntity.class))).thenReturn(roleEntity);
            when(mapper.toRoleResponse(roleEntity)).thenReturn(roleResponse);

            // WHEN
            RoleResponse result = roleService.createRole(roleRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("ADMIN");
            assertThat(result.getDescription()).isEqualTo("Administrator role");
            assertThat(result.getPermissions()).hasSize(2);
            assertThat(result.getPermissions()).contains(permissionResponse1, permissionResponse2);

            // Verify interactions
            verify(mapper, times(1)).toRoleEntity(roleRequest);
            verify(permissionRepository, times(1)).findAllById(roleRequest.getPermissions());
            verify(repository, times(1)).save(any(RoleEntity.class));
            verify(mapper, times(1)).toRoleResponse(roleEntity);

            // Verify that permissions were set correctly
            ArgumentCaptor<RoleEntity> roleCaptor = ArgumentCaptor.forClass(RoleEntity.class);
            verify(repository).save(roleCaptor.capture());
            RoleEntity savedRole = roleCaptor.getValue();
            assertThat(savedRole.getPermissions()).hasSize(2);
            assertThat(savedRole.getPermissions()).contains(permissionEntity1, permissionEntity2);
        }

        @Test
        @DisplayName("Should create role without permissions")
        void createRole_WithoutPermissions_Success() {
            // GIVEN
            RoleRequest requestWithoutPermissions = RoleRequest.builder()
                    .name("USER")
                    .description("Regular user role")
                    .permissions(Set.of())
                    .build();

            RoleEntity entityWithoutPermissions = RoleEntity.builder()
                    .name("USER")
                    .description("Regular user role")
                    .permissions(new HashSet<>())
                    .build();

            RoleResponse responseWithoutPermissions = RoleResponse.builder()
                    .name("USER")
                    .description("Regular user role")
                    .permissions(Set.of())
                    .build();

            when(mapper.toRoleEntity(requestWithoutPermissions)).thenReturn(entityWithoutPermissions);
            when(permissionRepository.findAllById(Set.of())).thenReturn(List.of());
            when(repository.save(any(RoleEntity.class))).thenReturn(entityWithoutPermissions);
            when(mapper.toRoleResponse(entityWithoutPermissions)).thenReturn(responseWithoutPermissions);

            // WHEN
            RoleResponse result = roleService.createRole(requestWithoutPermissions);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("USER");
            assertThat(result.getPermissions()).isEmpty();

            verify(mapper, times(1)).toRoleEntity(requestWithoutPermissions);
            verify(permissionRepository, times(1)).findAllById(Set.of());
            verify(repository, times(1)).save(any(RoleEntity.class));
            verify(mapper, times(1)).toRoleResponse(entityWithoutPermissions);
        }

        @Test
        @DisplayName("Should create role with partial permissions when some permissions not found")
        void createRole_PartialPermissions_Success() {
            // GIVEN
            Set<String> requestedPermissions = Set.of("READ_DATA", "WRITE_DATA", "DELETE_DATA");
            RoleRequest request = RoleRequest.builder()
                    .name("MODERATOR")
                    .description("Moderator role")
                    .permissions(requestedPermissions)
                    .build();

            RoleEntity entity = RoleEntity.builder()
                    .name("MODERATOR")
                    .description("Moderator role")
                    .permissions(new HashSet<>())
                    .build();

            // Only 2 permissions found (DELETE_DATA not found)
            List<PermissionEntity> foundPermissions = List.of(permissionEntity1, permissionEntity2);

            when(mapper.toRoleEntity(request)).thenReturn(entity);
            when(permissionRepository.findAllById(requestedPermissions)).thenReturn(foundPermissions);
            when(repository.save(any(RoleEntity.class))).thenReturn(entity);
            when(mapper.toRoleResponse(entity)).thenReturn(roleResponse);

            // WHEN
            RoleResponse result = roleService.createRole(request);

            // THEN
            assertThat(result).isNotNull();

            ArgumentCaptor<RoleEntity> roleCaptor = ArgumentCaptor.forClass(RoleEntity.class);
            verify(repository).save(roleCaptor.capture());
            RoleEntity savedRole = roleCaptor.getValue();
            // Should only have 2 permissions (the ones that were found)
            assertThat(savedRole.getPermissions()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getAllRoles Tests")
    class GetAllRolesTests {

        @Test
        @DisplayName("Should return all roles successfully")
        void getAllRoles_Success() {
            // GIVEN
            RoleEntity role1 = RoleEntity.builder()
                    .name("ADMIN")
                    .description("Administrator")
                    .permissions(new HashSet<>())
                    .build();

            RoleEntity role2 = RoleEntity.builder()
                    .name("USER")
                    .description("Regular user")
                    .permissions(new HashSet<>())
                    .build();

            RoleResponse response1 = RoleResponse.builder()
                    .name("ADMIN")
                    .description("Administrator")
                    .permissions(Set.of())
                    .build();

            RoleResponse response2 = RoleResponse.builder()
                    .name("USER")
                    .description("Regular user")
                    .permissions(Set.of())
                    .build();

            List<RoleEntity> roleEntities = List.of(role1, role2);

            when(repository.findAll()).thenReturn(roleEntities);
            when(mapper.toRoleResponse(role1)).thenReturn(response1);
            when(mapper.toRoleResponse(role2)).thenReturn(response2);

            // WHEN
            List<RoleResponse> result = roleService.getAllRoles();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("ADMIN");
            assertThat(result.get(1).getName()).isEqualTo("USER");

            verify(repository, times(1)).findAll();
            verify(mapper, times(1)).toRoleResponse(role1);
            verify(mapper, times(1)).toRoleResponse(role2);
        }

        @Test
        @DisplayName("Should return empty list when no roles exist")
        void getAllRoles_EmptyList() {
            // GIVEN
            when(repository.findAll()).thenReturn(List.of());

            // WHEN
            List<RoleResponse> result = roleService.getAllRoles();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(repository, times(1)).findAll();
            verify(mapper, never()).toRoleResponse(any());
        }

        @Test
        @DisplayName("Should return roles with permissions")
        void getAllRoles_WithPermissions_Success() {
            // GIVEN
            RoleEntity adminRole = RoleEntity.builder()
                    .name("ADMIN")
                    .description("Administrator")
                    .permissions(new HashSet<>(Set.of(permissionEntity1, permissionEntity2)))
                    .build();

            RoleResponse adminResponse = RoleResponse.builder()
                    .name("ADMIN")
                    .description("Administrator")
                    .permissions(Set.of(permissionResponse1, permissionResponse2))
                    .build();

            when(repository.findAll()).thenReturn(List.of(adminRole));
            when(mapper.toRoleResponse(adminRole)).thenReturn(adminResponse);

            // WHEN
            List<RoleResponse> result = roleService.getAllRoles();

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPermissions()).hasSize(2);
            assertThat(result.get(0).getPermissions()).contains(permissionResponse1, permissionResponse2);

            verify(repository, times(1)).findAll();
            verify(mapper, times(1)).toRoleResponse(adminRole);
        }
    }

    @Nested
    @DisplayName("deleteRole Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role successfully")
        void deleteRole_Success() {
            // GIVEN
            String roleName = "ADMIN";
            doNothing().when(repository).deleteById(roleName);

            // WHEN
            roleService.deleteRole(roleName);

            // THEN
            verify(repository, times(1)).deleteById(roleName);
        }

        @Test
        @DisplayName("Should call deleteById with correct role name")
        void deleteRole_CorrectRoleName() {
            // GIVEN
            String roleName = "USER";
            ArgumentCaptor<String> roleNameCaptor = ArgumentCaptor.forClass(String.class);

            // WHEN
            roleService.deleteRole(roleName);

            // THEN
            verify(repository).deleteById(roleNameCaptor.capture());
            assertThat(roleNameCaptor.getValue()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should not throw exception when deleting role")
        void deleteRole_NoException() {
            // GIVEN
            doNothing().when(repository).deleteById("ADMIN");

            // WHEN & THEN - Should not throw any exception
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
                roleService.deleteRole("ADMIN");
            });
        }
    }
}
