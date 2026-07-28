package com.github.seecret1.office_service.controller.privates;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.jwt_common.security.UserPrincipal;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.request.OfficeUpdateRequest;
import com.github.seecret1.office_service.dto.response.OfficeFullResponse;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.service.OfficeService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/private/offices")
@Tag(name = "Office Management", description = "API for managing offices")
@SecurityRequirement(name = "bearerAuth")
public class OfficePrivateController {

    private final OfficeService officeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    @Operation(summary = "Find all offices", description = "Find all offices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success find offices"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<OfficeFullResponse>> findAll(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(officeService.findAll(pageModel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    @Operation(summary = "Find office by ID", description = "Find office by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success find office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    public ResponseEntity<OfficeResponse> findById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(officeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create office", description = "Create office in city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success created office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<OfficeFullResponse> createOffice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody OfficeCreateRequest request
    ) {
        return ResponseEntity.ok(officeService.create(userPrincipal.getUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update office", description = "Update office in city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success updated office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<OfficeResponse> updateOffice(
            @PathVariable String id,
            @Valid @RequestBody OfficeUpdateRequest request
    ) {
        return ResponseEntity.ok(officeService.updateOffice(id, request));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Block office", description = "Block office from city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success blocked office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> blockOffice(
            @PathVariable String id
    ) {
        officeService.blocked(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete office", description = "Delete office from city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success deleted office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteOffice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id
    ) {
        officeService.delete(userPrincipal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
