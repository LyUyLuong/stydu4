package com.lul.Stydu4.repository;

import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, String> {
    boolean existsByUsername(String username);
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
    boolean existsByEmail(String email);
}
