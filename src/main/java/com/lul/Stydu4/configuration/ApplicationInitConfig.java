package com.lul.Stydu4.configuration;


import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.Role;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepository;

    @Autowired
    ApplicationInitConfig(PasswordEncoder passwordEncoder, IRoleRepository roleRepository){
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Bean
    ApplicationRunner applicationRunner(IUserRepository userRepository) {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){

                HashSet<RoleEntity> roles = new HashSet<>();
                RoleEntity adRole = roleRepository.findById(Role.ADMIN.name()).orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
                roles.add(adRole);

                UserEntity admin = UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();

                userRepository.save(admin);
                log.warn("admin user has been created with default password: `admin`, please change password");
            }
        };
    };

}
