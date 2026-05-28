package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInByEmailRequest(

    @NotBlank(message = "Email must be set!")
    @Email(message = "Invalid email address")
    String email,

    @NotBlank(message = "Password must be set!")
    @Size(min = 8, message = "Size of password must start from {min}")
    String password

) { }
