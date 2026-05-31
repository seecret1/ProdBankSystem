package com.github.seecret1.userservice.dto.response;

import com.github.seecret1.userservice.entity.RoleType;
import com.github.seecret1.userservice.entity.UserStatus;

import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(

    String id,

    String username,

    UserStatus status,

    String email,

    String firstName,

    String lastName,

    String middleName,

    LocalDate birthDate,

    RoleType role,

    Instant createdAt,

    Instant updatedAt,

    boolean deleted,

    Instant deletedAt,

    String deletedBy

) { }
