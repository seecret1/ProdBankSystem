package com.github.seecret1.cardservice.dto.user;

import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(

    String id,

    String username,

    String status,

    String email,

    String firstName,

    String lastName,

    String middleName,

    LocalDate birthDate,

    String role,

    Instant createdAt,

    Instant updatedAt,

    boolean deleted,

    Instant deletedAt,

    String deletedBy

) { }
