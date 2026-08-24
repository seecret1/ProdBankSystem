package com.github.seecret1.invoice_service.controller;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.service.CardInvoiceService;
import com.github.seecret1.jwt_common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Card Invoice Management", description = "API for card invoices CRUD with soft/hard delete")
public class CardInvoiceController {

    private final CardInvoiceService cardInvoiceService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get invoice by id", description = "Get not-deleted invoice by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found invoice"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<CardInvoiceResponse> findById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(cardInvoiceService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get all invoices", description = "Get paginated list of not-deleted invoices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found invoices page")
    })
    public ResponseEntity<PageResponse<CardInvoiceResponse>> findAll(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(cardInvoiceService.findAll(pageModel));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Create invoice", description = "Create new card invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invoice created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Invoice already exists")
    })
    public ResponseEntity<CardInvoiceResponse> create(
            @Valid @RequestBody CardInvoiceCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardInvoiceService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Soft delete invoice", description = "Soft delete invoice by id (sets deleted=true, deletedAt, deletedBy)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Soft deleted"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "410", description = "Already deleted")
    })
    public ResponseEntity<Void> softDelete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id
    ) {
        cardInvoiceService.softDelete(id, userPrincipal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Hard delete invoice", description = "Hard delete invoice by id (physical removal from DB)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hard deleted"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<Void> hardDelete(
            @PathVariable String id
    ) {
        cardInvoiceService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
