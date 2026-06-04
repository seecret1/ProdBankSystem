package com.github.seecret1.userservice.service.impl;

import com.github.seecret1.userservice.dto.request.*;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.entity.User;
import com.github.seecret1.userservice.exception.AuthException;
import com.github.seecret1.userservice.exception.CheckPasswordException;
import com.github.seecret1.userservice.exception.PasswordUpdateException;
import com.github.seecret1.userservice.mapper.UserMapper;
import com.github.seecret1.userservice.repository.RefreshTokenRepository;
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

        checkPassword(request.password(), request.confirmPassword());

        userService.create(userMapper.toCreateUserRequest(request));
        log.debug("User successful sign up: {}", email);

        return jwtService.generateAuthToken(email);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void signOut(String userId, RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        var refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException(
                        "Refresh token not found!"
                ));

        if (refreshTokenEntity.isRevoked()) {
            throw new AuthException("This token is revoked");
        }

        refreshTokenRepository.revokeAllByUserId(userId);
        SecurityContextHolder.clearContext();

        log.debug("Successful user sign out. User id: {}", userId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public JwtAuthenticationDto changePassword(String userId, ChangePasswordRequest request) {

        log.info("Change password user: {}", userId);
        var user = internalUserService.findUserEntityById(userId);

        checkPassword(user, request);
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        log.debug("Revoke tokens and save user");
        refreshTokenRepository.revokeAllByUserId(userId);
        internalUserService.saveUser(user);

        log.debug("Successfully change user password");
        return jwtService.generateAuthToken(user.getEmail());
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

    private void checkPassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new CheckPasswordException("Password does not match");
        }
    }

    private void checkPassword(User user,ChangePasswordRequest request) {
        log.debug("Check current password user and current password request");
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordUpdateException("The password was entered incorrectly");
        }
        log.debug("Check password new password and confirm password request");
        checkPassword(request.newPassword(), request.confirmPassword());

        log.debug("Check password new password request and current user password");
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new PasswordUpdateException("New password must be different from current password");
        }
    }
}
