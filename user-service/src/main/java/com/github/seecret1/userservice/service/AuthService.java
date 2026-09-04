package com.github.seecret1.userservice.service;

import com.github.seecret1.userservice.dto.request.*;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;

public interface AuthService {

    JwtAuthenticationDto signIn(SignInByEmailRequest request);

    JwtAuthenticationDto signIn(SignInByUsernameRequest request);

    JwtAuthenticationDto signUp(SignUpRequest request);

    void signOut(String userId, RefreshTokenRequest request);

    JwtAuthenticationDto changePassword(String userId, ChangePasswordRequest request);

    JwtAuthenticationDto refreshToken(RefreshTokenRequest request);
}
