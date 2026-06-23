package com.github.seecret1.cardservice.controller;

import com.github.seecret1.cardservice.dto.response.CardResponse;
import com.github.seecret1.cardservice.security.UserPrincipal;
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

    @GetMapping("/{criterial}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(summary = "Get card by criterial", description = "Get card by ID or number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success get card by ID or number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CardResponse> findByCriterial(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String criterial
    ) {
        return ResponseEntity.ok(cardService.findByCriterial(criterial));
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
}
