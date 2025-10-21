package com.lul.Stydu4.configuration;

import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import com.lul.Stydu4.enums.Role;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            log.info("========== APPLICATION INITIALIZATION START ==========");

            // 1. Init roles trước
            initRoles();

            // 2. Init admin user sau
            initAdminUser();

            log.info("========== APPLICATION INITIALIZATION COMPLETED ==========");
        };
    }

    /**
     * Khởi tạo các role mặc định nếu chưa tồn tại
     */
    @Transactional
    public void initRoles() {
        log.info("Checking roles...");

        int createdCount = 0;
        for (Role role : Role.values()) {
            if (!roleRepository.existsById(role.name())) {
                RoleEntity roleEntity = RoleEntity.builder()
                        .name(role.name())
                        .description("Default " + role.name() + " role")
                        .permissions(new HashSet<>())
                        .build();

                roleRepository.save(roleEntity);
                createdCount++;
                log.info("✓ Created role: {}", role.name());
            } else {
                log.debug("Role {} already exists", role.name());
            }
        }

        if (createdCount > 0) {
            log.info("Created {} new role(s)", createdCount);
        } else {
            log.info("All roles already exist");
        }
    }

    /**
     * Khởi tạo admin user nếu chưa tồn tại
     */
    @Transactional
    public void initAdminUser() {
        log.info("Checking admin user...");

        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists");
            return;
        }

        // Load ADMIN role trong cùng transaction
        RoleEntity adminRole = roleRepository.findById(Role.ADMIN.name())
                .orElseThrow(() -> new RuntimeException("ADMIN role not found. Please check Role enum."));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(adminRole);

        UserEntity admin = UserEntity.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .email("admin@stydu4.com")
                .firstName("System")
                .lastName("Administrator")
                .authProvider(AuthProvider.LOCAL)
                .roles(roles)
                .build();

        userRepository.save(admin);

        log.warn("╔════════════════════════════════════════════════════════╗");
        log.warn("║        DEFAULT ADMIN USER CREATED                      ║");
        log.warn("╠════════════════════════════════════════════════════════╣");
        log.warn("║  Username: admin                                       ║");
        log.warn("║  Password: admin                                       ║");
        log.warn("║  Email:    admin@stydu4.com                            ║");
        log.warn("╠════════════════════════════════════════════════════════╣");
        log.warn("║  SECURITY WARNING:                                     ║");
        log.warn("║  Please change the default password immediately!       ║");
        log.warn("╚════════════════════════════════════════════════════════╝");
    }
}
