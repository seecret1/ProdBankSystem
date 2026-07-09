package com.github.seecret1.userservice.controller.publics;

import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.service.UserService;
import com.github.seecret1.userservice.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "API for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserPublicController {

    private static final String X_INTERNAL_API_KEY = "X-Internal-Api-Key";

    private final UserService userService;

    @GetMapping("/services/{id}")
    public UserResponse findUserById(
            @PathVariable String id,
            @RequestHeader(value = X_INTERNAL_API_KEY, required = false) String apiKey
    ) {
        return userService.findById(id, apiKey);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Update own profile", description = "Update current user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success update user`s profile"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponse> updateYour(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.updateYour(
                AuthUtil.getCurrentUserId(userDetails),
                request
        ));
    }
}
