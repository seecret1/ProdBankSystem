package com.github.seecret1.userservice.mapper;


import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.SignUpRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR
)
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toListResponse(List<User> users);

    @Mapping(target = "status", constant = "PENDING_PROFILE")
    @Mapping(target = "role", constant = "ROLE_USER")
    CreateUserRequest toCreateUserRequest(SignUpRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "individual", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);
}
