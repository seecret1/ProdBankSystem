package com.github.seecret1.userservice.controller.publics;

import com.github.seecret1.userservice.aop.RequireUserStatus;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.entity.enums.UserStatus;
import com.github.seecret1.userservice.service.IndividualService;
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
@RequestMapping("/api/v1/public/individuals")
@Tag(name = "Individual Management", description = "API for bank retail clients (individuals)")
public class IndividualPublicController {

    private final IndividualService individualService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @RequireUserStatus(allowed = UserStatus.PENDING_PROFILE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Complete client profile", description = "Link personal data to the authenticated user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Individual successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "User, individual or address already exists")
    })
    public ResponseEntity<IndividualResponse> recordPersonalData(
            @Valid @RequestBody IndividualRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(individualService.recordPersonalData(
                        AuthUtil.getCurrentUserId(userDetails),
                        request
                ));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @RequireUserStatus(allowed = {UserStatus.PENDING_PROFILE, UserStatus.ACTIVE, UserStatus.INACTIVE})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update your personal data profile")
    public ResponseEntity<IndividualResponse> updateYour(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody IndividualRequest request
    ) {
        return ResponseEntity.ok(individualService.updateYour(
                AuthUtil.getCurrentUserId(userDetails),
                request
        ));
    }
}
