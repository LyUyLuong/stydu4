package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.enums.ErrorCode;
import com.lul.Stydu4.exception.AppException;
import com.lul.Stydu4.mapper.UserMapper;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(IUserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserEntity createUser(UserCreationRequest userCreationRequest) {

        if (userRepository.existsByUsername(userCreationRequest.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        UserEntity userEntity = userMapper.toUserEntity(userCreationRequest);

        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity updateUser(String id, UserUpdateRequest userUpdateRequest) {
        UserEntity userEntity = getUserById(id);
        userMapper.updateUserEntity(userEntity, userUpdateRequest);

        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
