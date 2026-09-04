package com.github.seecret1.invoice_service.controller;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@Tag(name = "Operation Management", description = "API for operations CRUD with soft/hard delete (soft = isActive=false)")
public class OperationController {

    private final OperationService operationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get operation by id", description = "Get active operation by id (soft-deleted not visible)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found operation"),
            @ApiResponse(responseCode = "404", description = "Operation not found")
    })
    public ResponseEntity<OperationResponse> findById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(operationService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all operations", description = "Get paginated list of active operations (isActive=true)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found operations page")
    })
    public ResponseEntity<PageResponse<OperationResponse>> findAll(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(operationService.findAll(pageModel));
    }

    @GetMapping("/all-including-inactive")
    @Operation(summary = "Get all operations including inactive", description = "Get paginated list of all operations regardless of isActive (for admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found operations page")
    })
    public ResponseEntity<PageResponse<OperationResponse>> findAllIncludingInactive(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(operationService.findAllIncludingInactive(pageModel));
    }

    @PostMapping
    @Operation(summary = "Create operation", description = "Create new operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operation created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<OperationResponse> create(
            @Valid @RequestBody OperationCreateRequest request
    ) {
        OperationResponse response = operationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete operation", description = "Soft delete operation by id (sets isActive=false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Soft deleted"),
            @ApiResponse(responseCode = "404", description = "Operation not found"),
            @ApiResponse(responseCode = "410", description = "Already soft-deleted")
    })
    public ResponseEntity<Void> softDelete(
            @PathVariable String id
    ) {
        operationService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{id}")
    @Operation(summary = "Hard delete operation", description = "Hard delete operation by id (physical removal from DB)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hard deleted"),
            @ApiResponse(responseCode = "404", description = "Operation not found")
    })
    public ResponseEntity<Void> hardDelete(
            @PathVariable String id
    ) {
        operationService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
