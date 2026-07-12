package com.github.seecret1.cardservice.controller;

import com.github.seecret1.cardservice.dto.request.CardRequest;
import com.github.seecret1.cardservice.dto.response.CardResponse;
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


@Validated
@RestController
@RequestMapping("/api/v1/public/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "User endpoints for card operations")
@SecurityRequirement(name = "bearerAuth")
public class PublicCardController {

    private final CardService cardService;

    @GetMapping("/card-number/{number}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get card by number", description = "Get card by number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get card by number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> findByNumber(
            @PathVariable String number
    ) {
        return ResponseEntity.ok(cardService.findByNumber(number));
    }

    @GetMapping("/your")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get all your cards", description = "Get all cards belonging to current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get cards to current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PageResponse<CardResponse>> findYourCards(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid PageModel pageModel
    ) {
        String userId = userPrincipal.getUserId();
        return ResponseEntity.ok(cardService.findYourCards(
                userId,
                pageModel
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Create card", description = "Create new card")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success create new card"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> createCard(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CardRequest cardRequest
    ) {
        String userId = userPrincipal.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardService.create(userId, cardRequest));
    }
}
