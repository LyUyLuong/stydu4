// package com.lul.Stydu4.integration;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.lul.Stydu4.dto.request.AuthenticationRequest;
// import com.lul.Stydu4.dto.request.User.UserCreationRequest;
// import com.lul.Stydu4.entity.RoleEntity;
// import com.lul.Stydu4.entity.UserEntity;
// import com.lul.Stydu4.enums.Role;
// import com.lul.Stydu4.repository.IRoleRepository;
// import com.lul.Stydu4.repository.IUserRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.util.Set;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// /**
//  * Integration Test cho Security & Authentication Flow
//  * 
//  * Test Cases:
//  * 1. Public endpoints accessible without auth
//  * 2. Protected endpoints require authentication
//  * 3. Role-based authorization (USER, ADMIN)
//  * 4. JWT token generation and validation
//  * 5. Login success/failure scenarios
//  */
// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @Transactional
// @DisplayName("Security Integration Tests")
// class SecurityIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private IUserRepository userRepository;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @Autowired
//     private IRoleRepository roleRepository;

//     private UserEntity testUser;
//     private UserEntity adminUser;
//     private RoleEntity userRole;
//     private RoleEntity adminRole;

//     @BeforeEach
//     void setUp() {
//         // Clean up
//         userRepository.deleteAll();

//         // Create roles if not exist
//         userRole = roleRepository.findById(Role.USER.name())
//                 .orElseGet(() -> {
//                     RoleEntity role = RoleEntity.builder()
//                             .name(Role.USER.name())
//                             .description("User role")
//                             .build();
//                     return roleRepository.save(role);
//                 });

//         adminRole = roleRepository.findById(Role.ADMIN.name())
//                 .orElseGet(() -> {
//                     RoleEntity role = RoleEntity.builder()
//                             .name(Role.ADMIN.name())
//                             .description("Admin role")
//                             .build();
//                     return roleRepository.save(role);
//                 });

//         // Create test user
//         testUser = UserEntity.builder()
//                 .username("testuser")
//                 .password(passwordEncoder.encode("Test123!"))
//                 .email("test@example.com")
//                 .firstName("Test")
//                 .lastName("User")
//                 .dob(LocalDate.of(2000, 1, 1))
//                 .roles(Set.of(userRole))
//                 .build();
//         userRepository.save(testUser);

//         // Create admin user
//         adminUser = UserEntity.builder()
//                 .username("adminuser")
//                 .password(passwordEncoder.encode("Admin123!"))
//                 .email("admin@example.com")
//                 .firstName("Admin")
//                 .lastName("User")
//                 .dob(LocalDate.of(1990, 1, 1))
//                 .roles(Set.of(adminRole))
//                 .build();
//         userRepository.save(adminUser);
//     }

//     // ========== Public Endpoints Tests ==========

//     @Nested
//     @DisplayName("Public Endpoints - No Auth Required")
//     class PublicEndpointsTests {

//         @Test
//         @DisplayName("GET /tests - should allow anonymous access")
//         void testPublicGetTestsEndpoint() throws Exception {
//             mockMvc.perform(get("/tests")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isOk());
//         }

//         @Test
//         @DisplayName("GET /files/{id} - should allow anonymous access")
//         void testPublicGetFilesEndpoint() throws Exception {
//             mockMvc.perform(get("/files/test-file-id")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isNotFound()); // 404 because file doesn't exist, not 401/403
//         }

//         @Test
//         @DisplayName("POST /users - registration should be public")
//         void testPublicRegistrationEndpoint() throws Exception {
//             UserCreationRequest request = UserCreationRequest.builder()
//                     .username("newuser")
//                     .password("NewUser123!")
//                     .email("newuser@example.com")
//                     .firstName("New")
//                     .lastName("User")
//                     .dob(LocalDate.of(1995, 6, 15))
//                     .build();

//             mockMvc.perform(post("/users")
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content(objectMapper.writeValueAsString(request)))
//                     .andDo(print())
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.result.username").value("newuser"));

//             // Verify user was created
//             assertThat(userRepository.findByUsername("newuser")).isPresent();
//         }
//     }

//     // ========== Authentication Tests ==========

//     @Nested
//     @DisplayName("Authentication Flow")
//     class AuthenticationTests {

//         @Test
//         @DisplayName("POST /auth/token - successful login returns JWT token")
//         void testSuccessfulLogin() throws Exception {
//             AuthenticationRequest request = AuthenticationRequest.builder()
//                     .username("testuser")
//                     .password("Test123!")
//                     .build();

//             mockMvc.perform(post("/auth/token")
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content(objectMapper.writeValueAsString(request)))
//                     .andDo(print())
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.result.token").exists())
//                     .andExpect(jsonPath("$.result.authenticated").value(true));
//         }

//         @Test
//         @DisplayName("POST /auth/token - wrong password returns 401")
//         void testLoginWithWrongPassword() throws Exception {
//             AuthenticationRequest request = AuthenticationRequest.builder()
//                     .username("testuser")
//                     .password("WrongPassword!")
//                     .build();

//             mockMvc.perform(post("/auth/token")
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content(objectMapper.writeValueAsString(request)))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }

//         @Test
//         @DisplayName("POST /auth/token - non-existent user returns 401")
//         void testLoginWithNonExistentUser() throws Exception {
//             AuthenticationRequest request = AuthenticationRequest.builder()
//                     .username("nonexistent")
//                     .password("Password123!")
//                     .build();

//             mockMvc.perform(post("/auth/token")
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content(objectMapper.writeValueAsString(request)))
//                     .andDo(print())
//                     .andExpect(status().isNotFound());
//         }
//     }

//     // ========== Protected Endpoints Tests ==========

//     @Nested
//     @DisplayName("Protected Endpoints - Auth Required")
//     class ProtectedEndpointsTests {

//         @Test
//         @DisplayName("GET /users/myInfo - anonymous access should return 401")
//         void testProtectedEndpointWithoutAuth() throws Exception {
//             mockMvc.perform(get("/users/myInfo")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }

//         @Test
//         @DisplayName("GET /users/myInfo - with valid JWT should succeed")
//         @WithMockUser(username = "testuser", roles = {"USER"})
//         void testProtectedEndpointWithAuth() throws Exception {
//             mockMvc.perform(get("/users/myInfo")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.result.username").value("testuser"));
//         }

//         @Test
//         @DisplayName("POST /courses - requires authentication")
//         void testCreateCourseWithoutAuth() throws Exception {
//             mockMvc.perform(post("/courses")
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content("{}"))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }

//         @Test
//         @DisplayName("PUT /users/{userId} - requires authentication")
//         void testUpdateUserWithoutAuth() throws Exception {
//             mockMvc.perform(put("/users/" + testUser.getId())
//                             .contentType(MediaType.APPLICATION_JSON)
//                             .content("{}"))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }
//     }

//     // ========== Role-Based Authorization Tests ==========

//     @Nested
//     @DisplayName("Role-Based Authorization")
//     class RoleBasedAuthorizationTests {

//         @Test
//         @DisplayName("Admin endpoints - USER role should be denied")
//         @WithMockUser(username = "testuser", roles = {"USER"})
//         void testAdminEndpointWithUserRole() throws Exception {
//             mockMvc.perform(get("/users")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isForbidden());
//         }

//         @Test
//         @DisplayName("Admin endpoints - ADMIN role should succeed")
//         @WithMockUser(username = "adminuser", roles = {"ADMIN"})
//         void testAdminEndpointWithAdminRole() throws Exception {
//             mockMvc.perform(get("/users")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isOk());
//         }

//         @Test
//         @DisplayName("User can only access their own data")
//         @WithMockUser(username = "testuser", roles = {"USER"})
//         void testUserCanOnlyAccessOwnData() throws Exception {
//             // User can access their own info
//             mockMvc.perform(get("/users/myInfo")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isOk())
//                     .andExpect(jsonPath("$.result.username").value("testuser"));

//             // User cannot access other user's data directly via GET /users (admin endpoint)
//             mockMvc.perform(get("/users")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isForbidden());
//         }
//     }

//     // ========== JWT Token Validation Tests ==========

//     @Nested
//     @DisplayName("JWT Token Validation")
//     class JwtTokenValidationTests {

//         @Test
//         @DisplayName("Invalid JWT token should return 401")
//         void testInvalidJwtToken() throws Exception {
//             mockMvc.perform(get("/users/myInfo")
//                             .header("Authorization", "Bearer invalid.jwt.token")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }

//         @Test
//         @DisplayName("Malformed Authorization header should return 401")
//         void testMalformedAuthorizationHeader() throws Exception {
//             mockMvc.perform(get("/users/myInfo")
//                             .header("Authorization", "InvalidFormat token")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }

//         @Test
//         @DisplayName("Missing Authorization header should return 401")
//         void testMissingAuthorizationHeader() throws Exception {
//             mockMvc.perform(get("/users/myInfo")
//                             .contentType(MediaType.APPLICATION_JSON))
//                     .andDo(print())
//                     .andExpect(status().isUnauthorized());
//         }
//     }

//     // ========== CORS Configuration Tests ==========

//     @Nested
//     @DisplayName("CORS Configuration")
//     class CorsConfigurationTests {

//         @Test
//         @DisplayName("OPTIONS request should return CORS headers")
//         void testCorsPreflightRequest() throws Exception {
//             mockMvc.perform(options("/auth/token")
//                             .header("Origin", "http://localhost:5173")
//                             .header("Access-Control-Request-Method", "POST"))
//                     .andDo(print())
//                     .andExpect(status().isOk())
//                     .andExpect(header().exists("Access-Control-Allow-Origin"));
//         }
//     }
// }
