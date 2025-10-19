package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.User.UserCreationRequest;
import com.lul.Stydu4.dto.response.RoleResponse;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)  // ← TẮT Security filters
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IUserService userService;

    private UserCreationRequest creationRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        RoleResponse userRole = RoleResponse.builder()
                .name("USER")
                .description("Default user role")
                .build();

        creationRequest = UserCreationRequest.builder()
                .username("john_doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .build();

        userResponse = UserResponse.builder()
                .id("user-123")
                .username("john_doe")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .roles(Set.of(userRole))
                .build();


    }

    @Test
    void createUser_ValidRequest_Success() throws Exception {
        // GIVEN: Mock service
        when(userService.createUser(any(UserCreationRequest.class)))
                .thenReturn(userResponse);

        // WHEN: Call API
        ResultActions result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)));

        // THEN: Verify response
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value("user-123"))
                .andExpect(jsonPath("$.result.username").value("john_doe"))
                .andExpect(jsonPath("$.result.firstName").value("John"))
                .andExpect(jsonPath("$.result.lastName").value("Doe"))
                .andExpect(jsonPath("$.result.roles").isArray())
                .andExpect(jsonPath("$.result.roles[0].name").value("USER"));

        // Verify service called
        verify(userService, times(1)).createUser(any(UserCreationRequest.class));
    }

    @Test
    void createUser_MissingUsername_ValidationError() throws Exception {
        // GIVEN: Invalid request
        creationRequest.setUsername(null);

        // WHEN: Call API
        ResultActions result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)));

        // THEN: Validation error
        result.andDo(print())
                .andExpect(status().isBadRequest());

        // Service NOT called
        verify(userService, never()).createUser(any(UserCreationRequest.class));
    }

    @Test
    void createUser_MissingPassword_ValidationError() throws Exception {
        creationRequest.setPassword(null);

        ResultActions rs = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)));
        rs.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_REQUIRED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_REQUIRED.getCode()));

        verify(userService, never()).createUser(any(UserCreationRequest.class));
    }


    @Test
    void createUser_ServiceThrowsException_InternalServerError() throws Exception {
        // GIVEN: Service throws exception
        when(userService.createUser(any(UserCreationRequest.class)))
                .thenThrow(new AppException(ErrorCode.USER_EXISTED));

        // WHEN: Call API
        ResultActions result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationRequest)));

        // THEN: Error response (depends on @ControllerAdvice)
        result.andDo(print());

        // Service WAS called
        verify(userService, times(1)).createUser(any(UserCreationRequest.class));
    }


}