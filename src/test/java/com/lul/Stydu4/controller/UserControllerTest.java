package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.User.UserCreationRequest;
import com.lul.Stydu4.dto.response.RoleResponse;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.enums.AuthProvider;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable Security filters
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    private UserCreationRequest validCreationRequest;
    private UserResponse userResponse;
    private RoleResponse userRole;

    @BeforeEach
    void setUp() {
        userRole = RoleResponse.builder()
                .name("USER")
                .description("Default user role")
                .build();

        validCreationRequest = UserCreationRequest.builder()
                .username("john_doe")
                .password("password123")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .build();

        userResponse = UserResponse.builder()
                .id("user-123")
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .authProvider(AuthProvider.LOCAL.name())
                .roles(Set.of(userRole))
                .build();
    }

    @Nested
    @DisplayName("POST /users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void createUser_ValidRequest_Success() throws Exception {
            // GIVEN
            when(userService.createUser(any(UserCreationRequest.class)))
                    .thenReturn(userResponse);

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.id").value("user-123"))
                    .andExpect(jsonPath("$.result.username").value("john_doe"))
                    .andExpect(jsonPath("$.result.email").value("john@example.com"))
                    .andExpect(jsonPath("$.result.firstName").value("John"))
                    .andExpect(jsonPath("$.result.lastName").value("Doe"))
                    .andExpect(jsonPath("$.result.authProvider").value("LOCAL"))
                    .andExpect(jsonPath("$.result.roles").isArray())
                    .andExpect(jsonPath("$.result.roles[0].name").value("USER"));

            verify(userService, times(1)).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when username is missing")
        void createUser_MissingUsername_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setUsername(null);

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USERNAME_REQUIRED.getCode()))
                    .andExpect(jsonPath("$.message").value(ErrorCode.USERNAME_REQUIRED.getMessage()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when username is too short")
        void createUser_UsernameTooShort_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setUsername("abc"); // < 5 characters

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_USERNAME.getCode()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when password is missing")
        void createUser_MissingPassword_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setPassword(null);

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_REQUIRED.getCode()))
                    .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_REQUIRED.getMessage()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when password is too short")
        void createUser_PasswordTooShort_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setPassword("123"); // < 6 characters

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD.getCode()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when email is missing")
        void createUser_MissingEmail_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setEmail(null);

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_REQUIRED.getCode()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when email format is invalid")
        void createUser_InvalidEmailFormat_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setEmail("invalid-email");

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_EMAIL.getCode()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when user already exists")
        void createUser_UserExists_ThrowsException() throws Exception {
            // GIVEN
            when(userService.createUser(any(UserCreationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.USER_EXISTED));

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_EXISTED.getCode()))
                    .andExpect(jsonPath("$.message").value(ErrorCode.USER_EXISTED.getMessage()));

            verify(userService, times(1)).createUser(any(UserCreationRequest.class));
        }

        @Test
        @DisplayName("Should fail when DOB is invalid (under 16)")
        void createUser_InvalidDOB_ValidationError() throws Exception {
            // GIVEN
            validCreationRequest.setDob(LocalDate.now().minusYears(10)); // 10 years old

            // WHEN
            ResultActions result = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // THEN
            result.andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_DOB.getCode()));

            verify(userService, never()).createUser(any(UserCreationRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /users - Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users successfully")
        void getAllUsers_Success() throws Exception {
            // GIVEN
            UserResponse user1 = UserResponse.builder()
                    .id("user-1")
                    .username("user1")
                    .email("user1@example.com")
                    .authProvider(AuthProvider.LOCAL.name())
                    .build();

            UserResponse user2 = UserResponse.builder()
                    .id("user-2")
                    .username("user2")
                    .email("user2@example.com")
                    .authProvider(AuthProvider.GOOGLE.name())
                    .build();

            when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

            // WHEN
            ResultActions result = mockMvc.perform(get("/users")
                    .contentType(MediaType.APPLICATION_JSON));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result", hasSize(2)))
                    .andExpect(jsonPath("$.result[0].id").value("user-1"))
                    .andExpect(jsonPath("$.result[0].authProvider").value("LOCAL"))
                    .andExpect(jsonPath("$.result[1].id").value("user-2"))
                    .andExpect(jsonPath("$.result[1].authProvider").value("GOOGLE"));

            verify(userService, times(1)).getAllUsers();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_EmptyList() throws Exception {
            // GIVEN
            when(userService.getAllUsers()).thenReturn(List.of());

            // WHEN
            ResultActions result = mockMvc.perform(get("/users"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result", hasSize(0)));

            verify(userService, times(1)).getAllUsers();
        }
    }

    @Nested
    @DisplayName("GET /users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when ID exists")
        void getUserById_ValidId_Success() throws Exception {
            // GIVEN
            when(userService.getUserById("user-123")).thenReturn(userResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/users/{id}", "user-123"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value("user-123"))
                    .andExpect(jsonPath("$.result.username").value("john_doe"))
                    .andExpect(jsonPath("$.result.email").value("john@example.com"));

            verify(userService, times(1)).getUserById("user-123");
        }

        @Test
        @DisplayName("Should fail when user ID not found")
        void getUserById_InvalidId_NotFound() throws Exception {
            // GIVEN
            when(userService.getUserById(anyString()))
                    .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

            // WHEN
            ResultActions result = mockMvc.perform(get("/users/{id}", "invalid-id"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_EXISTED.getCode()));

            verify(userService, times(1)).getUserById("invalid-id");
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_ValidId_Success() throws Exception {
            // GIVEN
            doNothing().when(userService).deleteUser("user-123");

            // WHEN
            ResultActions result = mockMvc.perform(delete("/users/{id}", "user-123"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User deleted"));

            verify(userService, times(1)).deleteUser("user-123");
        }

        @Test
        @DisplayName("Should fail when deleting non-existent user")
        void deleteUser_InvalidId_NotFound() throws Exception {
            // GIVEN
            doThrow(new AppException(ErrorCode.USER_NOT_EXISTED))
                    .when(userService).deleteUser("invalid-id");

            // WHEN
            ResultActions result = mockMvc.perform(delete("/users/{id}", "invalid-id"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).deleteUser("invalid-id");
        }
    }

    @Nested
    @DisplayName("GET /users/myInfo - Get Current User Info")
    class GetMyInfoTests {

        @Test
        @DisplayName("Should return current user info")
        void getMyInfo_Success() throws Exception {
            // GIVEN
            when(userService.getMyInfo()).thenReturn(userResponse);

            // WHEN
            ResultActions result = mockMvc.perform(get("/users/myInfo"));

            // THEN
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.username").value("john_doe"))
                    .andExpect(jsonPath("$.result.email").value("john@example.com"));

            verify(userService, times(1)).getMyInfo();
        }
    }
}
