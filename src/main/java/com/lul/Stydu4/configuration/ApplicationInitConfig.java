package com.lul.Stydu4.configuration;


import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.Role;
import com.lul.Stydu4.repository.IUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    ApplicationInitConfig(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    ApplicationRunner applicationRunner(IUserRepository userRepository) {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){

                Set<RoleEntity> roles = new HashSet<>();
                roles.add(Role.ADMIN.name());

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
