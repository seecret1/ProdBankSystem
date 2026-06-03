package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.dto.request.RefreshTokenRequest;
import com.github.seecret1.userservice.dto.request.SignInByEmailRequest;
import com.github.seecret1.userservice.dto.request.SignInByUsernameRequest;
import com.github.seecret1.userservice.dto.request.SignUpRequest;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.repository.RefreshTokenRepository;
import com.github.seecret1.userservice.security.CustomUserDetails;
import com.github.seecret1.userservice.security.jwt.JwtService;
import com.github.seecret1.userservice.service.AuthService;
import com.github.seecret1.userservice.service.InternalUserService;
import com.github.seecret1.userservice.service.UserService;
import com.github.seecret1.userservice.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final UserMapper userMapper;

    private final InternalUserService internalUserService;

    private final JwtService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public JwtAuthenticationDto signIn(SignInByEmailRequest request) {
        log.info("Sign in user by email: {}", request.email());
        var user = internalUserService.findUserEntityByCriterial(request.email());
        AuthUtil.checkValidUser(user);
        log.debug("Success sign in user by email. User: {}", user);
        return authenticate(user, request.password());
    }

    @Override
    @Transactional
    public JwtAuthenticationDto signIn(SignInByUsernameRequest request) {
        log.info("Sign in user by username: {}", request.username());
        var user = internalUserService.findUserEntityByCriterial(request.username());
        AuthUtil.checkValidUser(user);
        log.debug("Success sign in user by username. User: {}", user);
        return authenticate(user, request.password());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public JwtAuthenticationDto signUp(SignUpRequest request) {
        String email = request.email();
        log.info("Sign up user. User email: {}; username: {}",
                email, request.username());

        userService.create(userMapper.toCreateUserRequest(request));
        log.debug("User successful sign up: {}", email);

        return jwtService.generateAuthToken(email);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void signOut(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        var refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException(
                        "Refresh token not found!"
                ));

        if (refreshTokenEntity.isRevoked()) {
            throw new AuthException("User sign out using this token");
        }

        CustomUserDetails userDetails = AuthUtil.getAuthenticatedUser();
        log.info("Sign out user: {}", userDetails.getUsername());

        if (!refreshTokenEntity.getUser().getId().equals(userDetails.getId())) {
            throw new AuthException("Refresh token does not belong to current user");
        }

        refreshTokenRepository.revokeByToken(refreshToken);
        SecurityContextHolder.clearContext();

        log.debug("Successful user sign out. User: {}", userDetails);
    }

    @Override
    @Transactional
    public JwtAuthenticationDto refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.refreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new AuthException("Invalid or expired refresh token");
        }

        String email = jwtService.getEmailFromToken(refreshToken);
        var user = internalUserService.findUserEntityByCriterial(email);
        AuthUtil.checkValidUser(user);
        log.info("User requested refresh token by email: {}", email);

        return jwtService.refreshBaseToken(email, refreshToken);
    }

    private JwtAuthenticationDto authenticate(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }
        log.info("User successfully authenticated: {}, created new session", user.getEmail());
        return jwtService.generateAuthToken(user.getEmail());
    }
}
