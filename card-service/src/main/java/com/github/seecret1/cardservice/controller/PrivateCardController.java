package com.github.seecret1.cardservice.controller;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.request.UpdateStatusCardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.cardservice.service.CardService;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
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

@Validated
@RestController
@RequestMapping("/api/v1/private/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management (Admin)", description = "Admin API for card management")
@SecurityRequirement(name = "bearerAuth")
public class PrivateCardController {

    private final CardService cardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get all cards", description = "Retrieve paginated list of all cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get all cards"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<CardResponse>> findAllCards(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(cardService.findAll(pageModel));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get cards by filter", description = "Retrieve paginated list of cards by filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get cards by filter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<CardResponse>> findCardsByFilter(
            @Valid CardFilterModel filter
    ) {
        return ResponseEntity.ok(cardService.findByFilter(filter));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create card", description = "Create new card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success create new card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardRequest cardRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.create(cardRequest));
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update status", description = "Update card status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success update status card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> updateCard(
            @Valid @RequestBody UpdateStatusCardRequest request
    ) {
        return ResponseEntity.ok(cardService.updateStatus(request));
    }

    @DeleteMapping("/{cardCriterial}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete card", description = "Delete card by criterial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success delete card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteCards(
            @PathVariable String cardCriterial
    ) {
        cardService.delete(cardCriterial);
        return ResponseEntity.noContent().build();
    }
}
