package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.RoleEntity;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.enums.Role;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.UserMapper;
import com.lul.Stydu4.repository.IRoleRepository;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(IUserRepository userRepository, IRoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserCreationRequest userCreationRequest) {

        if (userRepository.existsByUsername(userCreationRequest.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        UserEntity userEntity = userMapper.toUserEntity(userCreationRequest);

        HashSet<RoleEntity> roles = new HashSet<>();
        RoleEntity usrRole = roleRepository.findById(Role.ADMIN.name()).orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        roles.add(usrRole);
        userEntity.setRoles(roles);

        userEntity.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));

        return userMapper.toUserResponse(userRepository.save(userEntity));
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateRequest userUpdateRequest) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUserEntity(userEntity, userUpdateRequest);
        userEntity.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));

        Set<RoleEntity> roles = new HashSet<>(roleRepository.findAllById(userUpdateRequest.getRoles()));
        userEntity.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(userEntity));
    }

    @Override
    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }


    @Override
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo() {
        UserEntity user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
}
