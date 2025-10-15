package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
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
public class TransactionController {

    private final CardService cardService;

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