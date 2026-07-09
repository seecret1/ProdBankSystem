package com.github.seecret1.userservice.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.model.UserFilterModel;

public interface UserService {

    PageResponse<UserResponse> findAllUsers(PageModel pageModel);

    PageResponse<UserResponse> findAllActiveUsers(PageModel pageModel);

    PageResponse<UserResponse> findByFilter(UserFilterModel filter);

    UserResponse findById(String id);

    UserResponse findByEmail(String email);

    UserResponse findByUsername(String username);

    UserResponse findById(String criterial, String apiKey);

    UserResponse create(CreateUserRequest request);

    UserResponse updateFull(String criterial, CreateUserRequest request);

    UserResponse updateYour(String userId, UpdateUserRequest request);

    void delete(String deletedBy, String criterial);
}
