package com.lul.Stydu4.service;

import com.lul.Stydu4.dto.request.User.UserCreationRequest;
import com.lul.Stydu4.dto.request.User.UserUpdateRequest;
import com.lul.Stydu4.dto.response.UserResponse;

import java.util.List;


public interface IUserService {
    UserResponse createUser(UserCreationRequest userCreationRequest);
    UserResponse updateUser(String id, UserUpdateRequest userUpdateRequest);
    UserResponse getUserById(String id);
    void deleteUser(String id);
    List<UserResponse> getAllUsers();
    UserResponse getMyInfo();
}
