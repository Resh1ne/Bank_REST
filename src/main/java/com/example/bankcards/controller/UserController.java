package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
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
public class UserController {

    private final CardService cardService;

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    @GetMapping("/{userId}/cards")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @PathVariable Long userId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false, name = "search") String panLast4,
            Pageable pageable
    ) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, status, panLast4, pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/cards/{cardId}/block-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> requestBlock(@PathVariable Long cardId, @AuthenticationPrincipal UserDetails userDetails) {
        CardDto updatedCard = cardService.requestCardBlock(cardId, userDetails.getUsername());
        return ResponseEntity.ok(updatedCard);
    }
}