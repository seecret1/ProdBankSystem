package com.github.seecret1.userservice.controller;

import com.github.seecret1.userservice.aop.RequireUserStatus;
import com.github.seecret1.userservice.dto.request.*;
import com.github.seecret1.userservice.dto.response.JwtAuthenticationDto;
import com.github.seecret1.userservice.service.AuthService;
import com.github.seecret1.userservice.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in/email")
    @Operation(summary = "Sign in by email", description = "Authenticate user using email and password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = JwtAuthenticationDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<JwtAuthenticationDto> signInByEmail(
            @Valid @RequestBody SignInByEmailRequest request
    ) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/sign-in/username")
    @Operation(summary = "Sign in by username", description = "Authenticate user using username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<JwtAuthenticationDto> signInByUsername(
            @Valid @RequestBody SignInByUsernameRequest request
    ) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<JwtAuthenticationDto> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/sign-out")
    @RequireUserStatus
    @Operation(summary = "Sign out", description = "Logout user and invalidate refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found")
    })
    public ResponseEntity<Void> signOut(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.signOut(
                AuthUtil.getCurrentUserId(userDetails),
                request
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @RequireUserStatus
    @Operation(summary = "Sign out", description = "Logout user and invalidate refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found")
    })
    public ResponseEntity<JwtAuthenticationDto> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(authService.changePassword(
                AuthUtil.getCurrentUserId(userDetails),
                request
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New token generated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<JwtAuthenticationDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
