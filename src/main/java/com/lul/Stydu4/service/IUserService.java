package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.entity.UserEntity;

import java.util.List;

public interface IUserService {
    UserEntity createUser(UserCreationRequest userCreationRequest);
    UserEntity updateUser(String id, UserUpdateRequest userUpdateRequest);
    UserEntity getUserById(String id);
    void deleteUser(String id);
    List<UserEntity> getAllUsers();
}
