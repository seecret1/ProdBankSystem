package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignUpRequest(

    @NotBlank(message = "Username must be set!")
    @Size(min = 8, message = "Size of username must start from {min}")
    String username,

    @NotBlank(message = "Email must be set!")
    @Email(message = "Invalid email address")
    String email,

    @Size(min = 8, max = 100, message = "Password must be between {min} and {max} characters")
    @NotBlank(message = "Password must be set!")
    String password,

    @Size(min = 8, max = 100, message = "Password must be between {min} and {max} characters")
    @NotBlank(message = "Confirm password must be set!")
    String confirmPassword,

    @NotBlank(message = "First name must be set!")
    @Size(max = 64, message = "Size of first name must start to {max}")
    String firstName,

    @NotBlank(message = "Last name must be set!")
    @Size(max = 64, message = "Size of last name must start to {max}")
    String lastName,

    String middleName,

    @NotNull(message = "Birth date must be set!")
    LocalDate birthDate

) { }
