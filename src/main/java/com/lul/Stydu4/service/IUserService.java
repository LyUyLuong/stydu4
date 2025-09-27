package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.UserCreationRequest;
import com.lul.Stydu4.dto.request.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;
import com.lul.Stydu4.entity.UserEntity;

import java.util.List;

public interface IUserService {
    UserResponse createUser(UserCreationRequest userCreationRequest);
    UserResponse updateUser(String id, UserUpdateRequest userUpdateRequest);
    UserResponse getUserById(String id);
    void deleteUser(String id);
    List<UserResponse> getAllUsers();
    UserResponse getMyInfo();
}
