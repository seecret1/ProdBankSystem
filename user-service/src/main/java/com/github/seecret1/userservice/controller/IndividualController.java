package com.github.seecret1.userservice.controller;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualRequest;
import com.github.seecret1.userservice.dto.response.IndividualResponse;
import com.github.seecret1.userservice.service.IndividualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.github.seecret1.userservice.utils.AuthUtil;
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
@RequestMapping("/api/v1/individuals")
@Tag(name = "Individual Management", description = "API for bank retail clients (individuals)")
public class IndividualController {

    private final IndividualService individualService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all individuals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Individual found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<IndividualResponse>> findAll(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(individualService.findAll(pageModel));
    }

    @GetMapping("/{criterial}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get individual by criterial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Individual found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Individual not found")
    })
    public ResponseEntity<IndividualResponse> findByCriterial(
            @PathVariable String criterial
    ) {
        return ResponseEntity.ok(individualService.findByCriterial(criterial));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
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

    @PutMapping("/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update individual profile")
    public ResponseEntity<IndividualResponse> update(
            @PathVariable String criterial,
            @Valid @RequestBody IndividualRequest request
    ) {
        return ResponseEntity.ok(individualService.update(criterial, request));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
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

    @DeleteMapping("/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft delete individual")
    public ResponseEntity<Void> softDelete(@PathVariable String criterial) {
        individualService.softDelete(criterial);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/compensateDelete/{criterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft delete individual")
    public ResponseEntity<Void> hardDelete(@PathVariable String criterial) {
        individualService.hardDelete(criterial);
        return ResponseEntity.noContent().build();
    }
}
