package com.github.seecret1.userservice.controller;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.CreateUserRequest;
import com.github.seecret1.userservice.dto.request.UpdateUserRequest;
import com.github.seecret1.userservice.dto.response.UserResponse;
import com.github.seecret1.userservice.model.UserFilterModel;
import com.github.seecret1.userservice.service.UserService;
import com.github.seecret1.userservice.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve paginated list of all users (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get all users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(userService.findAllUsers(pageModel));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all active users", description = "Retrieve paginated list of all active users (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get all active users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<UserResponse>> findAllActiveUsers(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(userService.findAllActiveUsers(pageModel));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get users by filter", description = "Retrieve paginated list of all users by filter (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get users by filter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<UserResponse>> findByFilter(
            @Valid UserFilterModel filter
    ) {
        return ResponseEntity.ok(userService.findByFilter(filter));
    }

    @GetMapping("/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get user by criterial", description = "Find user by ID, username or email (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get user by ID, username or email"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponse> findByCriterial(
            @PathVariable String criterial
    ) {
        return ResponseEntity.ok(userService.findByCriterial(criterial));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create user", description = "Create new user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success create new user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request));
    }

    @PutMapping("/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Full update user", description = "Update all user fields (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success full update"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<UserResponse> updateFull(
            @PathVariable String criterial,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateFull(criterial, request));
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
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

    @DeleteMapping("/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete user", description = "Delete user by ID, username or email (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Success delete user by ID, username or email"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> delete(
            @PathVariable String criterial,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.delete(
                AuthUtil.getCurrentUserId(userDetails),
                criterial
        );
        return ResponseEntity.noContent().build();
    }
}
