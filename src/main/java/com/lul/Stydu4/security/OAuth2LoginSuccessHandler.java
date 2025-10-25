package com.lul.Stydu4.security;

import com.lul.Stydu4.dto.oauth2.GoogleOAuth2UserInfo;
import com.lul.Stydu4.dto.oauth2.OAuth2UserInfo;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IAuthenticationService authenticationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional  // ✅ Thêm @Transactional để tránh LazyInitializationException
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oauth2User.getAttributes());

        log.info("OAuth2 Login successful for email: {}", userInfo.getEmail());

        try {
            // Tìm hoặc tạo user
            UserEntity user = userRepository.findByEmailAndAuthProvider(
                    userInfo.getEmail(),
                    AuthProvider.GOOGLE
            ).orElseGet(() -> registerNewUser(userInfo));

            // ✅ Eager load roles và permissions để tránh lazy loading error
            user.getRoles().forEach(role -> {
                if (role.getPermissions() != null) {
                    role.getPermissions().size(); // Trigger lazy loading
                }
            });

            // Generate JWT token
            String jwtToken = authenticationService.generateTokenForOAuth2User(user);

            // Redirect về frontend với token
            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/login")
                    .queryParam("token", jwtToken)
                    .build()
                    .toUriString();

            log.info("Redirecting to: {}", redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 authentication", e);

            // Redirect về frontend với error
            String errorUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/login")
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();

            log.error("Redirecting to error page: {}", errorUrl);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    /**
     * Đăng ký user mới từ OAuth2
     */
    private UserEntity registerNewUser(OAuth2UserInfo userInfo) {
        log.info("Registering new OAuth2 user: {}", userInfo.getEmail());

        // Load USER role
        var defaultRole = roleRepository.findById("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        // ✅ Eager load permissions của role
        if (defaultRole.getPermissions() != null) {
            defaultRole.getPermissions().size();
        }

        // Tạo user mới
        UserEntity newUser = UserEntity.builder()
                .username(userInfo.getEmail())
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .authProvider(AuthProvider.GOOGLE)
                .providerId(userInfo.getProviderId())
                .password(null)
                .roles(new HashSet<>())
                .build();

        newUser.getRoles().add(defaultRole);

        return userRepository.save(newUser);
    }
}
