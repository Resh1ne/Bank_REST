package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "4. Transactions", description = "Endpoints for performing financial transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final CardService cardService;

    @Operation(summary = "Transfer funds between own cards", description = "Performs a fund transfer between two cards belonging to the authenticated user. This operation is transactional. Requires USER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer successful", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid operation (e.g., insufficient funds, card not active)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found or does not belong to the user", content = @Content)
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionDto> createTransfer(@Valid @RequestBody TransferRequestDto transferRequest,
                                                         @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String currentUsername = userDetails.getUsername();
        TransactionDto createdTransaction = cardService.transferBetweenOwnCards(currentUsername, transferRequest);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }
}