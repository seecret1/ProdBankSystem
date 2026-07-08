package com.github.seecret1.cardservice.controller;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.model.CardFilterModel;
import com.github.seecret1.cardservice.service.CardService;
import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.jwt_common.security.UserPrincipal;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/private/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management (Admin)", description = "Admin API for card management")
@SecurityRequirement(name = "bearerAuth")
public class PrivateCardController {

    private final CardService cardService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get card by ID", description = "Get card by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get card by ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> findById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(cardService.findById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get only not deleted cards", description = "Retrieve paginated list of only not deleted cards")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get all cards"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<CardResponse>> findOnlyNotDeleted(
            @Valid PageModel pageModel
    ) {
        return ResponseEntity.ok(cardService.findOnlyNotDeleted(pageModel));
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

    @PatchMapping("/update-status/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update status", description = "Update card status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success update status card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable String id,
            @RequestParam CardStatus status
    ) {
        return ResponseEntity.ok(cardService.updateStatus(id, status));
    }

    @PatchMapping("/extend/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Extend card", description = "Extend the validity period of the card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success extend card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "402", description = "Payment Required"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> extendCard(
            @PathVariable String id,
            @RequestParam LocalDate dateExpiry
    ) {
        return ResponseEntity.ok(cardService.extendCard(id, dateExpiry));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Soft delete card", description = "Soft delete card by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success delete card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "410", description = "Gone")
    })
    public ResponseEntity<Void> softDelete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String cardId
    ) {
        String userId = userPrincipal.getUserId();
        cardService.softDelete(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard-delete/{cardId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Hard delete card", description = "Hard delete card by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success delete card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> hardDelete(
            @PathVariable String cardId
    ) {
        cardService.hardDelete(cardId);
        return ResponseEntity.noContent().build();
    }
}
