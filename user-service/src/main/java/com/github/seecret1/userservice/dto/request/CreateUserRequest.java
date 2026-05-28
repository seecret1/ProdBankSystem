package com.github.seecret1.userservice.dto.request;

import com.github.seecret1.userservice.entity.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateUserRequest(

    @NotBlank(message = "Username must be set!")
    @Size(min = 8, message = "Size of username must start from {min}")
    String username,

    @NotBlank(message = "Email must be set!")
    @Email(message = "Invalid email address")
    String email,

    @NotBlank(message = "Password must be set!")
    @Size(min = 8, message = "Size of password must start from {min}")
    String password,

    @NotBlank(message = "First name must be set!")
    @Size(max = 80, message = "Size of first name must start to {max}")
    String firstName,

    @NotBlank(message = "Last name must be set!")
    @Size(max = 100, message = "Size of first name must start to {max}")
    String lastName,

    String middleName,

    @NotNull(message = "birth date must be set!")
    LocalDate birthDate,

    @NotNull
    RoleType role
) { }
