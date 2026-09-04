package com.github.seecret1.userservice.dto.response;

public record JwtAuthenticationDto(

    String token,

    String refreshToken

) { }
