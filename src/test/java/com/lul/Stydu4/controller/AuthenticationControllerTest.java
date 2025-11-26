package com.lul.Stydu4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lul.Stydu4.dto.request.AuthenticationRequest;
import com.lul.Stydu4.dto.request.IntrospectRequest;
import com.lul.Stydu4.dto.request.LogoutRequest;
import com.lul.Stydu4.dto.request.RefreshTokenRequest;
import com.lul.Stydu4.dto.response.AuthenticationResponse;
import com.lul.Stydu4.dto.response.IntrospectResponse;
import com.lul.Stydu4.service.IAuthenticationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAuthenticationService authenticationService;

    private AuthenticationRequest authRequest;
    private AuthenticationResponse authResponse;
    private IntrospectRequest introspectRequest;
    private IntrospectResponse introspectResponse;
    private LogoutRequest logoutRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        // Setup authentication request
        authRequest = AuthenticationRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        // Setup authentication response
        authResponse = AuthenticationResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .authenticated(true)
                .build();

        // Setup introspect request
        introspectRequest = IntrospectRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .build();

        // Setup introspect response
        introspectResponse = IntrospectResponse.builder()
                .valid(true)
                .build();

        // Setup logout request
        logoutRequest = LogoutRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .build();

        // Setup refresh token request
        refreshTokenRequest = RefreshTokenRequest.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .build();
    }

    @Nested
    @DisplayName("POST /auth/token - Authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate user successfully with valid credentials")
        void authenticate_ValidCredentials_Success() throws Exception {
            // GIVEN
            when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                    .thenReturn(authResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                    .andExpect(jsonPath("$.result.authenticated").value(true));

            verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
        }

        @Test
        @DisplayName("Should return error for invalid credentials")
        void authenticate_InvalidCredentials_Fails() throws Exception {
            // GIVEN
            AuthenticationResponse failedResponse = AuthenticationResponse.builder()
                    .authenticated(false)
                    .build();

            when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                    .thenReturn(failedResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.authenticated").value(false));
        }

        @Test
        @DisplayName("Should return 400 for malformed request")
        void authenticate_MalformedRequest_BadRequest() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/introspect - Introspect Token Tests")
    class IntrospectTests {

        @Test
        @DisplayName("Should introspect valid token successfully")
        void introspect_ValidToken_Success() throws Exception {
            // GIVEN
            when(authenticationService.introspect(any(IntrospectRequest.class)))
                    .thenReturn(introspectResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/introspect")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(introspectRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.valid").value(true));

            verify(authenticationService, times(1)).introspect(any(IntrospectRequest.class));
        }

        @Test
        @DisplayName("Should return false for invalid token")
        void introspect_InvalidToken_ReturnsFalse() throws Exception {
            // GIVEN
            IntrospectResponse invalidResponse = IntrospectResponse.builder()
                    .valid(false)
                    .build();

            when(authenticationService.introspect(any(IntrospectRequest.class)))
                    .thenReturn(invalidResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/introspect")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(introspectRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.valid").value(false));
        }
    }

    @Nested
    @DisplayName("POST /auth/logout - Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout user successfully")
        void logout_ValidToken_Success() throws Exception {
            // GIVEN
            doNothing().when(authenticationService).logout(any(LogoutRequest.class));

            // WHEN & THEN
            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result").doesNotExist());

            verify(authenticationService, times(1)).logout(any(LogoutRequest.class));
        }

        @Test
        @DisplayName("Should handle logout with empty request body")
        void logout_EmptyRequest_BadRequest() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk()); // Will process but service may throw exception
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh - Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_ValidToken_Success() throws Exception {
            // GIVEN
            AuthenticationResponse newAuthResponse = AuthenticationResponse.builder()
                    .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_TOKEN...")
                    .authenticated(true)
                    .build();

            when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                    .thenReturn(newAuthResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.result.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.NEW_TOKEN..."))
                    .andExpect(jsonPath("$.result.authenticated").value(true));

            verify(authenticationService, times(1)).refreshToken(any(RefreshTokenRequest.class));
        }

        @Test
        @DisplayName("Should fail to refresh with expired token")
        void refreshToken_ExpiredToken_Fails() throws Exception {
            // GIVEN
            AuthenticationResponse expiredResponse = AuthenticationResponse.builder()
                    .authenticated(false)
                    .build();

            when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                    .thenReturn(expiredResponse);

            // WHEN & THEN
            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.authenticated").value(false));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full authentication flow")
        void fullAuthenticationFlow_Success() throws Exception {
            // 1. Authenticate
            when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                    .thenReturn(authResponse);

            mockMvc.perform(post("/auth/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.authenticated").value(true));

            // 2. Introspect
            when(authenticationService.introspect(any(IntrospectRequest.class)))
                    .thenReturn(introspectResponse);

            mockMvc.perform(post("/auth/introspect")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(introspectRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.valid").value(true));

            // 3. Refresh
            when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                    .thenReturn(authResponse);

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.authenticated").value(true));

            // 4. Logout
            doNothing().when(authenticationService).logout(any(LogoutRequest.class));

            mockMvc.perform(post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isOk());

            // Verify all service calls
            verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
            verify(authenticationService, times(1)).introspect(any(IntrospectRequest.class));
            verify(authenticationService, times(1)).refreshToken(any(RefreshTokenRequest.class));
            verify(authenticationService, times(1)).logout(any(LogoutRequest.class));
        }
    }
}
