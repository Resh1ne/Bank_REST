package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "3. User Resources", description = "Endpoints for accessing user-specific data")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final CardService cardService;

    @Operation(summary = "Get cards for a specific user", description = "Retrieves a paginated and filterable list of cards. Requires ADMIN role OR the user must be requesting their own data.")
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @GetMapping("/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @Parameter(description = "ID of the user whose cards to retrieve") @PathVariable Long userId,
            @Parameter(description = "Filter by card status (e.g., ACTIVE, BLOCKED)") @RequestParam(required = false) CardStatus status,
            @Parameter(description = "Search by last 4 digits of the card number") @RequestParam(required = false, name = "search") String panLast4,
            @Parameter(hidden = true) Pageable pageable
    ) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, status, panLast4, pageable);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Request to block own card", description = "Allows an authenticated user to block one of their own cards. Requires USER role.")
    @ApiResponse(responseCode = "404", description = "Card not found or does not belong to the user", content = @Content)
    @PostMapping("/cards/{cardId}/block-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> requestBlock(
            @Parameter(description = "ID of the card to be blocked") @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardDto updatedCard = cardService.requestCardBlock(cardId, userDetails.getUsername());
        return ResponseEntity.ok(updatedCard);
    }
}