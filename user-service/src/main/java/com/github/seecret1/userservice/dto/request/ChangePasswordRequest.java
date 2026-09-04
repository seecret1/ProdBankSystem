package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Current password must be set!")
        String currentPassword,

        @NotBlank(message = "New password must be set!")
        @Size(min = 8, message = "Password must be at least {min} characters")
        String newPassword,

        @NotBlank(message = "Confirm password must be set!")
        @Size(min = 8, message = "Password must be at least {min} characters")
        String confirmPassword

) { }
