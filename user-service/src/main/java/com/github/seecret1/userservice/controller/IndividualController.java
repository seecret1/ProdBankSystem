package com.github.seecret1.userservice.controller;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.userservice.dto.request.IndividualWriteDto;
import com.github.seecret1.userservice.dto.response.IndividualDto;
import com.github.seecret1.userservice.dto.response.IndividualWriteResponseDto;
import com.github.seecret1.userservice.service.IndividualService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/individuals")
@Tag(name = "Individual Management", description = "API for bank retail clients (individuals)")
public class IndividualController {

    private final IndividualService individualService;

    @PostMapping
    @Operation(summary = "Register individual", description = "Create client profile with linked user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Individual successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "User or individual already exists")
    })
    public ResponseEntity<IndividualWriteResponseDto> register(
            @Valid @RequestBody IndividualWriteDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(individualService.register(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get individual by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Individual found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Individual not found")
    })
    public ResponseEntity<IndividualDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(individualService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Find individuals by emails", description = "Admin lookup by email set")
    public ResponseEntity<PageResponse<IndividualWriteResponseDto>> findByEmails(
            @RequestParam(required = false) Set<String> emails,
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(individualService.findByEmails(emails, pageModel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update individual profile")
    public ResponseEntity<IndividualWriteResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody IndividualWriteDto request
    ) {
        return ResponseEntity.ok(individualService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft delete individual")
    public ResponseEntity<Void> softDelete(@PathVariable String id) {
        individualService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/compensateDelete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft delete individual")
    public ResponseEntity<Void> hardDelete(@PathVariable String id) {
        individualService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
