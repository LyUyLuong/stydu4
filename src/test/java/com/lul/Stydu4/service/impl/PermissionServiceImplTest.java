package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.PermissionRequest;
import com.lul.Stydu4.dto.response.PermissionResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.mapper.PermissionMapper;
import com.lul.Stydu4.repository.IPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl Tests")
class PermissionServiceImplTest {

    @Mock
    private IPermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private PermissionEntity permissionEntity;
    private PermissionRequest permissionRequest;
    private PermissionResponse permissionResponse;

    @BeforeEach
    void setUp() {
        permissionRequest = PermissionRequest.builder()
                .name("READ_COURSE")
                .description("Permission to read courses")
                .build();

        permissionEntity = PermissionEntity.builder()
                .name("READ_COURSE")
                .description("Permission to read courses")
                .build();

        permissionResponse = PermissionResponse.builder()
                .name("READ_COURSE")
                .description("Permission to read courses")
                .build();
    }

    @Nested
    @DisplayName("createPermission Tests")
    class CreatePermissionTests {

        @Test
        @DisplayName("Should create permission successfully")
        void createPermission_ValidData_Success() {
            // GIVEN
            when(permissionMapper.toPermissionEntity(permissionRequest)).thenReturn(permissionEntity);
            when(permissionRepository.save(permissionEntity)).thenReturn(permissionEntity);
            when(permissionMapper.toPermissionResponse(permissionEntity)).thenReturn(permissionResponse);

            // WHEN
            PermissionResponse result = permissionService.createPermission(permissionRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("READ_COURSE");
            assertThat(result.getDescription()).isEqualTo("Permission to read courses");

            verify(permissionMapper).toPermissionEntity(permissionRequest);
            verify(permissionRepository).save(permissionEntity);
            verify(permissionMapper).toPermissionResponse(permissionEntity);
        }

        @Test
        @DisplayName("Should handle permission with minimal data")
        void createPermission_MinimalData_Success() {
            // GIVEN
            PermissionRequest minimalRequest = PermissionRequest.builder()
                    .name("WRITE_COURSE")
                    .build();

            PermissionEntity minimalEntity = PermissionEntity.builder()
                    .name("WRITE_COURSE")
                    .build();

            PermissionResponse minimalResponse = PermissionResponse.builder()
                    .name("WRITE_COURSE")
                    .build();

            when(permissionMapper.toPermissionEntity(minimalRequest)).thenReturn(minimalEntity);
            when(permissionRepository.save(minimalEntity)).thenReturn(minimalEntity);
            when(permissionMapper.toPermissionResponse(minimalEntity)).thenReturn(minimalResponse);

            // WHEN
            PermissionResponse result = permissionService.createPermission(minimalRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("WRITE_COURSE");
        }
    }

    @Nested
    @DisplayName("getAllPermissions Tests")
    class GetAllPermissionsTests {

        @Test
        @DisplayName("Should return all permissions")
        void getAllPermissions_Success() {
            // GIVEN
            PermissionEntity permission2 = PermissionEntity.builder()
                    .name("WRITE_COURSE")
                    .description("Permission to write courses")
                    .build();

            PermissionResponse response2 = PermissionResponse.builder()
                    .name("WRITE_COURSE")
                    .description("Permission to write courses")
                    .build();

            when(permissionRepository.findAll()).thenReturn(List.of(permissionEntity, permission2));
            when(permissionMapper.toPermissionResponse(permissionEntity)).thenReturn(permissionResponse);
            when(permissionMapper.toPermissionResponse(permission2)).thenReturn(response2);

            // WHEN
            List<PermissionResponse> result = permissionService.getAllPermissions();

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("READ_COURSE");
            assertThat(result.get(1).getName()).isEqualTo("WRITE_COURSE");

            verify(permissionRepository).findAll();
            verify(permissionMapper, times(2)).toPermissionResponse(any(PermissionEntity.class));
        }

        @Test
        @DisplayName("Should return empty list when no permissions exist")
        void getAllPermissions_EmptyList() {
            // GIVEN
            when(permissionRepository.findAll()).thenReturn(List.of());

            // WHEN
            List<PermissionResponse> result = permissionService.getAllPermissions();

            // THEN
            assertThat(result).isEmpty();
            verify(permissionRepository).findAll();
        }

        @Test
        @DisplayName("Should map all permissions correctly")
        void getAllPermissions_CorrectMapping() {
            // GIVEN
            PermissionEntity readPermission = PermissionEntity.builder()
                    .name("READ_COURSE")
                    .description("Read permission")
                    .build();

            PermissionEntity writePermission = PermissionEntity.builder()
                    .name("WRITE_COURSE")
                    .description("Write permission")
                    .build();

            PermissionEntity deletePermission = PermissionEntity.builder()
                    .name("DELETE_COURSE")
                    .description("Delete permission")
                    .build();

            PermissionResponse readResponse = PermissionResponse.builder()
                    .name("READ_COURSE")
                    .description("Read permission")
                    .build();

            PermissionResponse writeResponse = PermissionResponse.builder()
                    .name("WRITE_COURSE")
                    .description("Write permission")
                    .build();

            PermissionResponse deleteResponse = PermissionResponse.builder()
                    .name("DELETE_COURSE")
                    .description("Delete permission")
                    .build();

            when(permissionRepository.findAll())
                    .thenReturn(List.of(readPermission, writePermission, deletePermission));
            when(permissionMapper.toPermissionResponse(readPermission)).thenReturn(readResponse);
            when(permissionMapper.toPermissionResponse(writePermission)).thenReturn(writeResponse);
            when(permissionMapper.toPermissionResponse(deletePermission)).thenReturn(deleteResponse);

            // WHEN
            List<PermissionResponse> result = permissionService.getAllPermissions();

            // THEN
            assertThat(result).hasSize(3);
            assertThat(result).extracting(PermissionResponse::getName)
                    .containsExactly("READ_COURSE", "WRITE_COURSE", "DELETE_COURSE");
        }
    }

    @Nested
    @DisplayName("deletePermission Tests")
    class DeletePermissionTests {

        @Test
        @DisplayName("Should delete permission successfully")
        void deletePermission_ValidName_Success() {
            // GIVEN
            doNothing().when(permissionRepository).deleteById("READ_COURSE");

            // WHEN
            permissionService.deletePermission("READ_COURSE");

            // THEN
            verify(permissionRepository).deleteById("READ_COURSE");
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent permission")
        void deletePermission_NonExistent_NoException() {
            // GIVEN
            doNothing().when(permissionRepository).deleteById("NON_EXISTENT");

            // WHEN & THEN
            assertThatCode(() -> permissionService.deletePermission("NON_EXISTENT"))
                    .doesNotThrowAnyException();

            verify(permissionRepository).deleteById("NON_EXISTENT");
        }

        @Test
        @DisplayName("Should delete multiple different permissions")
        void deletePermission_MultipleDeletes_Success() {
            // GIVEN
            doNothing().when(permissionRepository).deleteById(anyString());

            // WHEN
            permissionService.deletePermission("READ_COURSE");
            permissionService.deletePermission("WRITE_COURSE");
            permissionService.deletePermission("DELETE_COURSE");

            // THEN
            verify(permissionRepository).deleteById("READ_COURSE");
            verify(permissionRepository).deleteById("WRITE_COURSE");
            verify(permissionRepository).deleteById("DELETE_COURSE");
        }
    }
}
