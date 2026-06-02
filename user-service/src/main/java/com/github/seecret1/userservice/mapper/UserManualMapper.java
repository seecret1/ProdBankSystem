package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.SignUpRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.entity.enums.RoleType;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class UserManualMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getStatus(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getBirthDate(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeleted(),
                user.getDeletedAt(),
                user.getDeletedBy()
        );
    }

    public CreateUserRequest toCreateUserRequest(SignUpRequest request) {
        return new CreateUserRequest(
                request.username(),
                UserStatus.PENDING_PROFILE,
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.birthDate(),
                RoleType.ROLE_USER
        );
    }

    public List<UserResponse> toListResponse(List<User> users) {
        List<UserResponse> list = new ArrayList<>(users.size());

        for (var response : users) {
            list.add(toResponse(response));
        }
        return list;
    }

    public User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setMiddleName(request.middleName());
        user.setBirthDate(request.birthDate());
        user.setStatus(request.status());
        user.setRole(request.role());
        user.setDeleted(false);
        return user;
    }
}
