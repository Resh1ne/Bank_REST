package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "2. Administration", description = "Endpoints for administrators to manage cards and users")
@SecurityRequirement(name = "bearerAuth")
public class AdminCardController {

    private final CardService cardService;

    @Operation(summary = "Create a new card for any user", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Card created successfully", content = @Content)
    @ApiResponse(responseCode = "404", description = "Owner user not found", content = @Content)
    @PostMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest createCardRequest) {
        CardDto newCard = cardService.createCard(createCardRequest);
        return new ResponseEntity<>(newCard, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a paginated list of all cards", description = "Retrieves all cards in the system. Requires ADMIN role.")
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @Operation(summary = "Block a card by ID", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    @PostMapping("/cards/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> blockCard(@Parameter(description = "ID of the card to be blocked") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    @Operation(summary = "Activate a card by ID", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    @PostMapping("/cards/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> activateCard(@Parameter(description = "ID of the card to be activated") @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    @Operation(summary = "Delete a card by ID", description = "Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Card deleted successfully", content = @Content)
    @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    @DeleteMapping("/cards/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@Parameter(description = "ID of the card to be deleted") @PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}