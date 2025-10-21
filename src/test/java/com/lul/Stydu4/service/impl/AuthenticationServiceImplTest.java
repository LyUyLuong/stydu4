package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.AuthenticationRequest;
import com.lul.Stydu4.dto.request.IntrospectRequest;
import com.lul.Stydu4.dto.request.LogoutRequest;
import com.lul.Stydu4.dto.request.RefreshTokenRequest;
import com.lul.Stydu4.dto.response.AuthenticationResponse;
import com.lul.Stydu4.dto.response.IntrospectResponse;
import com.lul.Stydu4.entity.PermissionEntity;
import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.UserMapper;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IJwtBlacklistService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Tests")
class AuthenticationServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private IJwtBlacklistService jwtBlacklistService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserEntity localUser;
    private UserEntity oauth2User;
    private RoleEntity userRole;
    private PermissionEntity viewPermission;

    // Test JWT configuration
    private static final String TEST_SIGNER_KEY = "test-secret-key-at-least-64-bytes-long-for-jwt-hs512-signing-algorithm-requirement";
    private static final long VALID_DURATION = 3600; // 1 hour
    private static final long REFRESHABLE_DURATION = 36000; // 10 hours

    @BeforeEach
    void setUp() {
        // Inject test values using ReflectionTestUtils
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", TEST_SIGNER_KEY);
        ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", VALID_DURATION);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", REFRESHABLE_DURATION);

        // Setup permission
        viewPermission = PermissionEntity.builder()
                .name("VIEW_USER")
                .description("View user permission")
                .build();

        // Setup role
        userRole = RoleEntity.builder()
                .name("USER")
                .description("User role")
                .permissions(new HashSet<>(Set.of(viewPermission)))
                .build();

        // Setup LOCAL user
        localUser = UserEntity.builder()
                .id("user-123")
                .username("john_doe")
                .password(new BCryptPasswordEncoder(10).encode("password123"))
                .email("john@example.com")
                .authProvider(AuthProvider.LOCAL)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        // Setup OAuth2 user
        oauth2User = UserEntity.builder()
                .id("user-456")
                .username("jane_doe@gmail.com")
                .email("jane_doe@gmail.com")
                .authProvider(AuthProvider.GOOGLE)
                .providerId("google-123")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate LOCAL user with valid credentials")
        void authenticate_ValidCredentials_Success() {
            // GIVEN
            AuthenticationRequest request = AuthenticationRequest.builder()
                    .username("john_doe")
                    .password("password123")
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(localUser));

            // WHEN
            AuthenticationResponse response = authenticationService.authenticate(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.isAuthenticated()).isTrue();
            assertThat(response.getToken()).isNotNull();
            assertThat(response.getToken()).contains("eyJ"); // JWT starts with eyJ

            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void authenticate_UserNotFound_ThrowException() {
            // GIVEN
            AuthenticationRequest request = AuthenticationRequest.builder()
                    .username("unknown_user")
                    .password("password123")
                    .build();

            when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.authenticate(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

            verify(userRepository).findByUsername("unknown_user");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void authenticate_WrongPassword_ThrowException() {
            // GIVEN
            AuthenticationRequest request = AuthenticationRequest.builder()
                    .username("john_doe")
                    .password("wrong_password")
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(localUser));

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.authenticate(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);

            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw exception when trying to authenticate OAuth2 user with password")
        void authenticate_OAuth2User_ThrowException() {
            // GIVEN
            AuthenticationRequest request = AuthenticationRequest.builder()
                    .username("jane_doe@gmail.com")
                    .password("any_password")
                    .build();

            when(userRepository.findByUsername("jane_doe@gmail.com")).thenReturn(Optional.of(oauth2User));

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.authenticate(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

            verify(userRepository).findByUsername("jane_doe@gmail.com");
        }
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate valid JWT token for user")
        void generateToken_ValidUser_Success() throws ParseException {
            // WHEN
            String token = authenticationService.generateToken(localUser);

            // THEN
            assertThat(token).isNotNull();
            assertThat(token).startsWith("eyJ"); // JWT format

            // Parse and verify token claims
            SignedJWT signedJWT = SignedJWT.parse(token);
            assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("john_doe");
            assertThat(signedJWT.getJWTClaimsSet().getIssuer()).isEqualTo("Stydu4");
            assertThat(signedJWT.getJWTClaimsSet().getClaim("scope")).isNotNull();
            assertThat(signedJWT.getJWTClaimsSet().getJWTID()).isNotNull();
        }

        @Test
        @DisplayName("Should include roles and permissions in token scope")
        void generateToken_IncludesScope_Success() throws ParseException {
            // WHEN
            String token = authenticationService.generateToken(localUser);

            // THEN
            SignedJWT signedJWT = SignedJWT.parse(token);
            String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");

            assertThat(scope).contains("ROLE_USER");
            assertThat(scope).contains("VIEW_USER");
        }

        @Test
        @DisplayName("Should handle user with null roles gracefully")
        void generateToken_NullRoles_Success() {
            // GIVEN
            UserEntity userWithoutRoles = UserEntity.builder()
                    .id("user-789")
                    .username("no_role_user")
                    .email("norole@example.com")
                    .authProvider(AuthProvider.LOCAL)
                    .roles(null)
                    .build();

            // WHEN
            String token = authenticationService.generateToken(userWithoutRoles);

            // THEN
            assertThat(token).isNotNull();
            assertThat(token).startsWith("eyJ");
        }

        @Test
        @DisplayName("Should handle user with empty roles")
        void generateToken_EmptyRoles_Success() {
            // GIVEN
            UserEntity userWithEmptyRoles = UserEntity.builder()
                    .id("user-789")
                    .username("no_role_user")
                    .email("norole@example.com")
                    .authProvider(AuthProvider.LOCAL)
                    .roles(new HashSet<>())
                    .build();

            // WHEN
            String token = authenticationService.generateToken(userWithEmptyRoles);

            // THEN
            assertThat(token).isNotNull();
            assertThat(token).startsWith("eyJ");
        }
    }

    @Nested
    @DisplayName("generateTokenForOAuth2User Tests")
    class GenerateTokenForOAuth2UserTests {

        @Test
        @DisplayName("Should generate valid JWT token for OAuth2 user")
        void generateTokenForOAuth2User_ValidUser_Success() throws ParseException {
            // WHEN
            String token = authenticationService.generateTokenForOAuth2User(oauth2User);

            // THEN
            assertThat(token).isNotNull();
            assertThat(token).startsWith("eyJ");

            // Parse and verify OAuth2 specific claims
            SignedJWT signedJWT = SignedJWT.parse(token);
            assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("jane_doe@gmail.com");
            assertThat(signedJWT.getJWTClaimsSet().getIssuer()).isEqualTo("stydu4.com");
            assertThat(signedJWT.getJWTClaimsSet().getStringClaim("userId")).isEqualTo("user-456");
            assertThat(signedJWT.getJWTClaimsSet().getStringClaim("authProvider")).isEqualTo("GOOGLE");
            assertThat(signedJWT.getJWTClaimsSet().getClaim("scope")).isNotNull();
        }

        @Test
        @DisplayName("Should include correct authProvider in token")
        void generateTokenForOAuth2User_IncludesAuthProvider_Success() throws ParseException {
            // WHEN
            String token = authenticationService.generateTokenForOAuth2User(oauth2User);

            // THEN
            SignedJWT signedJWT = SignedJWT.parse(token);
            assertThat(signedJWT.getJWTClaimsSet().getStringClaim("authProvider")).isEqualTo("GOOGLE");
        }
    }

    @Nested
    @DisplayName("buildScope Tests")
    class BuildScopeTests {

        @Test
        @DisplayName("Should build scope with roles and permissions")
        void buildScope_WithRolesAndPermissions_Success() {
            // WHEN
            String scope = authenticationService.buildScope(localUser);

            // THEN
            assertThat(scope).contains("ROLE_USER");
            assertThat(scope).contains("VIEW_USER");
        }

        @Test
        @DisplayName("Should handle user with null roles")
        void buildScope_NullRoles_EmptyScope() {
            // GIVEN
            UserEntity userWithoutRoles = UserEntity.builder()
                    .username("test")
                    .roles(null)
                    .build();

            // WHEN
            String scope = authenticationService.buildScope(userWithoutRoles);

            // THEN
            assertThat(scope).isEmpty();
        }

        @Test
        @DisplayName("Should handle role with null permissions")
        void buildScope_RoleWithNullPermissions_OnlyRoleScope() {
            // GIVEN
            RoleEntity roleWithoutPermissions = RoleEntity.builder()
                    .name("ADMIN")
                    .permissions(null)
                    .build();

            UserEntity user = UserEntity.builder()
                    .username("admin")
                    .roles(new HashSet<>(Set.of(roleWithoutPermissions)))
                    .build();

            // WHEN
            String scope = authenticationService.buildScope(user);

            // THEN
            assertThat(scope).isEqualTo("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("introspect Tests")
    class IntrospectTests {

        @Test
        @DisplayName("Should return valid for valid token")
        void introspect_ValidToken_ReturnsTrue() throws JOSEException, ParseException {
            // GIVEN
            String validToken = authenticationService.generateToken(localUser);
            IntrospectRequest request = IntrospectRequest.builder()
                    .token(validToken)
                    .build();

            // WHEN
            IntrospectResponse response = authenticationService.introspect(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.isValid()).isTrue();
        }

        @Test
        @DisplayName("Should return invalid for malformed token")
        void introspect_MalformedToken_ReturnsFalse() throws JOSEException, ParseException {
            // GIVEN
            IntrospectRequest request = IntrospectRequest.builder()
                    .token("invalid.token.here")
                    .build();

            // WHEN
            IntrospectResponse response = authenticationService.introspect(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should return invalid for expired token")
        void introspect_ExpiredToken_ReturnsFalse() throws JOSEException, ParseException {
            // GIVEN - Set very short expiry for testing
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", -1L);
            String expiredToken = authenticationService.generateToken(localUser);
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", VALID_DURATION);

            IntrospectRequest request = IntrospectRequest.builder()
                    .token(expiredToken)
                    .build();

            // WHEN
            IntrospectResponse response = authenticationService.introspect(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully with valid token")
        void logout_ValidToken_Success() {
            // GIVEN
            String validToken = authenticationService.generateToken(localUser);
            LogoutRequest request = LogoutRequest.builder()
                    .token(validToken)
                    .build();

            doNothing().when(jwtBlacklistService).blacklistToken(anyString(), anyLong());

            // WHEN & THEN
            assertThatCode(() -> authenticationService.logout(request))
                    .doesNotThrowAnyException();

            verify(jwtBlacklistService).blacklistToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should handle logout with invalid token gracefully")
        void logout_InvalidToken_NoException() {
            // GIVEN
            LogoutRequest request = LogoutRequest.builder()
                    .token("invalid.token.here")
                    .build();

            // WHEN & THEN
            assertThatCode(() -> authenticationService.logout(request))
                    .doesNotThrowAnyException();

            verify(jwtBlacklistService, never()).blacklistToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should not blacklist expired token")
        void logout_ExpiredToken_NotBlacklisted() {
            // GIVEN - Create expired token
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", -1L);
            String expiredToken = authenticationService.generateToken(localUser);
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", VALID_DURATION);

            LogoutRequest request = LogoutRequest.builder()
                    .token(expiredToken)
                    .build();

            // WHEN
            authenticationService.logout(request);

            // THEN
            verify(jwtBlacklistService, never()).blacklistToken(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("refreshToken Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_ValidToken_Success() throws ParseException, JOSEException {
            // GIVEN
            String originalToken = authenticationService.generateToken(localUser);
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .token(originalToken)
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(localUser));
            doNothing().when(jwtBlacklistService).blacklistToken(anyString(), anyLong());

            // WHEN
            AuthenticationResponse response = authenticationService.refreshToken(request);

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.isAuthenticated()).isTrue();
            assertThat(response.getToken()).isNotNull();
            assertThat(response.getToken()).isNotEqualTo(originalToken); // New token

            verify(userRepository).findByUsername("john_doe");
            verify(jwtBlacklistService).blacklistToken(anyString(), anyLong());
        }

        @Test
        @DisplayName("Should throw exception when user not found during refresh")
        void refreshToken_UserNotFound_ThrowException() throws ParseException, JOSEException {
            // GIVEN
            String token = authenticationService.generateToken(localUser);
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .token(token)
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);

            verify(userRepository).findByUsername("john_doe");
        }

        @Test
        @DisplayName("Should throw exception for expired refresh token")
        void refreshToken_ExpiredToken_ThrowException() {
            // GIVEN - Create expired token
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", -1L);
            ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", -1L);
            String expiredToken = authenticationService.generateToken(localUser);
            ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", VALID_DURATION);
            ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", REFRESHABLE_DURATION);

            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .token(expiredToken)
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void refreshToken_MalformedToken_ThrowException() {
            // GIVEN
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .token("malformed.token.here")
                    .build();

            // WHEN & THEN
            assertThatThrownBy(() -> authenticationService.refreshToken(request))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Token Integration Tests")
    class TokenIntegrationTests {

        @Test
        @DisplayName("Should complete full authentication flow")
        void fullAuthenticationFlow_Success() throws ParseException, JOSEException {
            // GIVEN
            AuthenticationRequest authRequest = AuthenticationRequest.builder()
                    .username("john_doe")
                    .password("password123")
                    .build();

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(localUser));
            doNothing().when(jwtBlacklistService).blacklistToken(anyString(), anyLong());

            // WHEN - Authenticate
            AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);

            // THEN - Verify authentication
            assertThat(authResponse.isAuthenticated()).isTrue();
            String token = authResponse.getToken();
            assertThat(token).isNotNull();

            // WHEN - Introspect token
            IntrospectRequest introspectRequest = IntrospectRequest.builder()
                    .token(token)
                    .build();
            IntrospectResponse introspectResponse = authenticationService.introspect(introspectRequest);

            // THEN - Token should be valid
            assertThat(introspectResponse.isValid()).isTrue();

            // WHEN - Refresh token
            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .token(token)
                    .build();
            AuthenticationResponse refreshResponse = authenticationService.refreshToken(refreshRequest);

            // THEN - New token should be issued
            assertThat(refreshResponse.getToken()).isNotNull();
            assertThat(refreshResponse.getToken()).isNotEqualTo(token);

            // WHEN - Logout
            LogoutRequest logoutRequest = LogoutRequest.builder()
                    .token(token)
                    .build();

            // THEN - Should logout successfully
            assertThatCode(() -> authenticationService.logout(logoutRequest))
                    .doesNotThrowAnyException();
        }
    }
}
