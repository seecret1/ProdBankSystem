package com.github.seecret1.office_service.controller.privates;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.office_service.dto.request.OfficeCreateRequest;
import com.github.seecret1.office_service.dto.response.OfficeResponse;
import com.github.seecret1.office_service.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public PageResponse<OfficeResponse> findAll(
            @RequestParam PageModel pageModel
    ) {
        return officeService.findAll(pageModel);
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
    public OfficeResponse findById(
            @PathVariable String id
    ) {
        return officeService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create office", description = "Create office in city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success created office"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public OfficeResponse createOffice(
            @Valid @RequestBody OfficeCreateRequest request
    ) {
        return officeService.create(request);
    }
}
