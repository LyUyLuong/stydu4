package com.lul.Stydu4.configuration;

import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
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
            log.info("Initializing application data...");

            // 1. Init roles trước
            initRoles();

            // 2. Init admin user sau
            initAdminUser();

            log.info("Application initialization completed");
        };
    }

    /**
     * Khởi tạo các role mặc định nếu chưa tồn tại
     */
    @Transactional
    public void initRoles() {
        log.info("Checking roles...");

        for (Role role : Role.values()) {
            if (roleRepository.findById(role.name()).isEmpty()) {
                RoleEntity roleEntity = RoleEntity.builder()
                        .name(role.name())
                        .description("Default " + role.name() + " role")
                        .build();

                roleRepository.save(roleEntity);
                log.info("Created role: {}", role.name());
            }
        }

        log.info("Roles initialization completed");
    }

    /**
     * Khởi tạo admin user nếu chưa tồn tại
     */
    @Transactional
    public void initAdminUser() {
        log.info("Checking admin user...");

        if (userRepository.findByUsername("admin").isEmpty()) {
            // Load ADMIN role trong cùng transaction
            RoleEntity adminRole = roleRepository.findById(Role.ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            Set<RoleEntity> roles = new HashSet<>();
            roles.add(adminRole);

            UserEntity admin = UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .roles(roles)
                    .build();

            userRepository.save(admin);

            log.warn("==========================================================");
            log.warn("Admin user has been created with default credentials:");
            log.warn("Username: admin");
            log.warn("Password: admin");
            log.warn("PLEASE CHANGE PASSWORD IMMEDIATELY!");
            log.warn("==========================================================");
        } else {
            log.info("Admin user already exists");
        }
    }
}
