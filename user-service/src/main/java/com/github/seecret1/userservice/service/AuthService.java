package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.dto.request.RefreshTokenRequest;
import com.github.seecret1.userservice.dto.request.SignInByEmailRequest;
import com.github.seecret1.userservice.dto.request.SignInByUsernameRequest;
import com.github.seecret1.userservice.dto.request.SignUpRequest;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;

public interface AuthService {

    JwtAuthenticationDto signIn(SignInByEmailRequest request);

    JwtAuthenticationDto signIn(SignInByUsernameRequest request);

    JwtAuthenticationDto signUp(SignUpRequest request);

    void signOut(RefreshTokenRequest request);

    JwtAuthenticationDto refreshToken(RefreshTokenRequest request);
}
