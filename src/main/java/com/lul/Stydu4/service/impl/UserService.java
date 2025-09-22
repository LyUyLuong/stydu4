package com.lul.Stydu4.service.impl;

import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.entity.UserEntity;
import com.lul.Stydu4.repository.IUserRepository;
import com.lul.Stydu4.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity createUser(UserCreationRequest userCreationRequest) {
        UserEntity userEntity = new UserEntity();

        if (userRepository.existsByUsername(userCreationRequest.getUsername()))
            throw new RuntimeException("User existed.");

        userEntity.setUsername(userCreationRequest.getUsername());
        userEntity.setPassword(userCreationRequest.getPassword());
        userEntity.setFirstName(userCreationRequest.getFirstName());
        userEntity.setLastName(userCreationRequest.getLastName());
        userEntity.setDob(userCreationRequest.getDob());

        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity updateUser(String id, UserUpdateRequest userUpdateRequest) {
        UserEntity userEntity = getUserById(id);

        userEntity.setFirstName(userUpdateRequest.getFirstName());
        userEntity.setLastName(userUpdateRequest.getLastName());
        userEntity.setPassword(userUpdateRequest.getPassword());
        userEntity.setDob(userUpdateRequest.getDob());
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
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
