package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

    @Size(min = 8, message = "Size of username must start from {min}")
    String username,

    @Email(message = "Invalid email address")
    String email,

    @Size(min = 8, message = "Size of password must start from {min}")
    String password

) { }
