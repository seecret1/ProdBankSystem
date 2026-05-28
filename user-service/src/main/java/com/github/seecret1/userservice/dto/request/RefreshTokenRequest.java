package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

    @NotBlank(message = "Refresh token must be set!")
    String refreshToken

) { }
