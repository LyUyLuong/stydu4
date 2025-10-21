package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.User.UserCreationRequest;
import com.lul.Stydu4.dto.request.User.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.Role;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.UserMapper;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private UserResponse userResponse;
    private RoleEntity userRole;
    private UserCreationRequest creationRequest;

    @BeforeEach
    void setUp() {
        // Setup role
        userRole = RoleEntity.builder()
                .name("USER")
                .description("Default user role")
                .permissions(new HashSet<>())
                .build();

        // Setup creation request
        creationRequest = UserCreationRequest.builder()
                .username("john_doe")
                .password("password123")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .build();

        // Setup user entity
        userEntity = UserEntity.builder()
                .id("user-123")
                .username("john_doe")
                .password("encoded_password")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .authProvider(AuthProvider.LOCAL)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        // Setup user response
        userResponse = UserResponse.builder()
                .id("user-123")
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .authProvider(AuthProvider.LOCAL.name())
                .build();
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void createUser_ValidData_Success() {
            // GIVEN
            when(userRepository.existsByUsername(creationRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(creationRequest.getEmail())).thenReturn(false);
            when(userMapper.toUserEntity(creationRequest)).thenReturn(userEntity);
            when(roleRepository.findById(Role.USER.name())).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode(creationRequest.getPassword())).thenReturn("encoded_password");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.createUser(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getAuthProvider()).isEqualTo(AuthProvider.LOCAL.name());

            verify(userRepository).existsByUsername("john_doe");
            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).save(any(UserEntity.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createUser_UsernameExists_ThrowException() {
            // GIVEN
            when(userRepository.existsByUsername(creationRequest.getUsername())).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> userService.createUser(creationRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_EXISTED);

            verify(userRepository).existsByUsername("john_doe");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_EmailExists_ThrowException() {
            // GIVEN
            when(userRepository.existsByUsername(creationRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(creationRequest.getEmail())).thenReturn(true);

            // WHEN & THEN
            assertThatThrownBy(() -> userService.createUser(creationRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_EXISTED);

            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when USER role not found")
        void createUser_RoleNotFound_ThrowException() {
            // GIVEN
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toUserEntity(any())).thenReturn(userEntity);
            when(roleRepository.findById(Role.USER.name())).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> userService.createUser(creationRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION);

            verify(roleRepository).findById(Role.USER.name());
        }

        @Test
        @DisplayName("Should handle null password gracefully")
        void createUser_NullPassword_Success() {
            // GIVEN
            creationRequest.setPassword(null);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toUserEntity(creationRequest)).thenReturn(userEntity);
            when(roleRepository.findById(Role.USER.name())).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.createUser(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should handle empty password gracefully")
        void createUser_EmptyPassword_Success() {
            // GIVEN
            creationRequest.setPassword("");
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toUserEntity(creationRequest)).thenReturn(userEntity);
            when(roleRepository.findById(Role.USER.name())).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.createUser(creationRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {

        private UserUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = UserUpdateRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .password("newPassword123")
                    .roles(List.of("USER", "ADMIN"))
                    .build();
        }

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_ValidData_Success() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("new_encoded_password");

            RoleEntity adminRole = RoleEntity.builder()
                    .name("ADMIN")
                    .description("Admin role")
                    .build();

            when(roleRepository.findAllById(updateRequest.getRoles()))
                    .thenReturn(List.of(userRole, adminRole));

            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            doNothing().when(userMapper).updateUserEntity(any(), any());

            // WHEN
            UserResponse result = userService.updateUser("user-123", updateRequest);

            // THEN
            assertThat(result).isNotNull();
            verify(userRepository).findById("user-123");
            verify(passwordEncoder, times(2)).encode(updateRequest.getPassword());
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void updateUser_UserNotFound_ThrowException() {
            // GIVEN
            when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> userService.updateUser("invalid-id", updateRequest))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(userRepository).findById("invalid-id");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode password for LOCAL auth provider")
        void updateUser_LocalProvider_EncodesPassword() {
            // GIVEN
            userEntity.setAuthProvider(AuthProvider.LOCAL);
            when(userRepository.findById("user-123")).thenReturn(Optional.of(userEntity));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(roleRepository.findAllById(anyList())).thenReturn(List.of(userRole));
            when(userRepository.save(any())).thenReturn(userEntity);
            when(userMapper.toUserResponse(any())).thenReturn(userResponse);

            // WHEN
            userService.updateUser("user-123", updateRequest);

            // THEN
            verify(passwordEncoder, times(2)).encode(updateRequest.getPassword());
        }

        @Test
        @DisplayName("Should not encode password for OAuth2 user when password is null")
        void updateUser_OAuth2Provider_SkipsPasswordEncoding() {
            // GIVEN
            userEntity.setAuthProvider(AuthProvider.GOOGLE);
            updateRequest.setPassword(null);

            when(userRepository.findById("user-123")).thenReturn(Optional.of(userEntity));
            when(roleRepository.findAllById(anyList())).thenReturn(List.of(userRole));
            when(userRepository.save(any())).thenReturn(userEntity);
            when(userMapper.toUserResponse(any())).thenReturn(userResponse);

            // WHEN
            userService.updateUser("user-123", updateRequest);

            // THEN
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when ID exists")
        void getUserById_ValidId_Success() {
            // GIVEN
            when(userRepository.findById("user-123")).thenReturn(Optional.of(userEntity));
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.getUserById("user-123");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUsername()).isEqualTo("john_doe");

            verify(userRepository).findById("user-123");
            verify(userMapper).toUserResponse(userEntity);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserById_InvalidId_ThrowException() {
            // GIVEN
            when(userRepository.findById("invalid-id")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> userService.getUserById("invalid-id"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(userRepository).findById("invalid-id");
            verify(userMapper, never()).toUserResponse(any());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_ValidId_Success() {
            // GIVEN
            doNothing().when(userRepository).deleteById("user-123");

            // WHEN
            userService.deleteUser("user-123");

            // THEN
            verify(userRepository).deleteById("user-123");
        }

        @Test
        @DisplayName("Should not throw exception even if user doesn't exist")
        void deleteUser_InvalidId_NoException() {
            // GIVEN
            doNothing().when(userRepository).deleteById("invalid-id");

            // WHEN & THEN
            assertThatCode(() -> userService.deleteUser("invalid-id"))
                    .doesNotThrowAnyException();

            verify(userRepository).deleteById("invalid-id");
        }
    }

    @Nested
    @DisplayName("getAllUsers Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users")
        void getAllUsers_Success() {
            // GIVEN
            UserEntity user2 = UserEntity.builder()
                    .id("user-456")
                    .username("jane_doe")
                    .email("jane@example.com")
                    .authProvider(AuthProvider.GOOGLE)
                    .build();

            UserResponse response2 = UserResponse.builder()
                    .id("user-456")
                    .username("jane_doe")
                    .email("jane@example.com")
                    .authProvider(AuthProvider.GOOGLE.name())
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(userEntity, user2));
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);
            when(userMapper.toUserResponse(user2)).thenReturn(response2);

            // WHEN
            List<UserResponse> result = userService.getAllUsers();

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUsername()).isEqualTo("john_doe");
            assertThat(result.get(0).getAuthProvider()).isEqualTo(AuthProvider.LOCAL.name());
            assertThat(result.get(1).getUsername()).isEqualTo("jane_doe");
            assertThat(result.get(1).getAuthProvider()).isEqualTo(AuthProvider.GOOGLE.name());

            verify(userRepository).findAll();
            verify(userMapper, times(2)).toUserResponse(any());
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_EmptyList() {
            // GIVEN
            when(userRepository.findAll()).thenReturn(List.of());

            // WHEN
            List<UserResponse> result = userService.getAllUsers();

            // THEN
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getMyInfo Tests")
    class GetMyInfoTests {

        @Test
        @DisplayName("Should return current user info")
        void getMyInfo_Success() {
            // GIVEN
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn("john_doe");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(userEntity));
            when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.getMyInfo();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");

            verify(userRepository).findByUsername("john_doe");
            verify(userMapper).toUserResponse(userEntity);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getMyInfo_UserNotFound_ThrowException() {
            // GIVEN
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn("unknown_user");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> userService.getMyInfo())
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(userRepository).findByUsername("unknown_user");
        }
    }
}
