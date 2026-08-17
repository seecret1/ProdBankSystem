package com.github.seecret1.office_service.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

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
